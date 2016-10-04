package model;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class security {

	public static String calculateSha1(String data)
	{
		return DigestUtils.sha1Hex(data);
	}
	
	public static Map<String, Object> cutLogin_Pass(Map<String, Object> foo, String id)
	{
		Map<String, Object> result = foo;
		
		result.remove("LOGIN");
		result.remove("HASLO");
		result.put("ID", id);
		
		return result;
	}
	
	public static String generateCode(String user_id)
	{
		 int split=user_id.length()/4;

		    String temp1=(user_id.substring(0,split));
		    String temp2=(user_id.substring(split,split*2));
		    String temp3=(user_id.substring(split*2,split*3));
		    String temp4=(user_id.substring(split*3));

		    double random = Math.random();
		    if (random < 0.25) 
		        return temp4 + temp1 + temp3 + temp2;
		    else if(random >= 0.25 & random < 0.5)
		        return temp2 + temp1 + temp4 + temp3;
		    else if(random >= 0.5 & random < 0.75)
		        return temp1 + temp4 + temp2 + temp3;
		    else
		    	return temp3 + temp2 + temp1 + temp4;
	}
	
	public static String generateNewPassword()
	{
		return RandomStringUtils.randomAlphabetic(10);
	}
}