package org.mneidinger.windydays.githook;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CommitMsgHook {

	public static void main(String[] args) throws Exception{
		new CommitMsgHook().validateCommitMessage(args[0]);
	}
	
	public void validateCommitMessage(String commitEditMsgFileLocation) throws FileNotFoundException {
		File commitMessageFile = new File(commitEditMsgFileLocation);
		Scanner validateScan = new Scanner(commitMessageFile);
		
		String line = validateScan.nextLine();
		System.out.println("Commit message header length: "+line.length());
		if(line.length()>72) {
			System.out.println("Commit message header longer than 72 characters. Please reduce length.");
			validateScan.close();
			System.exit(1); //Exit in error resulting in blocking the commit
		}
		if(validateScan.hasNextLine()) {
			line = validateScan.nextLine();
			if(!"".equals(line)) {
				System.out.println("Second line of commit message is not blank. Please add blank second line to separate message header from body.");
				validateScan.close();
				System.exit(1);
			}
		}
		
		System.out.println("Commit message passes validation");
		validateScan.close();
		System.exit(0);
	}
}
