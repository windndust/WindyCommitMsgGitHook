package org.mneidinger.windydays.githook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CommitMsgHook {

	public static void main(String[] args) throws Exception{
		new CommitMsgHook().validateCommitMessage(args[0]);
		System.exit(0);
	}
	
	public void validateCommitMessage(String commitEditMsgFileLocation) throws IOException{
		List<String> readAllLines = Files.readAllLines(Paths.get(commitEditMsgFileLocation));
		String header = readAllLines.get(0);
		System.out.println("Commit message header length: "+header.length());
		if(header.length()>72) {
			System.out.println("Commit message header longer than 72 characters. Please reduce length.");
			System.exit(1); //Exit in error resulting in blocking the commit
		}		
		if(readAllLines.size()>1 && !"".equals(readAllLines.get(1))) {
			System.out.println("Second line of commit message is not blank. Please add blank second line to separate message header from body.");
			System.exit(1); //Exit in error resulting in blocking the commit
		}
		System.out.println("Commit message passes validation");
	}
}
