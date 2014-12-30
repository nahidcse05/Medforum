/**
 * 
 */
package analyzer;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import java.util.Set;
import java.util.StringTokenizer;
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


import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;




//import org.apache.commons.lang.ArrayUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;



import structures.Review;
import structures.MyPriorityQueue;


public class newEggAnalyzer {
	
	String m_threadURL;
	String m_threadTitle;
	ArrayList<JSONObject> m_threads;
	String forum_name;
	Tokenizer tokenizer;
	Set<String> m_stopwords;
	
	
	String[] itemName;
	Hashtable<String, Integer> pros_dictionary;
	Hashtable<String, Integer> cons_dictionary;
	int gram;
	
	
	public newEggAnalyzer(int gram, String[] itemName) throws InvalidFormatException, FileNotFoundException, IOException {
		this.gram = gram;
		this. itemName = itemName;
		m_threads = new ArrayList<JSONObject>();
		tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
		m_stopwords = new HashSet<String>();
	}
	
	
	public boolean isNumber(String token) {
		return token.matches("\\d+");
	}
	
	//Normalize.
	protected String Normalize(String token){
		token = Normalizer.normalize(token, Normalizer.Form.NFKC);
		token = token.replaceAll("\\W+", "");
		token = token.toLowerCase();
		if(isNumber(token))
			return "NUM";
		else 
			return token;
	}
	
	protected boolean isLegit(String token) {
		return !token.isEmpty() 
			&& !m_stopwords.contains(token)
			&& token.length()>1
			&& token.length()<20;
	}
	
	
	public void LoadStopwords(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				line = SnowballStemmingDemo(Normalize(line));
				if (!line.isEmpty())
					m_stopwords.add(line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n", m_stopwords.size(), filename);
		} catch(IOException e){
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}
	
	public void AnalyzeThreadedDiscussion(JSONObject json) {		
		try {
			
			for(String item: this.itemName){
				JSONObject jCamera = json.getJSONObject(item);
				pros_dictionary = new Hashtable<String, Integer>(); // initialize each time for new Item
				cons_dictionary = new Hashtable<String, Integer>(); // initialize each time for new Item
		
				for(String str:json.getNames(jCamera))
				{	
					JSONArray jarray = jCamera.getJSONArray(str);
					for(int i=0; i<jarray.length(); i++) {
						Review review = new Review(jarray.getJSONObject(i));
						String pros = review.getPros();
						String cons = review.getCons();
						TokenizerDemon(pros, cons);
					}	
				
				}
				
				String fileName = "F:\\amazon-newegg-data\\" + item + "_";
				generateCsvFile(fileName+"pros",pros_dictionary);
				generateCsvFile(fileName+"cons",cons_dictionary);
			}
			 
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	
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
		int size = 0;
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				System.out.println(f.getAbsolutePath());
				AnalyzeThreadedDiscussion(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		//size = m_threads.size() - size;
		System.out.println("Loading " + size + " json files from " + folder);
		
	}

	
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}

	
	public void TokenizerDemon(String pros, String cons) {
		for(String str:tokenizer.tokenize(pros)){
			String word = SnowballStemmingDemo(str).toLowerCase().replaceAll("\\W","");
			if(isLegit(word)){
				if(!word.equals("")){
					if(pros_dictionary.containsKey(word)) {
						Integer val = (Integer) pros_dictionary.get(word);
						pros_dictionary.put(word, val + 1);
					}
					else{
						pros_dictionary.put(word, 1);
					}
				}
			}
		}
		
		
		for(String str:tokenizer.tokenize(cons)){
			String word = SnowballStemmingDemo(str).toLowerCase().replaceAll("\\W","");
			if(isLegit(word)){
				if(!word.equals("")){
					if(cons_dictionary.containsKey(word)) {
						Integer val = (Integer) cons_dictionary.get(word);
						cons_dictionary.put(word, val + 1);
						}
					else{
						cons_dictionary.put(word, 1);
					}
				}
			}
		}
	}
	
	
	public void generateCsvFile(String sFileName, Hashtable<String, Integer> map)
    {
		
		
		
		//********Hastable sort*******************************/
        List<Map.Entry> list = new ArrayList<Map.Entry>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry>() {
            public int compare(Map.Entry e1, Map.Entry e2) {
                Integer i1 = (Integer) e1.getValue();
                Integer i2 = (Integer) e2.getValue();
                return i2.compareTo(i1);
            }
        });
		
		try
		{
			FileWriter writer = new FileWriter(sFileName+".csv");
			for (Map.Entry entry : list) {
				writer.append(entry.getKey()+","+entry.getValue()+"\n");
			}	    
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} 
   }
    
   public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {
	    int gram = 1;
	    String itemName [] = new String[]{"camera","tv","tablet", "laptop", "surveillance"};
	    String stopwords = "./data/Model/stopwords.dat";
		newEggAnalyzer analyzer = new newEggAnalyzer(gram, itemName);
		analyzer.LoadStopwords(stopwords);
		analyzer.LoadDirectory("F:\\amazon-newegg-data\\", ".json");
		
		
   }

}
