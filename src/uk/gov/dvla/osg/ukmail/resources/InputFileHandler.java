package uk.gov.dvla.osg.ukmail.resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class InputFileHandler {
	String line = null;
	
	
	
	public String getNextBagRef(String filename) throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String str = br.readLine();
		br.close();
		return str;
	}
	
	public PrintWriter createOutputFileWriter(String filename){
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true), 1000 * 1024));
		     return out;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(3);
		}
		return null;
	}
	
	public void appendToFile(PrintWriter out, String output){
		out.println(output);
	}
	
	public void write(String filename, String output){
		try{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true), 10 * 1024));
			out.println(output);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	public void closeFile(PrintWriter out){
		out.close();
	}
	
	public void deleteFile(String filename){
		File f = new File(filename);
		if(f.exists() && !f.isDirectory()){
			f.delete();
		}
	}

	public void writeReplace(String filename, String output){
		try{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, false), 10 * 1024));
			out.println(output);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	public boolean checkFileExists(String filename){
		
		File file = new File(filename);
		return file.exists();
	
	}
}
