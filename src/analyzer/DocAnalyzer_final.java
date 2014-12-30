/**
 * 
 */
package analyzer;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


//import org.apache.commons.lang.ArrayUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;




import structures.Post;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 */
public class DocAnalyzer_final {
	
	String m_threadURL;
	String m_threadTitle;
	ArrayList<JSONObject> m_threads;
	ArrayList<String> word_list;
	ArrayList<String> smart_system_stop_wordlist;
	String forum_name;
	
	Hashtable<String, Integer> dictionary = new Hashtable<String, Integer>();
	Hashtable<String, Integer> ByGramSet = new Hashtable<String, Integer>();
	
	Hashtable<String, Integer> dictionary_stop_word = new Hashtable<String, Integer>();
	int gram;
	SimpleDateFormat m_dateFormatter;
	
	//to store existing post IDs for checking the replyTo relation
	//HashSet<String> m_existingPostID;
	
	public DocAnalyzer_final(int gram) {
		this.gram = gram;
		//forum_name = name;
		m_threads = new ArrayList<JSONObject>();
		word_list = new ArrayList<String>();
		smart_system_stop_wordlist= new ArrayList<String>();
		m_dateFormatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss Z");//standard date format for this project
	}
	
	//check if students' crawled json files follows instruction
	//if you decide to use the sample code for your homework assignment, this is the place you can perform tokenization/stemming/word counting
	public void AnalyzeThreadedDiscussion(JSONObject json) {		
		
		String title=null;
		try {
			if(json.getString("title")!=null)
			{
				title = json.getString("title");
			}
		
			int no_post_content = 0;
			JSONArray jarray = json.getJSONArray("thread");
			//m_existingPostID = new HashSet<String>(); 
			for(int i=0; i<jarray.length(); i++) {
				
				Post p = new Post(jarray.getJSONObject(i));
				if(p.getContent()==null)
				{
					no_post_content = 1;
					break;
				}
			}
			
			if(no_post_content == 0)
			{
				
				for(int i=0; i<jarray.length(); i++) {
				Post p = new Post(jarray.getJSONObject(i));
			
				
				
					if(this.gram==1)
						TokenizerDemon(title+p.getContent());
					if(this.gram==2)    
						TokenizerDemonforBigram(title+p.getContent());
				
				}
			
	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//store this into memory for later use
		//m_threads.add(json);
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
		System.out.println(folder);
		int size = m_threads.size();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				//System.out.println(f.getAbsolutePath());
				AnalyzeThreadedDiscussion(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		//size = m_threads.size() - size;
		//System.out.println("Loading " + size + " json files from " + folder);
		

		
		
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

	
	public void TokenizerDemon(String text) {
		try {
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			
			//text=stripGarbage(text, 2);
			int inword=1;
			String prevToken="";
			
			for(String str:tokenizer.tokenize(text))
			{
				
				String word = SnowballStemmingDemo(str).toLowerCase().replaceAll("\\W","");
				if(!word.equals(""))
				{
					
					if(inword==1){
						inword=0;
						prevToken=word;
					}
					else{
						String bigram=prevToken+"_"+word;      //ByGramSet
						if(ByGramSet.containsKey(bigram))
						{
							int tempcount= (int)ByGramSet.get(bigram);
							tempcount++;
							ByGramSet.put(bigram, tempcount);
						}
						else{
							ByGramSet.put(bigram, 1);
						}						
						prevToken=word;
					}
					
					if(dictionary.containsKey(word)) {
						Integer val = (Integer) dictionary.get(word);
						dictionary.put(word, val + 1);
						}
					else
						{
							
								dictionary.put(word, 1);
						}
				}
				
			}
			
		
		
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
				
				//String stemmedText = SnowballStemmingDemo(stripGarbage(tokenlist[i]+" "+tokenlist[j]));
			//	checkinWordList(stemmedText);
		
			}
			
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	}
	
	
	
	

    private void generateCsvFile(String sFileName)
    {
 	try
 	{
 		Hashtable<String, Integer> map = dictionary;
 		FileWriter writer = new FileWriter(sFileName+"_"+"new_test"+".csv");
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
		
	
		DocAnalyzer_final analyzer = new DocAnalyzer_final(1);
		
		analyzer.LoadDirectory("./data/json/eHealth/Part2/", ".json");
		//analyzer_unigram.generateCsvFile("./data/json/eHealth/unigram_new_test1");
			
		try{
			/*
			WritableWorkbook workbook = Workbook.createWorkbook(new File("output.xls"));
	        WritableSheet sheet = workbook.createSheet("Wordcount", 0);

			WritableWorkbook workbookb = Workbook.createWorkbook(new File("outputgram.xls"));
	        WritableSheet sheetb = workbookb.createSheet("Wordcount", 0);	
	        */        
			//********Hastable sort*******************************/
	        List<Map.Entry> list = new ArrayList<Map.Entry>(analyzer.dictionary.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry>() {
	            public int compare(Map.Entry e1, Map.Entry e2) {
	                Integer i1 = (Integer) e1.getValue();
	                Integer i2 = (Integer) e2.getValue();
	                return i2.compareTo(i1);
	            }
	        });
	        //ByGramSet
	        List<Map.Entry> listb = new ArrayList<Map.Entry>(analyzer.ByGramSet.entrySet());
	        Collections.sort(listb, new Comparator<Map.Entry>() {
	            public int compare(Map.Entry e1, Map.Entry e2) {
	                Integer i1 = (Integer) e1.getValue();
	                Integer i2 = (Integer) e2.getValue();
	                return i2.compareTo(i1);
	            }
	        });
	        
	        //System.out.println("word - freq");
	        //System.out.println("-------------");
	        
	        FileWriter writer = new FileWriter("./data/json/eHealth/output.csv");
	        writer.append("Unigram"); 
	        writer.append(','); 
	        writer.append("Word Count"); 
	        writer.append('\n');	        
	        
	        
	        int wordcount=1;
	        
	        for(Map.Entry e : list) {
	            //System.out.printf("  %s      %d%n", e.getKey(), e.getValue());
	        	
	        	writer.append(e.getKey()+","+wordcount+","+e.getValue()+"\n");
	        	
	            //Label label = new Label(0, wordcount, (String)e.getKey());
	            //sheet.addCell(label);
	            //label = new Label(1, wordcount, e.getValue().toString());
	            //sheet.addCell(label);
	            wordcount++;
	        }		
	        writer.close();
	        
	        writer = new FileWriter("./data/json/eHealth/outputbi.csv");
	        writer.append("Bigram"); 
	        writer.append(','); 
	        writer.append("Word Count"); 
	        writer.append('\n');
	        
	        //workbook.write();
	        //workbook.close();
	        
	        int wordcountb=1;
	        for(Map.Entry e : listb) {
	        	
	        	writer.append(e.getKey()+","+wordcountb+","+e.getValue()+"\n");
	            //System.out.printf("  %s      %d%n", e.getKey(), e.getValue());
	            //Label label = new Label(0, wordcountb, (String)e.getKey());
	            //sheetb.addCell(label);
	            //label = new Label(1, wordcountb, e.getValue().toString());
	            //sheetb.addCell(label);
	            wordcountb++;
	        }		
	        writer.close();

	        
	        //workbookb.write();
	        //workbookb.close();
	        
		}catch(Exception e)
		{
			System.out.println(e);
		}	
		
	}

	
	

}
