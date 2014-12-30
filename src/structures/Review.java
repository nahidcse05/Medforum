/**
 * 
 */
package structures;

import json.JSONException;
import json.JSONObject;

/**
 * @author Md. Mustafizur Rahman
 * @version 0.1
 * @category data structure
 * data structure for a Review Post from newEgg.com
 */
public class Review {
	
	String m_pros;
	String m_cons;
	
	public String getPros()
	{
		return this.m_pros;
	}
	
	public String getCons()
	{
		return this.m_cons;
	}
	public Review(JSONObject json) {
		try {
			m_pros = json.getString("Pros");
			m_cons = json.getString("Cons");
					
		} catch (JSONException e) {
			//e.printStackTrace();
		}
	}
	
	public JSONObject getJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("Pros", m_pros);
		json.put("Cons", m_cons);
		
		return json;
	}
}
