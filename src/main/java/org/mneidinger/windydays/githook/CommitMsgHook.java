package org.mneidinger.windydays.githook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class CommitMsgHook {

	public static void main(String[] args) throws Exception{
		new CommitMsgHook().processHook(args[0]);
	}
	
	private boolean thirdToManyLines;
	
	public void processHook(String commitEditMsgFileLocation) throws FileNotFoundException {
		File commitMessageFile = new File(commitEditMsgFileLocation);
		validateCommitMsgHeaderAndSecondLine(commitMessageFile);
		if(isThirdToManyLines()) {
			autoFormatThirdToManyLines(commitMessageFile);
		}
		System.exit(0);
	}
	
	public void validateCommitMsgHeaderAndSecondLine(File commitMessageFile) throws FileNotFoundException {
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
			thirdToManyLines = validateScan.hasNextLine();
		}
		
		System.out.println("Commit message passes validation");
		validateScan.close();
	}
	
	public boolean isThirdToManyLines() {
		return thirdToManyLines;
	}
	
	public void autoFormatThirdToManyLines(File commitMessageFile) throws FileNotFoundException {
		Scanner readLines = new Scanner(commitMessageFile);

		VisitConsumer vc = new VisitConsumer();
		vc.addLine(readLines.nextLine());
		vc.addLine(readLines.nextLine());
		
		readLines.forEachRemaining(s -> vc.accept(s));
		vc.flush();
		readLines.close(); 
		
		PrintWriter pw = new PrintWriter(commitMessageFile);
		vc.getLinesToPrint().stream().forEach(line -> pw.println(line));
		
		pw.close();
	}
	
	private class VisitConsumer implements Consumer<String>{

		private List<String> linesToPrint = new ArrayList<String>();
		private StringBuilder line = new StringBuilder();
		
		private void addLine(String line) {
			linesToPrint.add(line);
		}
		
		@Override
		public void accept(String word) {
			if(line.length() + word.length() + 1 > 100) {
				flush();
			}
			line.append(word).append(" ");
		}
			
		private void flush() {
			linesToPrint.add(line.toString().strip());
			line.delete(0, line.length());
		}
		
		private List<String> getLinesToPrint(){
			return linesToPrint;
		}
	}
}
