package model;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;

public class utility {
	
	
	public static Map<String, Object> addId(SearchHit hit, String name)
	{
		Map<String, Object> result = new HashMap<String, Object>();
		 
		result.putAll(hit.getSource());
		result.put(name, hit.getId());
		return result;
	}

	public static Map<String, Object> addId(GetResponse getResponse, String name) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.putAll(getResponse.getSource());
		result.put(name, getResponse.getId());
		
		return result;
	}

	public static Map<String, Object> addInfoUser(SearchHit hit) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String user_id = hit.getSource().get("USER_ID").toString();
		String oplacone = hit.getSource().get("OPLACONE").toString();
				
		
		result.put("USER_ID", user_id);
		result.put("OPLACONE", oplacone);
		
		
		return result;
	}

}