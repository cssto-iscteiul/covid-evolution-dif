package cssto_82714.esii;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 * @author Catarina Teodoro
 *
 */
public class Covidevolutiondif 
{

	public static Git git;
	public static Document doc;
	public static List<String> differences = new ArrayList<String>();
	public static List<String> files = new ArrayList<String>();
	public static List<Date> dates = new ArrayList<Date>();
	public static String file1;
	public static String file2;

	public static void main( String[] args ) throws InvalidRemoteException, TransportException, GitAPIException, IOException
	{

		createHTML();
		openRepository();
		getFilesFromTags();
		findDifferences();

		try {
			writeIntoTextArea();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(cgi_lib.Header());
		Hashtable form_data = cgi_lib.ReadParse(System.in);
		System.out.println(doc.select("#ta01"));
		System.out.println(doc.select("#ta02"));
		System.out.println(doc.select("style"));
		System.out.println(cgi_lib.HtmlBot());


	}

	/**
	 * Lists all tags from repository and searches for the files associated to each tag
	 */
	public static void getFilesFromTags() {
		Repository repository = git.getRepository();
		try {
			List<Ref> call = git.tagList().call();

			for (Ref ref : call) {
				String tag = ref.getName();
				RevWalk walk = new RevWalk(repository);
				try {
					RevObject object = walk.parseAny(ref.getObjectId());

					if (object instanceof RevCommit) {
						findFileInCommit(ref.getObjectId(), tag);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * Given a commitId and the tag associated searches for the file covid19spreading.rdf
	 * If the file is found the information is added to the html table
	 * 
	 * @param commitId
	 * @param tag
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws CorruptObjectException
	 * @throws IOException
	 */
	public static void findFileInCommit(ObjectId commitId, String tag) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		Repository repository = git.getRepository();
		String[] tags = tag.split("/");
		String finalTag = tags[2];

		try(RevWalk revWalk = new RevWalk(repository)){
			RevCommit commit = revWalk.parseCommit(commitId);

			RevTree tree = commit.getTree();

			try(TreeWalk treeWalk = new TreeWalk(repository)){
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create("covid19spreading.rdf"));
				if(!treeWalk.next()) {
					throw new IllegalStateException("Didnt find file");
				}

				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);
				Date time = commit.getAuthorIdent().getWhen();
				String fileName = time.toString();
				FileOutputStream fos = new FileOutputStream(finalTag+".txt");
				loader.copyTo(fos);

				dates.add(time);
				files.add(finalTag+".txt");

			}

			revWalk.dispose();
		}
	}

	/**
	 * Creates html divs and adds style to them
	 */
	public static void createHTML() {

		doc = Jsoup.parse("<html></html>");
		doc.body().addClass("body-styles-cls");
		doc.body().appendElement("div").attr("id", "ta01").attr("class", "split right");
		doc.body().appendElement("div").attr("id", "ta02").attr("class", "split left");
		doc.body().appendElement("style");

		Element style = doc.select("style").get(0);
		style.append("textarea {width: 50%; height: 100%;}.split {\r\n" + 
				"	height: 100%;\r\n" + 
				"	width: 50%;\r\n" + 
				"	position: fixed;\r\n" + 
				"	z-index: 1;\r\n" + 
				"	top: 0;\r\n" + 
				"	overflow-x: hidden;\r\n" + 
				"	padding-top: 20px;\r\n" + 
				"}\r\n" + 
				" \r\n" + 
				".left {\r\n" + 
				"	left: 0;\r\n" +  
				"	text-align: center;\r\n" + 
				"}\r\n" + 
				" \r\n" + 
				".right {\r\n" + 
				"	right: 0;\r\n" + 
				"	text-align: center;\r\n" + 
				"}");
	}

	/**
	 * 
	 * Checks if str is in the list differences
	 * 
	 * @param str
	 * @return
	 */
	public static boolean inList(String str) {
		for(int i=0; i!=differences.size(); i++) {
			if(str.contentEquals(differences.get(i))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * Writes each file we are comparing into each div for side by side comparison
	 * Highlights the lines that are different between both files in one of the files
	 * 
	 * @throws IOException
	 */
	public static void writeIntoTextArea() throws IOException {
		
		Elements div1 = doc.select("#ta01");
		Elements div2 = doc.select("#ta02");


		File myObj = new File(file1);
		Scanner myReader = new Scanner(myObj);

		while (myReader.hasNextLine()) {
			String data = myReader.nextLine();

			String str = data.replaceAll("<", "&lt;");
			str = str.replaceAll(">", "&gt;");

			if(inList(str)) {
				div1 .append("<mark>"+str+"</mark><br>");
			} else {
				div1 .append(str+"<br>");
			}

		}
		myReader.close();

		File myObj2 = new File(file2);
		Scanner myReader2 = new Scanner(myObj2);
		while (myReader2.hasNextLine()) {
			String data = myReader2.nextLine();
			String str = data.replaceAll("<", "&lt;");
			str = str.replaceAll(">", "&gt;");

			if(inList(str)) {
				div2 .append("<mark>"+str+"</mark><br>");
			} else {
				div2 .append(str+"<br>");
			}
		}
		myReader.close();

	}

	/**
	 * 
	 * Finds the differences between two files and saves them into the list differences
	 * 
	 * @throws IOException
	 */
	public static void findDifferences() throws IOException {

		lastTwoFiles();

		BufferedReader br = null;
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		String sCurrentLine;
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		br1 = new BufferedReader(new FileReader(file1));
		br2 = new BufferedReader(new FileReader(file2));
		while ((sCurrentLine = br1.readLine()) != null) {

			list1.add(sCurrentLine);
		}
		while ((sCurrentLine = br2.readLine()) != null) {
			list2.add(sCurrentLine);
		}
		List<String> tmpList = new ArrayList<String>(list1);
		tmpList.removeAll(list2);
		for(int i=0;i<tmpList.size();i++){
			String str = tmpList.get(i).replaceAll("<", "&lt;");
			str = str.replaceAll(">", "&gt;");
			differences.add(str);
		}

	}

	/**
	 * 
	 * Finds the last two covid19spreading.rdf files
	 * 
	 */
	public static void lastTwoFiles() {		

		Date mostRecent = dates.get(0);
		Date secondMostRecent = dates.get(0);

		for(int i=1; i!=dates.size(); i++) {
			if(dates.get(i).after(mostRecent)) {
				mostRecent = dates.get(i);
				file1 = files.get(i);				
			}			
		}
		for(int i=1; i!=dates.size(); i++) {
			if(dates.get(i).before(mostRecent)) {
				secondMostRecent = dates.get(i);
				file2 = files.get(i);
			}			
		}
	}


	/**
	 * Accesses the git repository to export covid19spreading.rdf files
	 */
	public static void openRepository() {

		File f = new File("./ESII1920");
		if (f.exists() && f.isDirectory()) {
			try {
				FileUtils.cleanDirectory(f);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			git = Git.cloneRepository()
					.setURI("https://github.com/vbasto-iscte/ESII1920")
					.call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
