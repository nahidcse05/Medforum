/**
 * 
 */
package analyzer;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collections;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;




public class filecollector {
	ArrayList<String> file_list;
	ArrayList<String> std_id;
	ArrayList<String> std_last;
	ArrayList<String> std_first;
	
	public filecollector()
	{
		file_list = new ArrayList<String>();
		std_id = new ArrayList<String>();
		std_last = new ArrayList<String>();
		std_first = new ArrayList<String>();
		
	}
	
	public void listFilesForFolder(final File folder) {
		
		
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	           // System.out.println(fileEntry.getName());
	        	String Fname = fileEntry.getName().toString();
	            file_list.add(Fname);
	            String student_id = extract_filename(fileEntry.getName().toString());
	            
	            String sname="";
	            if(getIndex(student_id)>-1)
	            {
	            	int x = getIndex(student_id);
	            	//System.out.println("std_Id:"+student_id + " name:" + std_first.get(x)+" "+std_last.get(x));
	            	sname = std_first.get(x)+" "+std_last.get(x)+" "+extract_number(Fname);
	            }
	            
	            
	            
	            System.out.println(
	            "<div class=\"img\"> <a target=\"_blank\" href=\""+ Fname +"\"><img src=\""+ Fname +"\" alt=\""+ Fname +"\" width=\"110\" height=\"90\"></a><div class=\"desc\">" + sname + "</div></div>");
	            
	            
	        }
	    }
	}

	
	public int getIndex(String itemName)
	{
	    for (int i = 0; i < std_id.size(); i++)
	    {
	        String auction = std_id.get(i);
	        if (itemName.equals(std_id.get(i)))
	        {
	            return i;
	        }
	    } 

	    return -1;
	}
	
	
	public static String extract_number(String address) {
        int firstDotIndex = address.lastIndexOf('.');
        
        return  address.substring(firstDotIndex-1, firstDotIndex);      
        
		
    }
	
	
    public static String extract_filename(String address) {
        int firstDotIndex = address.indexOf('.');
        
        return  address.substring(0, firstDotIndex);      
        
		
    }
	
	public void readcsv(String csvFile)
	{

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] id = line.split(cvsSplitBy);
			
				std_id.add(id[0]);
				std_last.add(id[1]);
				std_first.add(id[2]);
				
				//System.out.println("Country [id= " + id[0]  + " , name=" + id[1]+id[2] + "]");
	
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		//System.out.println("Done");
	  }
	
	
	
	public static void main(String[] args) {
		
		
		final File folder = new File("E:\\Drive F\\UVA TA Stuffs\\assignment3\\artgallery");
		
		filecollector fc = new filecollector();
		
		fc.readcsv("E:\\Drive F\\UVA TA Stuffs\\assignment2\\bulk_download\\Assignment 2_ Ray Tracer\\grades.csv");
	    
		fc.listFilesForFolder(folder);
	    
	   // System.out.println(fc.extract_number("amm4yv.art.submission1.bmp"));
	    
	
		
	}

	

}
