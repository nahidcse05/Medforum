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

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

//import org.apache.commons.lang.ArrayUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;



import structures.Post;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 */
public class DocAnalyzer {
	
	String m_threadURL;
	String m_threadTitle;
	ArrayList<JSONObject> m_threads;
	ArrayList<String> word_list;
	ArrayList<String> smart_system_stop_wordlist;
	String forum_name;
	//Map dictionary = new HashMap<>();
	//LinkedHashMap dictionary = new LinkedHashMap();
	Map<String, Integer> dictionary = new HashMap<String, Integer>();
	
	Map<String, Integer> dictionary_stop_word = new HashMap<String, Integer>();
	int gram;
	SimpleDateFormat m_dateFormatter;
	
	//to store existing post IDs for checking the replyTo relation
	HashSet<String> m_existingPostID;
	
	public DocAnalyzer(int gram, String name) {
		this.gram = gram;
		forum_name = name;
		m_threads = new ArrayList<JSONObject>();
		word_list = new ArrayList<String>();
		smart_system_stop_wordlist= new ArrayList<String>();
		m_dateFormatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss Z");//standard date format for this project
	}
	
	//check if students' crawled json files follows instruction
	//if you decide to use the sample code for your homework assignment, this is the place you can perform tokenization/stemming/word counting
	public void AnalyzeThreadedDiscussion(JSONObject json) {		
		try {
			json.getString("title");
			json.getString("URL");
			
			JSONArray jarray = json.getJSONArray("thread");
			m_existingPostID = new HashSet<String>(); 
			for(int i=0; i<jarray.length(); i++) {
				
				Post p = new Post(jarray.getJSONObject(i));
				int check = checkPostFormat(p);
				if(check == 0) // 0 means no error in the post
				{
					if(this.gram==1)
						TokenizerDemon(p.getTitle()+p.getContent().replaceAll("\\s+", " ").trim());
					if(this.gram==2)    
						TokenizerDemonforBigram(p.getTitle()+p.getContent().replaceAll("\\s+", " ").trim());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//store this into memory for later use
		m_threads.add(json);
	}
	
	//check format for each post
	private int checkPostFormat(Post p) {
		if (p.getID() == null)
			{
				System.err.println("[Error]Missing postID!");
				return 1;
			}
		/*else if(p.getTitle() == null)
		{
			System.err.println("[Error]Missing postTitle!");
			return 1;
		
		}*/
		else if (p.getAuthor() == null)
		{
			System.err.format("[Error]Missing author name in %s!\n", p.getID());
			return 1;
		}
		else if (p.getAuthorID() == null)
		{
			System.err.format("[Error]Missing author ID in %s!\n", p.getID());
			return 1;
		}
		else if (p.getDate() == null)
		{
			System.err.format("[Error]Missing post date in %s!\n", p.getID());
			return 1;
		}
		else if (p.getContent() == null)
		{
			System.err.format("[Error]Missing post content in %s!\n", p.getID());
			return 1;
		}
		else {
			//hard to check!!!
			//register the post ID
//			m_existingPostID.add(p.getID());
//			m_existingPostID.add(p.getAuthorID());//might also be pointing to authors
//			m_existingPostID.add(p.getAuthor());//might also be pointing to authors
//			
//			if (p.getReplyToID()!=null 
//					&& !m_existingPostID.contains(p.getReplyToID()) ) {
//				System.err.format("[Error]Incorrect replyTO post ID '%s' in %s!\n", p.getReplyToID(), p.getID());
//			}
			
			//to check if the date format is correct
			try {
				m_dateFormatter.parse(p.getDate());
			} catch (ParseException e) {
				System.err.format("[Error]Wrong date format '%s' in %s\n!", p.getDate(), p.getID());
			}
			return 0;
		}
		
	}
	
	//sample code for loading the json file
	public JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	public void LoadDirectory(String folder, String suffix) {
		File dir = new File(folder);
		int size = m_threads.size();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				System.out.println(f.getAbsolutePath());
				AnalyzeThreadedDiscussion(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		size = m_threads.size() - size;
		System.out.println("Loading " + size + " json files from " + folder);
		
		/*System.out.println("\nUnSorted Map......");
		printWordList(dictionary);*/
		System.out.println("\nSorted Map......");
		//Map<String, Integer> sorteddictionary = sortByComparator(dictionary);
		//printWordList(sorteddictionary);
		
		//remove_stop_word(sorteddictionary);
		if(this.gram == 1)
		generateCsvFile("./data/json/eHealth/unigram", dictionary);
		
		if(this.gram == 2)
			generateCsvFile("./data/json/eHealth/Bigram", dictionary);
		
		
	}

	//sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
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
				String[] word = line.split(cvsSplitBy);
				smart_system_stop_wordlist.add(word[0]);
				
				System.out.println(word[0]);
	 
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
	
	
	public String normalizeText(String str)
	{
		ArrayList<String> badChar = new ArrayList<String>();
	//	badChar= ["@", "~","!"]; 
		String [] puncmark = {"?","’","…","”","“","%","$","#","<",">","—","¦","~","=","’","=:","*",",",".","'","(",")","{","}",":",";","-","_","@","...","?","/","!","+","\"","`"};
		
		String resultStr = str;
		for (String s : puncmark) {
		   resultStr = resultStr.replaceAll("[\\^"+s+"]","");
		}
		
		resultStr = resultStr.replaceAll("[0-9]","");
		resultStr = resultStr.replaceAll("[\u0000-\u001f]", "");
		
		//resultStr = resultStr.replaceAll("[^\\p{L}\\p{Nd}]+", "");
		
		return resultStr.toLowerCase();
	}
	
	 public String stripGarbage(String s) {  
		    
		 String good="";
		 	if(this.gram==1)
		 		good ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		 	if(this.gram==2)
		 		good =" abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		 	
		 	s = s.replaceAll("\\s+", " ").trim(); // trimming all extra space
		    String result = "";
		    for ( int i = 0; i < s.length(); i++ ) {
		        if ( good.indexOf(s.charAt(i)) >= 0 )
		           result += s.charAt(i);
		        }
		    return result.toLowerCase();
		    }
	
	public void checkinWordList(String word)
	{
		/*int contain = 0;
		for(int i=0; i<word_list.size();i++)
		{
			if(word_list.get(i).equalsIgnoreCase(word))
			{
				contain = 1;
				System.out.println("YES:"+word);
				break;
			}
				
		}
		if(contain == 0)
			word_list.add(word);
		*/
		if(dictionary.containsKey(word)) {
			Integer val = (Integer) dictionary.get(word);
			dictionary.put(word, val + 1);
			}
		else
			{
				if(word!="" || word!=null)
					dictionary.put(word, 1);
			}
		
	}
	
	public void printWordList(Map<String, Integer> map)
	{
		/*for(int i=0;i<word_list.size();i++)
		{
			System.out.println(word_list.get(i));
		}
		System.out.println("SIZE:"+word_list.size());*/
		/* for(String key:dictionary.keySet())
			 System.out.println(key + ": " + dictionary.get(key));
		 */
		 
		//sortTest();
		
		
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() 
                                      + " : " + entry.getValue());
		}	
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
	
	public void TokenizerDemon(String text) {
		try {
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			
			//text=stripGarbage(text, 2);
			
			String [] tokenlist = tokenizer.tokenize(text);
			for(int i=0;i<tokenlist.length;i++)
			{
				String stemmedText = SnowballStemmingDemo(stripGarbage(tokenlist[i]));
				checkinWordList(stemmedText);
				//System.out.println(stemmedText);
			}
			
		
		/*	System.out.format("Token\tSnonball Stemmer\tPorter Stemmer\n");
			for(String token:tokenizer.tokenize(text)){
				System.out.format("%s\t%s\t%s\n", token, SnowballStemmingDemo(token), PorterStemmingDemo(token));
			}
			*/
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	}
	
	
	
	public void TokenizerDemonforBigram(String text) {
		try {
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			
			
			
			String [] tokenlist_temp = tokenizer.tokenize(text);
			String [] tokenlist=new String[tokenlist_temp.length];
			
			int k=0;
			for(int i=0;i<tokenlist_temp.length;i++)
			{
				if(tokenlist_temp[i]==" "|| tokenlist_temp[i]==null)
					continue;
				tokenlist[k]=tokenlist_temp[i];
				k++;
			}
			
			for(int i=0,j=1;i<k-1;i++,j++)
			{
				
				String stemmedText = SnowballStemmingDemo(stripGarbage(tokenlist[i]+" "+tokenlist[j]));
				checkinWordList(stemmedText);
				//System.out.println(stemmedText);
			}
			
			
			/*ArrayList<String> tokenlist = ngrams(2,text);
			
			for(int i=0;i<tokenlist.size();i++)
			{
				String stemmedText = SnowballStemmingDemo(stripGarbage(tokenlist.get(i),2));
				checkinWordList(stemmedText);
				//System.out.println(stemmedText);
			}
			*/
		
		/*	System.out.format("Token\tSnonball Stemmer\tPorter Stemmer\n");
			for(String token:tokenizer.tokenize(text)){
				System.out.format("%s\t%s\t%s\n", token, SnowballStemmingDemo(token), PorterStemmingDemo(token));
			}
			*/
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	}
	
	
	
	public ArrayList<String> ngrams(int n, String str) throws InvalidFormatException, FileNotFoundException, IOException {
        ArrayList<String> ngrams = new ArrayList<String>();
        //String[] words = str.split(" ");
        
        //str=stripGarbage(str, 2);
        Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
        String[] words1 =  tokenizer.tokenize(str);
        
        String temp="";
       
        for (int i = 0; i < words1.length; i++)
        {
            //temp.concat(words1[i]+" ");
        	temp = temp+words1[i]+" ";
        	//System.out.println("i:"+i+" "+words1[i]);
        }
        
        str=stripGarbage(temp);
        
        String []words;
        words = tokenizer.tokenize(str);
        if(words[0].length()>4)
        words[0]= words[0].substring(4);
        for (int i = 0; i < words.length; i++)
        {
            //temp = temp+words1[i]+" ";
        	System.out.println("i:"+i+" "+words[i]);
        }
        
        
        
        for (int i = 0; i < words.length - n + 1; i++)
            ngrams.add(concat(words, i, i+n));
        return ngrams;
    }

    public String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }
    
    
    private void remove_stop_word(Map<String, Integer> map)
    {
    	
    	Map<String, Integer> final_map = null;
    	for (String str:smart_system_stop_wordlist)
    	{
    		for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
    			
    			if(str.equalsIgnoreCase(entry.getKey()))
    					{
    						continue;
    					}
    			final_map.put(entry.getKey(), entry.getValue());
 	 		   //writer.append(entry.getKey()+","+entry.getValue()+"\n");
 	 
 			}	    
 	
    	}
    	
    	String sFileName="";
    	 if(this.gram == 1)
  			sFileName="./data/json/eHealth/unigram.csv";
    	 if(this.gram == 2)
   			sFileName="./data/json/eHealth/Bigram.csv";
   		
  		
    	
    	generateCsvFile(sFileName, final_map);
    }
    

	
    private void generateFrequency_for_smart_word_list()
    {
    	for (String str:smart_system_stop_wordlist)
    	{
    		for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
    			
    			if(str.equalsIgnoreCase(entry.getKey()))
    					{
    						dictionary_stop_word.put(entry.getKey(), entry.getValue());
    					}
 	 		   //writer.append(entry.getKey()+","+entry.getValue()+"\n");
 	 
 			}	    
 	
    	}
    	
    	Map<String, Integer> sortedMap = sortByComparator(dictionary_stop_word);
    	generateCsvFile("./data/json/eHealth/smart_word_list_top_stop_word", sortedMap);
    }
    
    private void generateCsvFile(String sFileName,Map<String, Integer> map)
    {
 	try
 	{
 		
 		FileWriter writer = new FileWriter(sFileName+"_"+forum_name+".csv");
 	    //FileWriter writer_log = new FileWriter(sFileName+"log.csv");
  
 	    if(this.gram == 1)
 	    	writer.append("Unigram");
 	    if(this.gram == 2)
 	    	writer.append("Bigram");
	 	    writer.append(',');
	 	    writer.append("Word Count");
	 	    writer.append('\n');
	  
	 	   int x= 1;
	 	   for (Map.Entry<String, Integer> entry : map.entrySet()) {
			
	 		   writer.append(entry.getKey()+","+entry.getValue()+"\n");
	 		   //writer_log.append(Math.log(x)+","+Math.log(entry.getValue())+"\n");
	 		   //x++;
	 
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
	
	
	public static void main(String[] args) {
		
		
		//codes for demonstrating tokenization and stemming
		//analyzer.TokenizerDemon("I've practiced for 30 years in pediatrics, and I've never seen anything quite like this.");
		
		
	/*	ArrayList<String> bigrams;
		try {
			bigrams = analyzer.ngrams(2, "This is my car");
			 for (String ngram : bigrams)
	             System.out.println(ngram);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		 
		/*
		String text = "I've? *pr~ac1235’t¦iced` \"for 30 /years in 5=:pedia[trics, and I've “never” seen anything quite like this.";
		
		String test = "  I     have  been.    I love   ";
		System.out.println(test.replaceAll("\\s+", " ").trim());
		
        */

		/*char ch=']';

		System.out.println((int)ch);

		System.out.println(analyzer.stripGarbage(text));
		
		System.out.println(analyzer.normalizeText(text));
		*/
		
		
		
	/*	String remove= text.replaceAll("[\\^.]", " ");
		remove= remove.replaceAll("[\\^,]", " ");
		remove= remove.replaceAll("[\\^']", " ");
		
		analyzer.TokenizerDemon(remove);
		
		System.out.println(remove);
		*/
		//codes for loading json file
		//analyzer.AnalyzeThreadedDiscussion(analyzer.LoadJson("./data/json/MedHelp/Anxiety/sample.json"));
		
		//when we want to execute it in command line/
	
		//analyzer.readcsv("./data/json/eHealth/stopword_list.csv");
		//analyzer.LoadDirectory("./data/json/eHealth/Part2/", ".json");
		
		
		String [] forum_name = new String [4];
		
		forum_name[2]="eHealth";
		forum_name[3]="healthboards";
		forum_name[0]="MedHelp";
		forum_name[1]="WebMD";
		
		for(int i=3;i<4;i++)
        {
       
            String filename = "./data/json/eHealth/Part2/"+forum_name[i]+"/";
            DocAnalyzer analyzer = new DocAnalyzer(1,forum_name[i]);

    		analyzer.LoadDirectory(filename, ".json");
    		
        }
		
		//analyzer.LoadJson("D:/nahid/Medforum/data/json/eHealth/Part2/eHealth/Schizophrenia/ka5am-T142.json");
		
		//analyzer.generateFrequency_for_smart_word_list();
		
		
		//DocAnalyzer analyzer_bigram = new DocAnalyzer(2);
		//analyzer_bigram.LoadDirectory("./data/json/eHealth/Panic_Attack/", ".json");
		
	}

	
	

}
