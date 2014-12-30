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




public class JobStopward {
	
	Map<String, Integer> dictionary = new HashMap<String, Integer>();
	Map<String, Integer> sortedMap;
	ArrayList<String> smart_system_stop_wordlist;
	
	
	 public String stripGarbage(String s) {  
		    
		 String good="";
		 		good ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		 
		 	
		 	s = s.replaceAll("\\s+", " ").trim(); // trimming all extra space
		    String result = "";
		    for ( int i = 0; i < s.length(); i++ ) {
		        if ( good.indexOf(s.charAt(i)) >= 0 )
		           result += s.charAt(i);
		        }
		    return result.toLowerCase();
		    }
	
		public String SnowballStemmingDemo(String token) {
			SnowballStemmer stemmer = new englishStemmer();
			stemmer.setCurrent(token);
			if (stemmer.stem())
				return stemmer.getCurrent();
			else
				return token;
		}
		 
	
		
		public void readcsv1(String csvFile)
		{
			
			
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";
		 
			try {
		 
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
		 
				        // use comma as separator
					String[] word = line.split(cvsSplitBy);
					
					dictionary.put(word[0], Integer.parseInt(word[1]));
					//System.out.println(word[0]+":"+word[1]);
					
		 
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
		 
			System.out.println("Done"+dictionary.size());
		  
		}
		
	public void readcsv(String csvFile)
	{
		
		smart_system_stop_wordlist= new ArrayList<String>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try {
			FileWriter writer = new FileWriter(csvFile+"new.csv");
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] word = line.split(cvsSplitBy);
				String temp= SnowballStemmingDemo(stripGarbage(word[0]));
				writer.append(word[0]+","+word[1]+"\n");
				smart_system_stop_wordlist.add(temp);
				//System.out.println(word[0]+":"+temp);
				
			}

	 	    writer.flush();
	 	   // writer_log.flush();
	 	    writer.close();
	 	   // writer_log.close();
	 	    
	 
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
	 
		System.out.println("Done"+smart_system_stop_wordlist.size());
	  
	}
	
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {
		 
		// Convert Map to List
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	
	  private void suggested_stop_word(String fName)
	    {
	    	
	    	Map<String, Integer> final_map = new HashMap<String, Integer>() ;
	    	
	    	int top_k = smart_system_stop_wordlist.size()+50;
	    	int i=1;
	    	
	    	

	    
	    	for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
    		
	    		if(i==top_k-1)
	    			break;
	    		
	    	String tmp = entry.getKey();
	    	//System.out.println(tmp);
	    	if(tmp.length()>=4 && tmp.substring(0, 4).equalsIgnoreCase("null"))
    		{
    			//System.out.println("NULL::"+tmp);
    			tmp = tmp.substring(4);
    		}
	    	
	    	if(tmp.length()==1 || tmp.length()>=15)
	    		continue;
	    	int flag = 0;
	    	
	    	for (String str:smart_system_stop_wordlist)
	    	{
	    		
	    		
	    			if(str.equalsIgnoreCase(tmp))
	    					{
	    				System.out.print("EQUAL");	
	    				flag = 1;
	    				break;
	    				
	    					}
	    			
	 	 		   //writer.append(entry.getKey()+","+entry.getValue()+"\n");
	 	 
	 			}
	    	if(flag==0)
	    		final_map.put(tmp, entry.getValue());
	    	if(flag==1)
	    		flag = 0;
	    	i++;
	 	
	    	}
	    	

	  		
	    	Map<String, Integer> sortMap = sortByComparator(final_map);
	    	generateCsvFile1(fName, sortMap);
	    }
	
	  
		
		private void generateCsvFile1(String sFileName, Map<String, Integer> map)
	    {
	 	try
	 	{
	 		
	 		
	 		FileWriter writer = new FileWriter(sFileName+".csv");
	 	
		 	   int x= 1;
		 	   for (Map.Entry<String, Integer> entry : map.entrySet()) {
				
		 		 
		 		   writer.append(entry.getKey()+", "+entry.getValue()+"\n");
		 		   x++;
		 
				}	    
		 	    
	 	  
	 	
	  
	 	    writer.flush();
	 	   // writer_log.flush();
	 	    writer.close();
	 	   // writer_log.close();
	 	    
	 	}
	 	catch(IOException e)
	 	{
	 	     e.printStackTrace();
	 	} 
	     }  
	
	private void generateCsvFile(String sFileName)
    {
 	try
 	{
 		
 		
 		FileWriter writer = new FileWriter(sFileName+".csv");
 	
	 	   int x= 1;
	 	   for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			
	 		  writer.append(x+","+entry.getValue()+"\n");
	 		   x++;
	 
			}	    
	 	    
 	  
 	    //generate whatever data you want
  
 	    writer.flush();
 	   // writer_log.flush();
 	    writer.close();
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
		
		
	
		
		JobStopward com = new JobStopward();
	
		com.readcsv("D:\\nahid\\googlecrawler\\data\\smart_word_list_top_stop_word.csv");
		com.readcsv1("D:\\nahid\\googlecrawler\\data\\json\\google\\unigram_job.csv");
		com.suggested_stop_word("D:\\nahid\\googlecrawler\\data\\json\\google\\stopword_job");
		

		
	}

	

}
