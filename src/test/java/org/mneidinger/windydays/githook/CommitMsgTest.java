package org.mneidinger.windydays.githook;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.github.stefanbirkner.systemlambda.SystemLambda;

public class CommitMsgTest {

	private CommitMsgHook commitmsg = new CommitMsgHook();
	
	@Test
	public void testValidLengthHeader() throws Exception {
		List<Integer> status = new ArrayList<>();
		String out = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					commitmsg.validateCommitMessage("src/test/resources/msg_header_valid_length");
				})
			);
		});
		assertEquals(0, status.get(0));
		assertEquals("Commit message header length: 29"+System.lineSeparator()+"Commit message passes validation"+System.lineSeparator(), out);
	}
	
	@Test
	public void testThreeLineValid() throws Exception {
		List<Integer> status = new ArrayList<>();
		String out = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					commitmsg.validateCommitMessage("src/test/resources/msg_three_line_valid");
				})
			);
		});
		assertEquals(0, status.get(0));
		assertEquals("Commit message header length: 45"+System.lineSeparator()+"Commit message passes validation"+System.lineSeparator(), out);
	}
	
	@Test
	public void testTooLongHeader() throws Exception {
		List<Integer> status = new ArrayList<>();
		String out = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					commitmsg.validateCommitMessage("src/test/resources/msg_header_too_long");
				})
			);
		});
		assertEquals(1, status.get(0));
		assertEquals("Commit message header length: 76"+System.lineSeparator()+"Commit message header longer than 72 characters. Please reduce length."+System.lineSeparator(), out);
	}

	@Test
	public void testSecondLineNotBlank() throws Exception {
		List<Integer> status = new ArrayList<>();
		String out = SystemLambda.tapSystemOut( () -> {
			status.add( 
				SystemLambda.catchSystemExit( () -> {
					commitmsg.validateCommitMessage("src/test/resources/msg_second_line_not_blank");
				})
			);
		});
		assertEquals(1, status.get(0));
		assertEquals("Commit message header length: 44"+System.lineSeparator()+"Second line of commit message is not blank. Please add blank second line to separate message header from body."+System.lineSeparator(), out);
	}
	
}
