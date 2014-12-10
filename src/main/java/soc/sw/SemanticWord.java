package soc.sw;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class SemanticWord {
	public static Vector<Double> scoreVec = new Vector<Double>();
	
	public static void main(String[] args) {
		//GetSemanticWordsBySingleWord("toast");
		Vector<String> tmpVector = new Vector<String>();
        for (String s: args) {
            tmpVector.add(s);
        }
		//tmpVector.add("music");
		//tmpVector.add("rock");
		//tmpVector.add("jazz");
//		Vector<String> resultStrings = GetSemanticWordsByMultipleWords(tmpVector, 3, 9);
//		for (int i= 0; i < resultStrings.size();i++){
//			System.out.print(resultStrings.get(i));
//			if (i != resultStrings.size() -1) {
//				System.out.print("\t");
//			} else {
//				System.out.println("");
//			}
//		}
		Vector<String> userTags = new Vector<String>();
		Vector<String> videoTags = new Vector<String>();
		
		userTags.add("cat");
		userTags.add("dog");
		userTags.add("rabit");
		
		videoTags.add("animal");
		videoTags.add("play");
		videoTags.add("cute");
		
		System.out.println("Sim between user and video is " + getTagSimilarityScore("cat", videoTags));
		//System.out.println("Sim between user and video is " + getTagVectorSimilarityScore(userTags, videoTags));
	}

	private static Vector<String> GetSemanticWordsByMultipleWords(
			final Vector<String> inTags, int inTargetSize, int outputTargetSize) {
		HashMap<String, Long> tmpMap = new HashMap<String, Long>();
		Vector<String> resultStringVector = new Vector<String>();
		int inSize = Math.min(inTags.size(), inTargetSize);
		
		for (int i = 0; i < inSize; i++) {
			Vector<String> tmpStringVector = GetSemanticWordsBySingleWord(inTags.get(i));
			final int compensateWeitht = 50 - tmpStringVector.size();
			final int BASE_WEIGHT = 1000;
			for (int j = 0; j < tmpStringVector.size(); j++) {
				String newKey = tmpStringVector.get(j);
				if (tmpMap.get(newKey)== null) {
					tmpMap.put(newKey, (long)(BASE_WEIGHT + compensateWeitht + j));
				} else {
					long curVal = tmpMap.get(newKey);
					tmpMap.put(newKey, (long)(BASE_WEIGHT + compensateWeitht + j + curVal));
				}
			}
		}
		
		//System.out.println("unsorted map: " + tmpMap);
		// do the sorting 
	    ValueComparator bvc =  new ValueComparator(tmpMap);
	    TreeMap<String,Long> sortedMap = new TreeMap<String,Long>(bvc);
	    sortedMap.putAll(tmpMap);
	    
	    //System.out.println("sorted map: " + sortedMap);
	    Iterator<Map.Entry<String,Long>> it = sortedMap.entrySet().iterator();
	    int i = 1;
	    while (it.hasNext()){
	    	if (i > outputTargetSize) {
	    		break;
	    	}
	    	//System.out.println(it.next().getKey());
	    	resultStringVector.add(it.next().getKey());
	    	i++;
	    }
	    
		return resultStringVector;
	}
	
	private static Vector<String> GetSemanticWordsBySingleWord(String inWord) {
		String strURL = "http://conceptnet5.media.mit.edu/data/5.2/c/en/" + inWord;
		String tmpString;
		String rawJSONString = new String();
		Vector<String> resultString = new Vector<String>();
		//System.out.println("Get URL        " + new Date().getTime());
		try {
			URL targetURL = new URL(strURL);
			//URLConnection uc = targetURL.openConnection(Proxy.NO_PROXY);
			URLConnection uc = targetURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			while ((tmpString = in.readLine()) != null) {
				rawJSONString = rawJSONString + tmpString;
			}
			in.close();
			//System.out.print(rawJSONString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Parse JSON     " + new Date().getTime());
		JsonElement jElement = new JsonParser().parse(rawJSONString);
		JsonArray  jArray = jElement.getAsJsonObject().get("edges").getAsJsonArray();
		
		//System.out.println("The size of json array is " + jArray.size());
		//System.out.println("Parse JSON Det " + new Date().getTime());
		Set<String> set = new HashSet<String>();
		for ( int i = 0; i < jArray.size(); i++) {
			String startString = jArray.get(i).getAsJsonObject().get("startLemmas").toString().replaceAll("\"", "");
			String endString = jArray.get(i).getAsJsonObject().get("endLemmas").toString().replaceAll("\"", "");
			//System.out.println("--start: " + startString);
			//System.out.println("--end: " + endString);
			if (inWord.equals(startString) && inWord.equals(endString)) {
				continue;
			}
			if (inWord.equals(startString)) { 
				tmpString = endString;
			} else {
				tmpString = startString;
			}
			if (IsMoreThanTwoWords(tmpString)) {
				continue;
			}
			if (!set.contains(tmpString)){ 
				set.add(tmpString);
				resultString.add(tmpString);
			}
		}
		//System.out.println("The size of result strings: " + resultString.size());
		//System.out.println("Reverse sorting"  + new Date().getTime());
		Collections.reverse(resultString);

		return resultString;
	}
	
	private static Boolean IsMoreThanTwoWords(String inStr) {
		int cnt = 0;
		int idx = 0 ;
		while ((idx = inStr.indexOf(" ", idx)) != -1) {
			cnt++;
			idx++;
			if (cnt > 2 ) {
				return true;
			}
		}
		return false;
	}
	
	private static Double getSimilarity(final String inStr1,final String inStr2) {
		Double score_1 = getOneWaySimilarity(inStr1, inStr2);
		//Double score_2 = getOneWaySimilarity(inStr2, inStr1);
		return score_1;
	}
	
	private static Double getOneWaySimilarity(final String inStr1, final String inStr2) {
		String str1 = inStr1.replaceAll(" ", "_");
		String str2 = inStr2.replaceAll(" ", "_");
		String strURL = "http://conceptnet5.media.mit.edu/data/5.2/assoc/c/en/"
						+str1+"?filter=/c/en/"+str2+"&limit=1";
		String tmpString = new String();
		String rawJSONString = new String();
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
		JsonArray jArray = jElement.getAsJsonObject().get("similar").getAsJsonArray();
		if (jArray.size() == 0) {
			return 0.0;
		} else {
			return Double.parseDouble(jArray.get(0).getAsJsonArray().get(1).toString());
		}
	}
	
	private static double getTagVectorSimilarityScore(final Vector<String> inTags1, final Vector<String> inTags2) {
		final int THRESHOLD = 2; 
		Vector<String> vec1 = GetSemanticWordsByMultipleWords(inTags1, 3, 3);
		Vector<String> vec2 = GetSemanticWordsByMultipleWords(inTags2, 3, 3);
		vec1.addAll(inTags1);
		vec2.addAll(inTags2);
		
		//Vector<Double> scoreVec = new Vector<Double>();
		scoreVec.clear();
		// create thread pool
		//List<GetSimilarityThread> threads = new ArrayList<GetSimilarityThread>();
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < vec1.size(); i ++) {
			for (int j = 0; j < vec2.size(); j++) {
				//System.out.println("Compare " + vec1.get(i) + " with " + vec2.get(j));
				//scoreVec.add(getSimilarity(vec1.get(i), vec2.get(j)));
				
				//GetSimilarityThread newThread = new GetSimilarityThread(vec1.get(i), vec2.get(j));
				threads.add(new Thread(new GetSimilarityThread(vec1.get(i), vec2.get(j))));
			}
		}
		System.out.println("Start thread"  + new Date());
		// start the thread
		//System.out.println("Thread size: " + threads.size());
		for (int i =0; i < threads.size(); i++) {
			threads.get(i).start();
		}
		System.out.println("Start Join"  + new Date());
		for (int i =0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("End thread  "  + new Date());
		
		Collections.sort(scoreVec);
		Collections.reverse(scoreVec);
		System.out.println("Score Vec size: " + scoreVec.size());
//		for (int i = 0; i < scoreVec.size(); i ++) {
//			System.out.println(scoreVec.get(i));
//		}
		
		int half_size = scoreVec.size()/THRESHOLD;
		double result = 0.0;
		for (int i = 0 ; i < half_size; i++) {
			result += scoreVec.get(i);
		}

		return result/half_size;
	}


	public static double getTagSimilarityScore(final String inTag1, final Vector<String> inTags2) {
		final int THRESHOLD = 2; 
		Vector<String> vec2 = GetSemanticWordsByMultipleWords(inTags2, 3, 3);
		vec2.addAll(inTags2);
		
		//Vector<Double> scoreVec = new Vector<Double>();
		scoreVec.clear();
		// create thread pool
		//List<GetSimilarityThread> threads = new ArrayList<GetSimilarityThread>();
		List<Thread> threads = new ArrayList<Thread>();
		
		for (int j = 0; j < vec2.size(); j++) {
			//System.out.println("Compare " + vec1.get(i) + " with " + vec2.get(j));
			//scoreVec.add(getSimilarity(vec1.get(i), vec2.get(j)));
			
			threads.add(new Thread(new GetSimilarityThread(inTag1, vec2.get(j))));
		}
	
		System.out.println("Start thread"  + new Date());
		// start the thread
		//System.out.println("Thread size: " + threads.size());
		for (int i =0; i < threads.size(); i++) {
			threads.get(i).start();
		}
		System.out.println("Start Join"  + new Date());
		for (int i =0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("End thread  "  + new Date());
		
		Collections.sort(scoreVec);
		Collections.reverse(scoreVec);
		System.out.println("Score Vec size: " + scoreVec.size());

		
		int half_size = scoreVec.size()/THRESHOLD;
		double result = 0.0;
		for (int i = 0 ; i < half_size; i++) {
			result += scoreVec.get(i);
		}
	
		return result*100/half_size;
	}
}


class ValueComparator implements Comparator<String> {

    Map<String, Long> base;
    public ValueComparator(Map<String, Long> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
