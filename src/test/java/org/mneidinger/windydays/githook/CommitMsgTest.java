package org.mneidinger.windydays.githook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.stefanbirkner.systemlambda.SystemLambda;

public class CommitMsgTest {

	private CommitMsgHook commitmsg = new CommitMsgHook();
	
	@Test
	public void testMainValidLengthHeader() throws Exception{
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					CommitMsgHook.main(new String[]{"src/test/resources/msg_header_valid_length"});
				})
			);
		});
		assertEquals(0, status.get(0), "outMessage value: "+outMessage);
		assertEquals("Commit message header length: 29"+System.lineSeparator()+"Commit message passes validation"+System.lineSeparator(), outMessage);		
	}
	
	@Test
	public void testValidLengthHeader() throws Exception {
		List<Integer> status = new ArrayList<>();
		List<String> readAllLines = Files.readAllLines(Paths.get("src/test/resources/msg_header_valid_length"));
		String outMessage = SystemLambda.tapSystemOut( () -> {
			try {
				status.add( 
					SystemLambda.catchSystemExit( () -> {
						commitmsg.validateCommitMessage(readAllLines);
					})
				);
			}catch(AssertionError ae) {} //on successful test run, System.exit() isn't called so catch assertionError and ignore it
		});
		assertEquals(0, status.size(), "System.exit() shouldn't have been called");
		assertEquals("Commit message header length: 29"+System.lineSeparator()+"Commit message passes validation"+System.lineSeparator(), outMessage);
	}
	
	@Test
	public void testThreeLineValid() throws Exception {
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			try {
				status.add( 
					SystemLambda.catchSystemExit( () -> {
						commitmsg.processHook("src/test/resources/msg_three_line_valid");
					})
				);
			}catch(AssertionError ae) {} // on successful test run, System.exit() isn't called so catch AssertionError and ignore it.
		});
		assertEquals(1, status.size(), "outMessage value: "+outMessage);
		assertEquals("Commit message header length: 45"+System.lineSeparator()+"Commit message passes validation"+System.lineSeparator(), outMessage);
	}
	
	@Test
	public void testTooLongHeader() throws Exception {
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					commitmsg.processHook("src/test/resources/msg_header_too_long");
				})
			);
		});
		assertEquals(1, status.get(0), "outMessage value: "+outMessage);
		assertEquals("Commit message header length: 76"+System.lineSeparator()+"Commit message header longer than 72 characters. Please reduce length."+System.lineSeparator(), outMessage);
	}

	@Test
	public void testSecondLineNotBlank() throws Exception {
		List<String> readAllLines = Files.readAllLines(Paths.get("src/test/resources/msg_second_line_not_blank"));
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					commitmsg.validateCommitMessage(readAllLines);
				})
			);
		});
		assertEquals(1, status.get(0), "outMessage value: "+outMessage);
		assertEquals("Commit message header length: 44"+System.lineSeparator()+"Second line of commit message is not blank. Please add blank second line to separate message header from body."+System.lineSeparator(), outMessage);
	}
	
	@Test
	public void testWrapThirdLineOnce() throws Exception {
		Path path = Paths.get("src/test/resources/msg_third_line_118_characters");
		List<String> readAllLines = Files.readAllLines(path);
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
					SystemLambda.catchSystemExit( () -> {
						commitmsg.processHook(path.toString());
					})
				);
		});
		assertEquals(0 , status.get(0), "outMessage value: "+outMessage);
		assertTrue(outMessage.contains("Commit message body auto formatted to 100 characters"), "outMessage value "+outMessage);
		List<String> readAllLines2 = Files.readAllLines(path);
		assertEquals(4, readAllLines2.size());
		assertEquals("over the lazy dog", readAllLines2.get(3));
		resetTestFile(readAllLines, path);
	}
	
	@Test
	public void testWrapThirdLineTwice() throws Exception {
		Path path = Paths.get("src/test/resources/msg_third_line_235_characters");
		List<String> readAllLines = Files.readAllLines(path);
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
					SystemLambda.catchSystemExit( () -> {
						commitmsg.processHook(path.toString());
					})
				);
		});
		assertEquals(0 , status.get(0), "outMessage value: "+outMessage);
		assertTrue(outMessage.contains("Commit message body auto formatted to 100 characters"), "outMessage value "+outMessage);
		List<String> readAllLines2 = Files.readAllLines(path);
		assertEquals(5, readAllLines2.size());
		assertEquals("quick brown fox jumped over the lazy dog", readAllLines2.get(4));
		resetTestFile(readAllLines, path);
	}

	@Test
	public void testWrapThirdLineOnceFourthLine11Characters() throws Exception {
		Path path = Paths.get("src/test/resources/msg_third_line_118_characters_fourth_line_10_characters");
		List<String> readAllLines = Files.readAllLines(path);
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
					SystemLambda.catchSystemExit( () -> {
						commitmsg.processHook(path.toString());
					})
				);
		});
		assertEquals(0 , status.get(0), "outMessage value: "+outMessage);
		assertTrue(outMessage.contains("Commit message body auto formatted to 100 characters"), "outMessage value "+outMessage);
		List<String> readAllLines2 = Files.readAllLines(path);
		assertEquals(5, readAllLines2.size());
		assertEquals("over the lazy dog", readAllLines2.get(3));
		assertEquals("Warm Kitty", readAllLines2.get(4));
		resetTestFile(readAllLines, path);
	}
	
	@Test
	public void testWrapThirdAndFithLines() throws Exception {
		Path path = Paths.get("src/test/resources/msg_third_line_235_characters_fourth_line_10_characters_fifth_line_111_characters_sixth_line_10_characters");
		List<String> readAllLines = Files.readAllLines(path);
		List<Integer> status = new ArrayList<>();
		String outMessage = SystemLambda.tapSystemOut( () -> {
			status.add( 
					SystemLambda.catchSystemExit( () -> {
						commitmsg.processHook(path.toString());
					})
				);
		});
		assertEquals(0 , status.get(0), "outMessage value: "+outMessage);
		assertTrue(outMessage.contains("Commit message body auto formatted to 100 characters"), "outMessage value "+outMessage);
		List<String> readAllLines2 = Files.readAllLines(path);
		assertEquals(9, readAllLines2.size());
		assertEquals("quick brown fox jumped over the lazy dog", readAllLines2.get(4));
		assertEquals("Warm Kitty", readAllLines2.get(5));
		assertEquals("like it also", readAllLines2.get(7));
		assertEquals("Warm Kitty", readAllLines2.get(8));
		resetTestFile(readAllLines, path);
	}
	
	private void resetTestFile(List<String> lines, Path path) throws IOException {
		BufferedWriter newBufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING);
		Iterator<String> iterator =lines.iterator();
		while(iterator.hasNext()) {
			String line = iterator.next();
			newBufferedWriter.write(line);
			if(iterator.hasNext()) {
				newBufferedWriter.newLine();
			}
		}
		newBufferedWriter.close();
	}
}
