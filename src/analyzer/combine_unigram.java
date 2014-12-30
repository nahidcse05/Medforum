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




public class combine_unigram {
	
	Map<String, Integer> dictionary = new HashMap<String, Integer>();
	Map<String, Integer> sortedMap;
	
	public void readcsv(String csvFile)
	{
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] word = line.split(cvsSplitBy);
	
				
				System.out.println(word[0]);
				if(dictionary.containsKey(word[0])) {
					Integer val = (Integer) dictionary.get(word[0]);
					dictionary.put(word[0], val + 1);
					}
				else
					{
						if(word[0]!="" || word[0]!=null)
							dictionary.put(word[0], Integer.parseInt(word[1]));
					}
	 
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
	 
		//System.out.println("Done"+smart_system_stop_wordlist.size());
	  
	}
	
	private void generateCsvFile(String sFileName)
    {
 	try
 	{
 		
 		
 		FileWriter writer = new FileWriter(sFileName+".csv"); // for Matlab
 		FileWriter writer1 = new FileWriter(sFileName+"_stopword.csv");// for StopWord Removal
 	    
 	    //FileWriter writer_log = new FileWriter(sFileName+"log.csv");
  
 	   /* if(this.gram == 1)
 	    	writer.append("Unigram");
 	    if(this.gram == 2)
 	    	writer.append("Bigram");
	 	    writer.append(',');
	 	    writer.append("Word Count");
	 	    writer.append('\n');
	  */
	 	   int x= 1;
	 	   for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			
	 		  writer.append(x+","+entry.getValue()+"\n");
	 		   
	 		  writer1.append(entry.getKey()+","+entry.getValue()+"\n");
	 		   //writer_log.append(Math.log(x)+","+Math.log(entry.getValue())+"\n");
	 		   x++;
	 
			}	    
	 	    
 	  
 	    //generate whatever data you want
  
 	   writer.flush();
 	   writer1.flush();
 	   // writer_log.flush();
 	   writer.close();
 	   writer1.close();
 	   // writer_log.close();
 	    
 	}
 	catch(IOException e)
 	{
 	     e.printStackTrace();
 	} 
     }
	
	public void sortByComparator() {
		 
		// Convert Map to List
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(dictionary.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
	
	}

	
	public static void main(String[] args) {
		
		
		
		String [] forum_name = new String [4];
		
		forum_name[2]="unigram_eHealth";
		forum_name[3]="unigram_healthboards";
		forum_name[0]="unigram_MedHelp";
		forum_name[1]="unigram_WebMD";
		
		combine_unigram com = new combine_unigram();
		for(int i=0;i<4;i++)
        {
       
            String filename = "D:/nahid/Medforum/data/json/eHealth/summarized/"+forum_name[i]+".csv";
            
            com.readcsv(filename);
            
        }
		
		com.sortByComparator();
		com.generateCsvFile("D:/nahid/Medforum/data/json/eHealth/summarized/combine");
		
		
		
		//analyzer.LoadJson("D:/nahid/Medforum/data/json/eHealth/Part2/eHealth/Schizophrenia/ka5am-T142.json");
		
		//analyzer.generateFrequency_for_smart_word_list();
		
		
		//DocAnalyzer analyzer_bigram = new DocAnalyzer(2);
		//analyzer_bigram.LoadDirectory("./data/json/eHealth/Panic_Attack/", ".json");
		
	}

	

}
