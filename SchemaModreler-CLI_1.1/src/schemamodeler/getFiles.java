package schemamodeler;

import java.io.File;
import java.util.*;

public class getFiles {
	


	public static void listSchemaNames(File dir, List<File> fileNames, List<String> schemaNames) {
	    
	    if(dir==null||dir.listFiles()==null){
	        return ;
	    }
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) {
	        	schemaNames.add(entry.getName());
	        	fileNames.add(entry);
	        }
	        else listSchemaNames(entry, fileNames, schemaNames);
	    }
	    return ;
	}
	
	public static void main (String[] args) {
		File path = new File("c:\\path"); 
 		 List<String> schemaNames = new ArrayList<String>();
		 List<File> fileNames = new ArrayList<File>();
		listSchemaNames(path, fileNames, schemaNames);
		System.out.println(fileNames);
		System.out.println(schemaNames);
		
		
	}
}
	

