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
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;



//import org.apache.commons.lang.ArrayUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;







import structures.Post;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 */
public class JobAnalyzer {
	
	String m_threadURL;
	String m_threadTitle;
	ArrayList<JSONObject> m_threads;
	ArrayList<String> word_list;
	ArrayList<String> smart_system_stop_wordlist;
	String forum_name;
	
	Hashtable<String, Integer> dictionary = new Hashtable<String, Integer>();
	Hashtable<String, Integer> noun_dictionary = new Hashtable<String, Integer>();
	static ArrayList<String> experience_year = new ArrayList<String>();
	
	Hashtable<String, Integer> dictionary_stop_word = new Hashtable<String, Integer>();
	int gram;
	SimpleDateFormat m_dateFormatter;
	
	//to store existing post IDs for checking the replyTo relation
	//HashSet<String> m_existingPostID;
	
	public JobAnalyzer(int gram) {
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
			
			
			String contents = json.getString("title")+" "+json.getString("responsibilities")+" "+ json.getString("minQual")+" "+json.getString("prefQual")+" "+ json.getString("description") +" "+json.getString("location"); // getting the Title
            String qual = json.getString("minQual")+" "+json.getString("prefQual");
					if(this.gram==1)
						TokenizerDemon(contents,qual);
					if(this.gram==2)    
						TokenizerDemonforBigram(contents);
				
		}
			
	
		 catch (JSONException e) {
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
	
	public static ArrayList<String> POSTag(String input) throws IOException{
		
		
		
		String str = input;
		Pattern p = Pattern.compile("[A-Z0-9](?i)[^.?!]*?\\b(experience|experienced)\\b[^.?!]*[.?!]");
		Matcher m = p.matcher(str);

		ArrayList<String> matched=new ArrayList<String>();
		ArrayList<String> noun = new ArrayList<String>();
		
		
		int matched_inde = 0;
		
		while (m.find()) {
			matched.add(m.group());
			System.out.println(matched.get(matched_inde));
		
			POSModel model = new POSModelLoader()	
			.load(new File("./data/Model/en-pos-maxent.bin"));
			PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
			POSTaggerME tagger = new POSTaggerME(model);
		
	 
		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(matched.get(matched_inde) ));
		matched_inde++;
		perfMon.start();
		String line;
		
		
		
		while ((line = lineStream.read()) != null) {
	 
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			String tokenized_line[] = tokenizer.tokenize(line);
			
			
			String[] tags = tagger.tag(tokenized_line);
	 
			POSSample sample = new POSSample(tokenized_line, tags);
			
			//System.out.println(sample.toString());
			
			
			
			int preposition_index = 0;
			
			
			for(int i=0;i<sample.getTags().length;i++)
			{
				
				/*if(sample.getTags()[i].equalsIgnoreCase("NNP") || sample.getTags()[i].equalsIgnoreCase("NN"))
				{
					//System.out.println(sample.getSentence()[i]+": "+sample.getTags()[i]);
					noun.add(sample.getSentence()[i]);
				}*/
				if(sample.getTags()[i].equalsIgnoreCase("CD"))
				{
					int limit = i+5;
					String str1="";
					for(int j=i; j<limit;j++)
					{
						str1=str1+sample.getSentence()[j]+" ";
						
					}
					experience_year.add(str1);
				}
				
				if(sample.getTags()[i].equalsIgnoreCase("IN") && preposition_index==0)
				{
					preposition_index=i;
				}
				
			}
			
			String aspects="";
		
			for(int i=preposition_index + 1; i<sample.getTags().length;i++)
			{
				
				if(sample.getSentence()[i].contains("experience"))
					continue;
				
				if(sample.getTags()[i].contains(".")==false && sample.getTags()[i].contains(",")==false)
				{
					
					if(sample.getTags()[i].equalsIgnoreCase("NN") || sample.getTags()[i].equalsIgnoreCase("NNP"))
						aspects = aspects + sample.getSentence()[i] + " ";
					else
					{
						if(aspects!="")
						{
							System.out.println(aspects);
							noun.add(aspects);
						}
						
							
						aspects="";
					}
				}
				else
				{
					System.out.println(aspects);
					if(aspects!="")
					{
						System.out.println(aspects);
						noun.add(aspects);
					}
					aspects="";
				}
				
					
			}
	 
			perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();
		}
		return noun;
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

	
	public boolean isAllUpperChar(String valitateStr) {
	    for (int i = valitateStr.length() - 1; i >= 0; i--) {
	        if (!Character.isUpperCase(valitateStr.charAt(i))) {
	            return false;
	        }
	    }
	    return true;
	}
	
	public void TokenizerDemon(String text, String qual) {
		try {
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			
			ArrayList<String> noun = POSTag(qual);
			
			for(int i=0;i<noun.size();i++)
			{
				String word = noun.get(i);
				if(!word.equals(""))
				{
					
					if(noun_dictionary.containsKey(word)) {
						Integer val = (Integer) noun_dictionary.get(word);
						noun_dictionary.put(word, val + 1);
						}
					else
						{
							
								noun_dictionary.put(word, 1);
						}
				}
				
			}
			
			//text=stripGarbage(text, 2);
			
			
			
			for(String str:tokenizer.tokenize(text))
			{
				
				String word = SnowballStemmingDemo(str).toLowerCase().replaceAll("\\W","");
				if(!word.equals(""))
				{
					
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
	
	
    private void generateNounCsvFile(String sFileName)
    {
	 	try
	 	{
	 		Hashtable<String, Integer> map = noun_dictionary;
	 		FileWriter writer = new FileWriter(sFileName+".csv");
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
	
    private void generateExperienceCsvFile(String sFileName)
    {
	 	try
	 	{
	 		
	 		FileWriter writer = new FileWriter(sFileName+".csv");
	 	    //FileWriter writer_log = new FileWriter(sFileName+"log.csv");
	  
	 	    if(this.gram == 1)
	 	    	writer.append("Unigram");
	 	    if(this.gram == 2)
	 	    	writer.append("Bigram");
		 	    writer.append(',');
		 	    writer.append("Word Count");
		 	    writer.append('\n');
		  
		 	  
		 	   for (int i=0;i<experience_year.size();i++) {
				
		 		   writer.append(experience_year.get(i)+"\n");
		 		   
		 
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

    private void generateCsvFile(String sFileName)
    {
	 	try
	 	{
	 		Hashtable<String, Integer> map = dictionary;
	 		FileWriter writer = new FileWriter(sFileName+".csv");
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
		
	
		JobAnalyzer analyzer_unigram = new JobAnalyzer(1);
		analyzer_unigram.LoadDirectory("D:\\nahid\\googlecrawler\\data\\json\\google1\\", ".json");
		analyzer_unigram.generateCsvFile("D:\\nahid\\googlecrawler\\data\\json\\google1\\unigram_job");
		analyzer_unigram.generateNounCsvFile("D:\\nahid\\googlecrawler\\data\\json\\google1\\noun_job");
		analyzer_unigram.generateExperienceCsvFile("D:\\nahid\\googlecrawler\\data\\json\\google1\\experience_job");
			
			
		
	}

	
	

}
