package org.mneidinger.windydays.githook;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CommitMsgHook {

	public static void main(String[] args) throws Exception{
		new CommitMsgHook().processHook(args[0]);
	}
	
	public void processHook(String commitEditMsgFileLocation) throws IOException {
		String linesFromFile = Files.readString(Paths.get(commitEditMsgFileLocation));
		List<String> lines = readAllLinesPreserveLineEnd(linesFromFile);
		validateCommitMessage(lines);
		if(lines.size()>2 && wordWrapNeeded(linesFromFile)) {
			autoFormatThirdToManyLines(lines, Paths.get(commitEditMsgFileLocation));
		}
		System.exit(0);
	}
	
	private List<String> readAllLinesPreserveLineEnd(String linesFromFile){
		List<String> lines = new ArrayList<String>();
		StringBuilder line = new StringBuilder();
		for(int i=0;i<linesFromFile.length();i++) {
			char c = linesFromFile.charAt(i);
			line.append(c);
			if(c=='\n') {
				lines.add(line.toString());
				line.delete(0, line.length());
			}
		}
		if(!line.isEmpty()) {
			lines.add(line.toString());
			line.delete(0, line.length());
		}
		return lines;
	}
	
	public boolean wordWrapNeeded(String line) {
		return line.length() > 176-line.substring(0, line.indexOf("\n")).length();
	}
	
	public void validateCommitMessage(List<String> readAllLines) throws IOException{
		String header = readAllLines.get(0);
		System.out.println("Commit message header length: "+header.strip().length());
		if(header.strip().length()>72) {
			System.out.println("Commit message header longer than 72 characters. Please reduce length.");
			System.exit(1); //Exit in error resulting in blocking the commit
		}		
		if(readAllLines.size()>1 && !System.lineSeparator().contains(readAllLines.get(1))) {
			System.out.println("Second line of commit message is not blank. Please add blank second line to separate message header from body.");
			System.exit(1); //Exit in error resulting in blocking the commit
		}
		System.out.println("Commit message passes validation");
	}
	
	public void autoFormatThirdToManyLines(List<String> lines, Path path) throws IOException {
		VisitConsumer vc = new VisitConsumer();
		vc.linesToPrint.add(lines.get(0));
		vc.linesToPrint.add(lines.get(1));
		lines.subList(2, lines.size()).forEach(line ->{
			String[] splitLine = line.split(" ");
			Stream.of(splitLine).forEach(word -> vc.accept(word));
			vc.flush();
		});
		vc.stripLineSeparator();
		try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
			Iterator<String> iterator = vc.linesToPrint.iterator();
			while(iterator.hasNext()) {
				newBufferedWriter.write(iterator.next());
			}			
		}
		System.out.println("Commit message body auto formatted to 100 characters");
	}

	private class VisitConsumer implements Consumer<String>{
		private List<String> linesToPrint = new ArrayList<String>();
		private StringBuilder line = new StringBuilder();
		
		@Override
		public void accept(String word) {
			if(line.length() + word.length() + 1 > 100) {
				flush();
			}
			line.append(word).append(" ");
		}
			
		private void flush() {
			linesToPrint.add(line.toString().strip()+System.lineSeparator());
			line.delete(0, line.length());
		}
		
		private void stripLineSeparator() {
			linesToPrint.set(linesToPrint.size()-1, linesToPrint.get(linesToPrint.size()-1).replace("\r\n", ""));
		}
	}
}
