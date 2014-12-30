/**
 * 
 */
package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;


import java.io.FileWriter;




import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;




import opennlp.tools.chunker.*;
import opennlp.tools.cmdline.*;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.coref.*;
import opennlp.tools.dictionary.*;
import opennlp.tools.doccat.*;
import opennlp.tools.formats.*;
import opennlp.tools.namefind.*;
import opennlp.tools.ngram.*;
import opennlp.tools.parser.*;
import opennlp.tools.postag.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.stemmer.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;
import opennlp.maxent.*;
import opennlp.model.*;
import opennlp.perceptron.*;
import structures.Post;

import java.util.*;

/*
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
*/





/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 */
public class DocAnalyzer {
	
	String m_threadURL;
	String m_threadTitle;
	ArrayList<JSONObject> m_threads;
	
	Hashtable<String, Integer> WordSet = new Hashtable<String, Integer>();
	Hashtable<String, Integer> ByGramSet = new Hashtable<String, Integer>();
	
	SimpleDateFormat m_dateFormatter;
	
	//to store existing post IDs for checking the replyTo relation
	HashSet<String> m_existingPostID;
	
	public DocAnalyzer() {
		m_threads = new ArrayList<JSONObject>();
		m_dateFormatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss Z");//standard date format for this project
	}
	
	


public static void POSTag() throws IOException{
	POSModel model = new POSModelLoader()	
		.load(new File("./data/Model/en-pos-maxent.bin"));
	PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
	POSTaggerME tagger = new POSTaggerME(model);
 
	String input1 = "Hi. How are you? This is Mike.";
	String input = "5 years Knowledge of UI frameworks (either Android, iOS, XML), MVP application design and complex, reactive touch based UI.";
	ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));
 
	perfMon.start();
	String line;
	while ((line = lineStream.read()) != null) {
 
		Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
		String tokenized_line[] = tokenizer.tokenize(line);
		
		String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
				.tokenize(line);
		String[] tags = tagger.tag(tokenized_line);
 
		POSSample sample = new POSSample(tokenized_line, tags);
		
		System.out.println(sample.toString());
		//System.out.println(sample.getTags()[0]);
		for(int i=0;i<sample.getTags().length;i++)
		{
			if(sample.getTags()[i].equalsIgnoreCase("NNP") || sample.getTags()[i].equalsIgnoreCase("NN"))
			System.out.println(sample.getSentence()[i]+": "+sample.getTags()[i]);
		}
 
		perfMon.incrementCounter();
	}
	perfMon.stopAndPrintFinalResult();
	return;
}
	
	//check if students' crawled json files follows instruction
	//if you decide to use the sample code for your homework assignment, this is the place you can perform tokenization/stemming/word counting
	public void AnalyzeThreadedDiscussion(JSONObject json) {
		String title=null;
		try {
			if (json.getString("title") != null){
				title=json.getString("title");
			}
			//json.getString("URL");
			
			
			JSONArray jarray = json.getJSONArray("thread");
			//m_existingPostID = new HashSet<String>(); 
			int flagcontent=1;
			for(int i=0; i<jarray.length(); i++) {
				Post temp_post=new Post(jarray.getJSONObject(i));
				//checkPostFormat(temp_post);
				if(temp_post.getContent()==null){
					flagcontent=0;
				}
			}
			if(flagcontent==1){
				TokenizerDemon(title);
				for(int i=0; i<jarray.length(); i++) {
					Post temp_post=new Post(jarray.getJSONObject(i));
					
					TokenizerDemon(temp_post.getContent());
					
				}	
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//store this into memory for later use
		//m_threads.add(json);
	}
	
	//check format for each post
	private void checkPostFormat(Post p) {
		if (p.getID() == null)
			System.err.println("[Error]Missing postID!");
		else if (p.getAuthor() == null)
			System.err.format("[Error]Missing author name in %s!\n", p.getID());
		else if (p.getAuthorID() == null)
			System.err.format("[Error]Missing author ID in %s!\n", p.getID());
		else if (p.getDate() == null)
			System.err.format("[Error]Missing post date in %s!\n", p.getID());
		else if (p.getContent() == null)
			System.err.format("[Error]Missing post content in %s!\n", p.getID());
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
			String tempsuffix=f.getName().toLowerCase();
			//System.out.print(folder+"  :  ");
			//System.out.println(tempsuffix);
			if (f.isFile() && tempsuffix.endsWith(suffix)){
				AnalyzeThreadedDiscussion(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				//System.out.println(f.getAbsolutePath());
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		size = m_threads.size() - size;
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
	
	//sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	public void TokenizerDemon(String text) {
		try {
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			
			//System.out.format("Token\tSnonball Stemmer\tPorter Stemmer\n");
			int inword=1;
			String prevToken="";
			text=text.toLowerCase();
			
			for(String token:tokenizer.tokenize(text)){
				//token=token.toLowerCase();
				String strippedInput = SnowballStemmingDemo(token).toLowerCase().replaceAll("\\W", "");
				if(!strippedInput.equals("")){
					
					if(inword==1){
						inword=0;
						prevToken=strippedInput;
					}
					else{
						String bigram=prevToken+"_"+strippedInput;      //ByGramSet
						if(ByGramSet.containsKey(bigram))
						{
							int tempcount= (int)ByGramSet.get(bigram);
							tempcount++;
							ByGramSet.put(bigram, tempcount);
						}
						else{
							ByGramSet.put(bigram, 1);
						}						
						prevToken=strippedInput;
					}
					
					//.......................................................
					if(WordSet.containsKey(strippedInput))
					{
						int tempcount= (int)WordSet.get(strippedInput);
						tempcount++;
						WordSet.put(strippedInput, tempcount);
					}
					else{
						WordSet.put(strippedInput, 1);
					}
				}
				//System.out.format("%s\t%s\t%s\n", token, SnowballStemmingDemo(token), PorterStemmingDemo(token));
			}
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DocAnalyzer analyzer = new DocAnalyzer();
		
		//codes for demonstrating tokenization and stemming
		//analyzer.TokenizerDemon("I've practiced for [30] years %20 << in pediatrics, \"cat' and I've never Seen Anything quite like this.");
		
		//codes for loading json file
		//System.out.println(analyzer.m_threads.size());
		//analyzer.AnalyzeThreadedDiscussion(analyzer.LoadJson("./data/json/MedHelp/Anxiety/sample.json"));
		//System.out.println(analyzer.m_threads.size());
		
		//when we want to execute it in command line
		
		try {
			analyzer.POSTag();
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		//analyzer.LoadDirectory("./data/json/eHealth/Part2/eHealth/Asthma/", ".json");
		
		/*try{
			/*
			WritableWorkbook workbook = Workbook.createWorkbook(new File("output.xls"));
	        WritableSheet sheet = workbook.createSheet("Wordcount", 0);

			WritableWorkbook workbookb = Workbook.createWorkbook(new File("outputgram.xls"));
	        WritableSheet sheetb = workbookb.createSheet("Wordcount", 0);	
	        */        
			//********Hastable sort*******************************/
	     /*   List<Map.Entry> list = new ArrayList<Map.Entry>(analyzer.WordSet.entrySet());
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
	        
	        FileWriter writer = new FileWriter("output.csv");
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
	        
	        writer = new FileWriter("outputbi.csv");
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
		
		*/
		
	}

}
