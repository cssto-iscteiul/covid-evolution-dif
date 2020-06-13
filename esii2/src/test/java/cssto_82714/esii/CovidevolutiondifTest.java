package cssto_82714.esii;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

import junit.framework.TestCase;

public class CovidevolutiondifTest extends TestCase {
	

	public void testCreateHTML() {
		Covidevolutiondif.createHTML();
		assertEquals("Didnt create div 1", 1, Covidevolutiondif.doc.select("#ta01").size());
		assertEquals("Didnt create div 2", 1, Covidevolutiondif.doc.select("#ta02").size());
	}


	public void testInList() {
		String str = "hello";
		String str2 = "world";

		Covidevolutiondif.differences.add("hello");

		assertEquals(Covidevolutiondif.inList(str), true);
		assertEquals(Covidevolutiondif.inList(str2), false);

	}


	public void testWriteIntoTextArea() {

		Covidevolutiondif.createHTML();
		Covidevolutiondif.differences.add("hello");
		Covidevolutiondif.file1 = "file1.txt";
		Covidevolutiondif.file2 = "file2.txt";

		int size1 = Covidevolutiondif.doc.select("#ta01").size();
		int size2 = Covidevolutiondif.doc.select("#ta02").size();

		try {
			Covidevolutiondif.writeIntoTextArea();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals("Checking number lines of div1", size1, Covidevolutiondif.doc.select("#ta01").size());
		assertEquals("Checking number lines of div2", size2, Covidevolutiondif.doc.select("#ta02").size());

	}


	public void testFindDifferences() {

		Covidevolutiondif.files.add("file1.txt");
		Covidevolutiondif.files.add("file2.txt");

		Date date1 = new GregorianCalendar(2010, Calendar.MAY, 26).getTime();
		Date date2 = new GregorianCalendar(2010, Calendar.MAY, 27).getTime();

		Covidevolutiondif.dates.add(date1);
		Covidevolutiondif.dates.add(date2);

		try {
			Covidevolutiondif.findDifferences();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals("Checking if differences were found", 1, Covidevolutiondif.differences.size());
	}
	
	

	public void testOpenRepository() { 

		Covidevolutiondif.openRepository();
		Git git = Covidevolutiondif.git;

		assertNotNull("Git repository", git);		
	}
	

	public void testFindFileInCommit() { 

		Repository repository = Covidevolutiondif.git.getRepository();
		
		try {
			List<Ref> call = Covidevolutiondif.git.tagList().call();

			for (Ref ref : call) {
				String tag = ref.getName();
				RevWalk walk = new RevWalk(repository);
				try {
					RevObject object = walk.parseAny(ref.getObjectId());

					if (object instanceof RevCommit) {
						Covidevolutiondif.findFileInCommit(ref.getObjectId(), tag);
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

		assertEquals("Checking if files were found", false, Covidevolutiondif.files.isEmpty());
		assertEquals("Checking if dates were added", false, Covidevolutiondif.dates.isEmpty());

	}
	
	public void testGetFilesFromTags() {
		
		Covidevolutiondif.getFilesFromTags();
		
		assertEquals("Checking if files were found", false, Covidevolutiondif.files.isEmpty());
		assertEquals("Checking if dates were added", false, Covidevolutiondif.dates.isEmpty());
		
	}



}
