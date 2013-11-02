package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DataInfo {
	private String file;

	public DataInfo(String file) {
		this.file = file;
	}
	
	public void clearDataInfo() {
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
		} catch (IOException e) {
			System.out.println("Write to DataInfo failed: " + e);
		}
	}
	
	public void writeDataInfo(String text) {
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true));
			buffer.append(text + "\n");
			buffer.flush();
			buffer.close();
		} catch (IOException e) {
			System.out.println("Write to DataInfo failed: " + e);
		}
		
	}
	
	public String getFileName(){
		
		return file;
	}
	
	public boolean existFile(){
		
		File f = new File(file);
		if(f.exists()) { /* do something */	
			return true;
		}else{
			return false;
		}
		
	}
	
}
