package org.mneidinger.windydays.githook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class CommitMsgHook {

	public static void main(String[] args) throws Exception{
		new CommitMsgHook().processHook(args[0]);
	}
	
	private boolean thirdToManyLines;
	
	public void processHook(String commitEditMsgFileLocation) throws IOException {
		File commitMessageFile = new File(commitEditMsgFileLocation);
		validateCommitMessage(commitEditMsgFileLocation);
		if(isThirdToManyLines()) {
			autoFormatThirdToManyLines(commitMessageFile);
		}
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
