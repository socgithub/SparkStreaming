package soc.sw;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.spec.ECFieldF2m;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


//public class GetSimilarityThread extends Thread {
public class GetSimilarityThread implements Runnable {
	
	private Thread t;
	private String threadName;
	private String str1;
	private String str2;
	
	public GetSimilarityThread(String str1, String str2) {
		this.str1 = str1;
		this.str2 = str2;
		this.threadName = str1 + str2;
	}
	
	public void setUp(String str1, String str2) {
		this.str1 = str1;
		this.str2 = str2;
		this.threadName = str1 + str2;
	}
	
	@Override
	public void run() {
		//System.out.println("Run " +  threadName );
		//getSimilarity(this.str1, this.str2);
		SemanticWord.scoreVec.add(getSimilarity(this.str1, this.str2));
		//System.out.println("Run Done" +  threadName );
	}

	private Double getSimilarity(final String inStr1,final String inStr2) {
		Double score_1 = getOneWaySimilarity(inStr1, inStr2);
		//Double score_2 = getOneWaySimilarity(inStr2, inStr1);
		return score_1;
	}
	
//	@Override
//	public void start (){
//		System.out.println("Starting " +  threadName );
//	    if (t == null){
//	    	t = new Thread (this, threadName);
//	        t.start ();
//	    }
//	}
	
	private  Double getOneWaySimilarity(final String inStr1, final String inStr2) {
		String str1 = inStr1.replaceAll(" ", "_");
		String str2 = inStr2.replaceAll(" ", "_");
		String strURL = "http://conceptnet5.media.mit.edu/data/5.2/assoc/c/en/"
						+str1+"?filter=/c/en/"+str2+"&limit=1";
		String tmpString = new String();
		String rawJSONString = new String();
		StringBuffer response = new StringBuffer();
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(strURL).openConnection();
	        con.setDoOutput(true);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", "Mozilla/5.0");
	        con.setRequestProperty("Accept", "*/*");
	        
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(con.getInputStream()));        
	        String inputLine = new String();
	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();
	        con.disconnect();
		} catch (Exception e){
			e.printStackTrace();
		}
		/*
		try {
			URL targetURL = new URL(strURL);
			URLConnection uc = targetURL.openConnection(Proxy.NO_PROXY);
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			while ((tmpString = in.readLine()) != null) {
				rawJSONString = rawJSONString + tmpString;
			}
			in.close();
			//System.out.print(rawJSONString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JsonElement jElement = new JsonParser().parse(rawJSONString);
		*/
		JsonElement jElement = new JsonParser().parse(response.toString());
		JsonArray jArray = jElement.getAsJsonObject().get("similar").getAsJsonArray();
		if (jArray.size() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(jArray.get(0).getAsJsonArray().get(1).toString());
		}
	}
	
}


