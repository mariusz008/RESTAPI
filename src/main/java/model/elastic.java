package model;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchParseException;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.client.RestTemplate;

import mail.mailSender;
import model.security;

public class elastic {
	
	final static String actionForbidden = "Action forbidden. Login again.";

	static Node node = nodeBuilder().settings(Settings.builder()
			.put("path.home", "/usr/share/elasticsearch")
			.put("path.data", "/var/lib/elasticsearch")
			.put("path.work", "/tmp/elasticsearch")
			.put("path.logs", "/var/log/elasticsearch")
			.put("path.conf", "/etc/elasticsearch")).node();
	static Client client = node.client();
	
	static RestTemplate restTemplate = new RestTemplate();

	//ok
	//---------------------------------------------------------------------------------------------------------
	private static String getUserIdByToken(String token)
	{
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("TOKEN", token))
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() == 0)
			return null;
		else
			return response.getHits().getHits()[0].getId();
	}
	
	private static String getToken(String user_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "user", user_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("TOKEN") == null)
				return null;
			else
				return getResponse.getSource().get("TOKEN").toString();
		}
		
		else return null;
	}
	
	public static String generateToken(String user_id, String actual_token)
	{
		String token = security.calculateSha1(actual_token);
		 try {
				client.prepareUpdate("projektzespolowy", "user", user_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("TOKEN", token)
				                    		.endObject())
				                    .get();		
			} catch (IOException e) {
				e.printStackTrace();
			}
		 return token;
	}
	//---------------------------------------------------------------------------------------------------------
	//ok
	public static message addUser(String name, String surname, String login, String password, String email, String age, String sex, String club, String obywatelstwo, String nr_tel, String ice)
	{
		if(checkUserLogin(login) == true)
		{
			if(checkUserEmail(email) == true)
			{
				String user_id = client.prepareIndex("projektzespolowy", "user").setSource(createUser(name, surname, login, password, email, age, sex, club, obywatelstwo, nr_tel, ice)).execute().actionGet().getId();
				String code = addCode(user_id);
				mailSender.initialization();
				mailSender.sendMail(email, "Aktywacja konta", "Witaj " + login + "!<br>By aktywować swoje konto kliknij w link poniżej:<br>" + "http://209785serwer.iiar.pwr.edu.pl/Rest1/rest/activate/user?code=" + code);
				return new message("200", "User added");
			}
			else
			{
				return new message("215", "Email already in use");
			}
		}
		else
		{
			return new message("215", "Login already in use");
		}
		
	}
	
	public static String userActivation(String code)
	{
		if(code.equals("done"))
			return "Account with this key doesn't exist";
		String result = setActive(code);
		return result;
	}
	
	public static Map<String, Object> getUser(String token)
	{
		Map<String, Object> source = new HashMap<String, Object>();
		
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			{
			 source.put("code", "230");
			 source.put("content", actionForbidden);
			 return source;
			}
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "user", user_id).execute().actionGet();
		if(getResponse.isExists())
		{
			source.putAll(getResponse.getSource());
			
			return source;
		}
		else
			return null;
	}
	
	public static Map<String, Object> login(String login, String password)
	{
		if(login.isEmpty() && password.isEmpty())
		{
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("CODE", "215");
			result.put("MESSAGE", "Forbidden login");
		}
	
		Map<String, Object> response = tryLogin(login, password);
	
		return response;
	}
	
	public static Map<String, Object> createUser(String name, String surname, String login, String password, String email, String age, String sex, String club, String obywatelstwo, String nr_tel, String ice)
	{
		Map<String, Object> jsonDoc = new HashMap<String, Object>();

		jsonDoc.put("LOGIN", login);
		jsonDoc.put("HASLO", security.calculateSha1(password));
		jsonDoc.put("IMIE", name);
		jsonDoc.put("NAZWISKO", surname);
		jsonDoc.put("EMAIL", email);
		jsonDoc.put("WIEK", age);
		jsonDoc.put("PLEC", sex);
		jsonDoc.put("KLUB", club);
		jsonDoc.put("OBYWATELSTWO", obywatelstwo);	
		jsonDoc.put("NR_TEL", nr_tel);	
		jsonDoc.put("ICE", ice);	
		jsonDoc.put("ACTIVATE", "false");
		
		return jsonDoc;
	}
	
	
	public static Map<String, Object> createCompetition(String user_id, String name, String data_rozp, String czas_rozp, String data_zak, String czas_zak, String typ, String limit_ucz, String miejscowosc, String oplata,String opis, String wieloetapowe)
	{

		Map<String, Object> jsonDoc = new HashMap<String, Object>();
		jsonDoc.put("USER_ID", user_id);
		jsonDoc.put("NAME", name);
		jsonDoc.put("DATA_ROZP", data_rozp);
		jsonDoc.put("CZAS_ROZP", czas_rozp);
		jsonDoc.put("DATA_ZAK", data_zak);
		jsonDoc.put("CZAS_ZAK", czas_zak);
		jsonDoc.put("TYP", typ);
		jsonDoc.put("LIMIT_UCZ", limit_ucz);
		jsonDoc.put("MIEJSCOWOSC", miejscowosc);
		jsonDoc.put("OPLATA", oplata);
		jsonDoc.put("OPIS", opis);
		jsonDoc.put("WIELOETAPOWE", wieloetapowe);
		return jsonDoc;
	}
	
	
	public static Map<String, Object> createRoute()
	{
		Map<String, Object> jsonDoc = new HashMap<String, Object>();
		
		jsonDoc.put("DYSTANS", "");	
		
		return jsonDoc;
	}
	
	// true = OK
	// false = taken
	public static boolean checkUserLogin(String login)
	{
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.termQuery("LOGIN", login))
				.setSize(0).setFrom(0)
				.execute()
				.actionGet();
		
		
		if(response.getHits().getTotalHits() > 0)
			return false;
		else 
			return true;
	}
	
	// true = OK
	// false = taken
	public static boolean checkUserEmail(String email)
	{
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchPhraseQuery("EMAIL", email))
				.setSize(0).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() > 0)
			return false;
		else 
			return true;
	}
	
	public static Map<String, Object> tryLogin(String login, String password)
	{
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("LOGIN", login));
		boolQuery.must(QueryBuilders.matchQuery("HASLO", security.calculateSha1(password)));
		
		
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		Map<String, Object> result = new HashMap<String, Object>();
		
		if(results.length != 0)
		{
			if(checkIfActivated(results[0].getSource()))
			{
				String user_id = results[0].getId();
				String token = getToken(user_id);
				if(token == null)
					generateToken(user_id, user_id);
				else
					generateToken(user_id, token);
				
				results[0].getSource().put("TOKEN", getToken(user_id));
				return security.cutLogin_Pass(results[0].getSource(), getToken(user_id));
			}
			else
			{
				result.put("CODE", "215");
				result.put("MESSAGE", "Account not activated");
				
				return result;
			}
		}
		
		result.put("CODE", "215");
		result.put("MESSAGE", "WRONG PASSWORD OR LOGIN");
		
		return result;
		
	}


	public static Map<String, Object> signForCompetition(String user_id, String competition_id, String category_name, String token) 
	{
		Map<String, Object> jsonDoc = new HashMap<String, Object>();
		jsonDoc.putAll(getUser(token));
		jsonDoc.putAll(getCompetition(competition_id));
		jsonDoc.put("USER_ID", user_id);
		jsonDoc.put("OPLACONE", "nie");
		jsonDoc.put("CATEGORY", category_name);
		
		jsonDoc.remove("OBYWATELSTWO");
		jsonDoc.remove("CZAS_ZAK");
		jsonDoc.remove("DATA_ZAK");
		jsonDoc.remove("ACTIVATE");
		jsonDoc.remove("CODE");
		jsonDoc.remove("OPIS");
		jsonDoc.remove("ICE");
		jsonDoc.remove("LIMIT_UCZ");
		jsonDoc.remove("NR_TEL");
		jsonDoc.remove("LOGIN");
		jsonDoc.remove("HASLO");
		jsonDoc.remove("KLUB");
		jsonDoc.remove("OPLATA");
		jsonDoc.remove("TOKEN");
		
		return jsonDoc;
	}

	public static Boolean checkFreePlaces(String competition_id)
	{
			SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id))
				.setSize(0).setFrom(0)
				.execute()
				.actionGet();

		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		
		String limit = getResponse.getSource().get("LIMIT_UCZ").toString();
		if(limit.isEmpty())
			return true;
		
		if(Integer.parseInt(limit) <= response.getHits().getTotalHits())
			return false;
		else
			return true;
	}


	public static boolean checkIfAlreadySigned(String user_id, String competition_id) 
	{
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		
		
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(0).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() != 0)
		return true;
		else
		return false;
	}
	
	public static String checkIfSignedReturnId(String user_id, String competition_id) 
	{
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		
		
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() != 0)
		{
			return response.getHits().getHits()[0].getId();
		}
		else
		return null;
	}
	
	public static Vector<Map<String, Object>> getParticipantsFiltrated(String competition_id, String sex, String age, String phrase, String category) {
		
		Vector<Map<String, Object>> res = new Vector<Map<String, Object>>();
		
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		
		if(!sex.isEmpty())
		boolQuery.must(QueryBuilders.matchQuery("PLEC", sex));
		
		if(!age.isEmpty())
		boolQuery.must(QueryBuilders.matchQuery("WIEK", age));
		
		String[] parts = phrase.split(" ");
		if(parts.length==1)
		{
			boolQuery.should(QueryBuilders.wildcardQuery("IMIE", "*"+parts[0].toLowerCase()+"*")).should(QueryBuilders.wildcardQuery("NAZWISKO", "*"+parts[0].toLowerCase()+"*")).minimumNumberShouldMatch(1);
		}
		else
		{
			boolQuery.must(QueryBuilders.wildcardQuery("IMIE", "*"+parts[0].toLowerCase()+"*"));
			boolQuery.must(QueryBuilders.wildcardQuery("NAZWISKO", "*"+parts[1].toLowerCase()+"*"));
		}
		
		if(!category.isEmpty())
		{
			String[] partsSecando = category.split(" ");
			for(String part : partsSecando)
				boolQuery.must(QueryBuilders.matchQuery("CATEGORY", part));
		}
		
		
		try{SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.addSort("IMIE", SortOrder.ASC)
				.addSort("NAZWISKO", SortOrder.ASC)
				.setFrom(0)
				.setSize(10000)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
			 res.add(hit.getSource());
		 }
		return res;
		}catch(Throwable e)
		{
			return null;
		}
	}


	public static String findEvent(String user_id, String competition_id) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		
		
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() > 0)
			return response.getHits().getHits()[0].getId();
		else
			return null;
	}
	
	public static Boolean checkIfUserExist(String user_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "user", user_id).execute().actionGet();
		
		if(getResponse.isExists())
		return true;
		else
		return false;
	}
	
	public static Boolean checkIfUserExist(String user_id, String password)
	{
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "user", user_id).execute().actionGet();
		if(getResponse.getSource().get("HASLO").toString().equals(security.calculateSha1(password)))
			return true;
		else
			return false;
	}


	public static boolean checkIfCompetitionsExist(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		
		if(getResponse.isExists())
		return true;
		else
		return false;
	}
	
	public static boolean checkIfActivated(Map<String, Object> data)
	{
		String status = data.get("ACTIVATE").toString();
		if(status.equals("false") || status.equals(null))
			return false;
		else
			return true;
	}
	
	public static String setActive(String code)
	{
		String user_id = elastic.findUserByCode(code);

        if(user_id != null)
        {
	        try {
				client.prepareUpdate("projektzespolowy", "user", user_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("ACTIVATE", "true")
				                    			.field("CODE", "done")
				                    		.endObject())
				                    .get();
				
				return "Account activated";
			} catch (IOException e) {
				return "Something went wrong. Conntact with administration";
			}
        }
        else
        	return "Account with this key doesn't exist";
	}


	private static String findUserByCode(String code) {
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("CODE", code))
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() != 0)
			{
				return response.getHits().getHits()[0].getId();
			}
		else 
			return null;
	}


	public static String addCode(String user_id) {
		 try {
			 	String code = security.generateCode(user_id);
				client.prepareUpdate("projektzespolowy", "user", user_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("CODE", code)
				                    		.endObject())
				                    .get();			
				return code;
			} catch (IOException e) {
				return null;
			}
		
	}
	
	
	public static String getEmail(String user_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "user", user_id).execute().actionGet();
		if(getResponse.isExists())
		{
			return getResponse.getSource().get("EMAIL").toString();
		}
		
		else return null;
	}


	public static String getEmailByEvent(String event_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "event", event_id).execute().actionGet();
		if(getResponse.isExists())
		{
			return getEmail(getResponse.getSource().get("USER_ID").toString());
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                                                                                                             	//
    //                                                                                                              //
	//                                                                                                              //
	//                                                                                                              //
	//                                                 ZAWODY                                                       //
	//                                                                                                              //
	//                                                                                                              //
	//                                                                                                              //
	//                                                                                                              //
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static String getCompetitionNameByEvent(String event_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "event", event_id).execute().actionGet();
		if(getResponse.isExists())
		{
			return getCompetitionName(getResponse.getSource().get("COMPETITION_ID").toString());
		}
		return null;
	}
	
	public static String getCompetitionName(String com_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", com_id).execute().actionGet();
		if(getResponse.isExists())
		{
			return getResponse.getSource().get("NAME").toString();
		}
		
		else return null;
	}
	
	public static Vector<Map<String, Object>> getAllCompetitions(Client client)
	{
		Vector<Map<String, Object>> result = new Vector<Map<String, Object>>();
		
		try{
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("competition")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setFrom(0)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
			 result.add(utility.addId(hit, "COMPETITION_ID"));
		 }
		return result;	
		}catch(Throwable e)
		{
			return null;
		}			
	}
	
	public static boolean checkIfExist(String email, String login)
	{
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchPhraseQuery("EMAIL", email));
		boolQuery.must(QueryBuilders.matchQuery("LOGIN", login));
		
		
		try{
			SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() > 0)
			return true;
		else
			return false;
		}catch(Throwable e)
		{
			return false;
		}
		
	}
	
	public static String getUserIdByLogin(String login)
	{
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("LOGIN", login))
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() != 0)
			{
				return response.getHits().getHits()[0].getId();
			}
		else
			return null;
	}
	
	
	public static String addCodeForPasswordRecorvery(String user_id) {
		 try {
			 	String code = security.generateCode(user_id);
				client.prepareUpdate("projektzespolowy", "user", user_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("RECORVERY_CODE", code)
				                    		.endObject())
				                    .get();			
				return code;
			} catch (IOException e) {
				return null;
			}
		
	}


	public static String generateNewPassword(String code) {
		if(code.equals("done"))
			return "Account with this key doesn't exist";
		
		String user_id = elastic.findUserByRecorveryCode(code);

        if(user_id != null)
        {
	        try {
				String pass = security.generateNewPassword();
				
				client.prepareUpdate("projektzespolowy", "user", user_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("HASLO", security.calculateSha1(pass))
				                    			.field("RECORVERY_CODE", "done")
				                    		.endObject())
				                    .get();
				
				String email = getEmail(user_id);
				
				mailSender.initialization();
				mailSender.sendMail(email, "Nowe haslo", "Witaj.<br>Twoje nowe haslo to: " + pass + "<br>Nie zapomnij go zmienić zaraz po zalogowaniu!");
				return "Check your email for new password";
			} catch (IOException e) {
				return "Something went wrong. Conntact with administration";
			}
        }
        else
        	return "Account with this key doesn't exist";
	}
	
	private static String findUserByRecorveryCode(String code) {
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("RECORVERY_CODE", code))
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		if(response.getHits().getTotalHits() != 0)
			{
				return response.getHits().getHits()[0].getId();
			}
		else 
			return null;
	}
	



	public static message changePassword(String token, String old_password, String new_password) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "user", user_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(!getResponse.getSource().get("HASLO").equals(security.calculateSha1(old_password)))
			{
				return new message("215", "Wrong password");
			}
			
			try{
				client.prepareUpdate("projektzespolowy", "user", user_id) 
					.setDoc(jsonBuilder()
							.startObject()
								.field("HASLO", security.calculateSha1(new_password))
							.endObject())
					.get();
				return new message("200", "Password changed");
			}catch (IOException e) {
				return new message("215", "Something went wrong");
			}	
		}
		return new message("215", "No such user");
	}


	public static String deactivateUser(String user_id) {
		try{
			client.prepareUpdate("projektzespolowy", "user", user_id) 
				.setDoc(jsonBuilder()
						.startObject()
							.field("LOGIN", "")
							.field("HASLO", "")
							.field("EMAIL", "")
							.field("OBYWATELSTWO", "")
							.field("NR_TEL", "")
							.field("ICE", "")
							.field("WIEK", "")
							.field("PLEC", "")
						.endObject())	
				.get();
			return removeFromEvents(user_id);
		}catch (IOException e) {
			return e.getMessage();
		}	
		
	}


	private static String removeFromEvents(String user_id) {
		try{
			SearchResponse response = client.prepareSearch("projektzespolowy")
					.setTypes("event")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.matchQuery("USER_ID", user_id))
					.setFrom(0)
					.execute()
					.actionGet();
			
			SearchHit[] results = response.getHits().getHits();
			DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
			 for (SearchHit hit : results) {
				 String date = hit.getSource().get("DATA_ROZP").toString();	
				 date = date + " " + hit.getSource().get("CZAS_ROZP").toString();
				 DateTime eventDate = formatter.parseDateTime(date);
				 
				 
					if(eventDate.isBeforeNow())
					{
						client.prepareDelete("projektzespolowy", "event", hit.getId()).execute().actionGet();
					}
			 }
			
			return "Ok";
		}catch(Throwable e)
		{
			return e.getMessage();
		}
	}
	
	public static message updateProfile(String name, String surname, String email, String token, String wiek, String klub, String nr, String ICE, String obywatelstwo)
	{
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		try{
			client.prepareUpdate("projektzespolowy", "user", user_id) 
				.setDoc(jsonBuilder()
						.startObject()
							.field("IMIE", name)
							.field("NAZWISKO", surname)
							.field("EMAIL", email)
							.field("WIEK", wiek)
							.field("KLUB", klub)
							.field("OBYWATELSTWO", obywatelstwo)
							.field("NR_TEL", nr)
							.field("ICE", ICE)
						.endObject())				
				.get();
			
			return new message("200", "Profile updated");
		}catch (IOException e) {
			return new message("215", "Something went wrong");
		}	
	}


	public static message updateCompetition(String token, String competition_id, String name, String data_rozp, String czas_rozp,
			String data_zak, String czas_zak, String typ, String limit_ucz, String miejscowosc, String oplata,
			String opis, String wieloetapowe) {
		
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		
		if(!getResponse.getSource().get("USER_ID").toString().equals(user_id))
			return new message ("215", "You can't edit not yours competitions");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
		
		 String date = getResponse.getSource().get("DATA_ROZP").toString();	
		 date = date + " " + getResponse.getSource().get("CZAS_ROZP").toString();
		 DateTime eventDate = formatter.parseDateTime(date);
		 
		 
			if(eventDate.isAfterNow())
			{
			try{
				client.prepareUpdate("projektzespolowy", "competition", competition_id) 
					.setDoc(jsonBuilder()
							.startObject()
								.field("NAME", name)
								.field("DATA_ROZP", data_rozp)
								.field("CZAS_ROZP", czas_rozp)
								.field("DATA_ZAK", data_zak)
								.field("CZAS_ZAK", czas_zak)
								.field("TYP", typ)
								.field("LIMIT_UCZ", limit_ucz)
								.field("MIEJSCOWOSC", miejscowosc)
								.field("OPLATA", oplata)
								.field("OPIS", opis)
								.field("WIELOETAPOWE", wieloetapowe)
							.endObject())
					.get();
				
				return new message("200", "Competition updated");
			}catch (IOException e) {
				return new message("215", "Something went wrong");
			}
			}
			else
				return new message("215", "You can't edit competitions that already have taken place");
	}


	public static Vector<Map<String, Object>> getUserEvents(String token, String type, String name,
			String place, String wieloetapowe) {
				Vector<Map<String, Object>> res = new Vector<Map<String, Object>>();
				
				String user_id = getUserIdByToken(token);
				if(user_id == null)
				{
					Map<String, Object> foo = new HashMap<String, Object>();
					foo.put("code", "230");
					foo.put("content", actionForbidden);
					res.add(foo);
					return res;
				}
		
		
		try{
			BoolQueryBuilder boolQuery = new BoolQueryBuilder();
			
			boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
			
			if(!type.isEmpty())
			{
				String[] parts = type.split(" ");
				for(String part : parts)
				boolQuery.must(QueryBuilders.wildcardQuery("TYP", "*"+part.toLowerCase()+"*"));
			}
			
			if(!name.isEmpty())
			{
				String[] parts = name.split(" ");
				for(String part : parts)
					boolQuery.must(QueryBuilders.wildcardQuery("NAME", "*"+part.toLowerCase()+"*"));
			}
			
			if(!place.isEmpty())
			{
				String[] parts = place.split(" ");
				for(String part : parts)
					boolQuery.must(QueryBuilders.wildcardQuery("MIEJSCOWOSC", "*"+part.toLowerCase()+"*"));
			}
			if(!wieloetapowe.isEmpty())
			{
				if(wieloetapowe.length()==1) {
					boolQuery.should(QueryBuilders.wildcardQuery("WIELOETAPOWE", "0")).should(QueryBuilders.wildcardQuery("WIELOETAPOWE", "1")).minimumNumberShouldMatch(1);
				}
				else if(wieloetapowe.length()>1)
					boolQuery.must(QueryBuilders.matchQuery("WIELOETAPOWE", wieloetapowe));
			}

			SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.addSort("DATA_ROZP", SortOrder.ASC)
				.addSort("CZAS_ROZP", SortOrder.ASC)
				.setFrom(0)
				.setSize(10000)
				.execute()
				.actionGet();


		 SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
				 res.add(hit.getSource());
		 } 
		 return res;
		}catch(Throwable e)
		{
			System.out.print(e.getMessage());
			return null;
		}
	}

	public static message addCompetition(String token, String name, String data_rozp, String czas_rozp,
			String data_zak, String czas_zak, String typ, String limit_ucz, String miejscowosc, String oplata,
			String opis, String image, String wieloetapowe){
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(checkIfUserExist(user_id))
		{
			String id = client.prepareIndex("projektzespolowy", "competition").setSource(createCompetition(user_id, name, data_rozp, czas_rozp, data_zak, czas_zak, typ, limit_ucz, miejscowosc, oplata, opis, wieloetapowe)).execute().actionGet().getId();
			try{
				@SuppressWarnings("unused")
				String result = restTemplate.getForObject("http://209785serwer.iiar.pwr.edu.pl/RestImage/rest/competition/image?image="+image+"&competition_id="+id,String.class);
			}catch(Exception e)
			{
				return new message("215", e.getMessage());
			}
			
			return new message("200", "Competitions Created");
		}
		else
			return new message("215", "User does not exist");
	}


	public static Vector<Map<String, Object>> showCompetitions(String type, String name, String place, String wieloetapowe) {
		Vector<Map<String, Object>> result = new Vector<Map<String, Object>>();
		
		try{
			BoolQueryBuilder boolQuery = new BoolQueryBuilder();
			
			if(!type.isEmpty())
			{
				String[] parts = type.split(" ");
				for(String part : parts)
				boolQuery.must(QueryBuilders.wildcardQuery("TYP", "*"+part.toLowerCase()+"*"));
			}
			
			if(!name.isEmpty())
			{
				String[] parts = name.split(" ");
				for(String part : parts)
					boolQuery.must(QueryBuilders.wildcardQuery("NAME", "*"+part.toLowerCase()+"*"));
			}
			
			if(!place.isEmpty())
			{
				String[] parts = place.split(" ");
				for(String part : parts)
					boolQuery.must(QueryBuilders.wildcardQuery("MIEJSCOWOSC", "*"+part.toLowerCase()+"*"));
			}
			if(!wieloetapowe.isEmpty())
			{
				if(wieloetapowe.length()==1) {
					boolQuery.should(QueryBuilders.wildcardQuery("WIELOETAPOWE", "0")).should(QueryBuilders.wildcardQuery("WIELOETAPOWE", "1")).minimumNumberShouldMatch(1);
				}
				else if(wieloetapowe.length()>1)
					boolQuery.must(QueryBuilders.matchQuery("WIELOETAPOWE", wieloetapowe));
			}
			
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("competition")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.addSort("DATA_ROZP", SortOrder.ASC)
				.addSort("CZAS_ROZP", SortOrder.ASC)
				.setFrom(0)
				.setSize(10000)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
			 hit.getSource().remove("DATA_ZAK");
			 hit.getSource().remove("CZAS_ZAK");
			 hit.getSource().remove("LIMIT_UCZ");
			 hit.getSource().remove("OPLATA");
			 hit.getSource().remove("OPIS");
			 Long count = getHowManyPeoplePart(hit.getId());
			 Map<String, Object> tmp = utility.addId(hit, "COMPETITION_ID");
			 tmp.put("ILE_OSOB", count);
			 result.add(tmp);

		 }
		return result;	
		}catch(Throwable e)
		{
			return null;
		}
	}

	private static Long getHowManyPeoplePart(String competition_id) {
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id))
				.setSize(0).setFrom(0)
				.execute()
				.actionGet();
		
		return response.getHits().getTotalHits();
	}

	public static Vector<Map<String, Object>> showMyCompetitions(String token, String type, String name,
			String place, String wieloetapowe) {
		Vector<Map<String, Object>> result = new Vector<Map<String, Object>>();
		
			String user_id = getUserIdByToken(token);
			if(user_id == null)
			{
				Map<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("code", "230");
				tmp.put("content", actionForbidden);
				result.add(tmp);
				return result;
			}
				
		try{
			BoolQueryBuilder boolQuery = new BoolQueryBuilder();
			
			boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
			if(!type.isEmpty())
			{
				String[] parts = type.split(" ");
				for(String part : parts)
				boolQuery.must(QueryBuilders.wildcardQuery("TYP", "*"+part.toLowerCase()+"*"));
			}

			if(!name.isEmpty())
			{
				String[] parts = name.split(" ");
				for(String part : parts)
					boolQuery.must(QueryBuilders.wildcardQuery("NAME", "*"+part.toLowerCase()+"*"));
			}
			
			if(!place.isEmpty())
			{
				String[] parts = place.split(" ");
				for(String part : parts)
					boolQuery.must(QueryBuilders.wildcardQuery("MIEJSCOWOSC", "*"+part.toLowerCase()+"*"));
			}
			if(!wieloetapowe.isEmpty())
			{
				if(wieloetapowe.length()==1) {
					boolQuery.should(QueryBuilders.wildcardQuery("WIELOETAPOWE", "0")).should(QueryBuilders.wildcardQuery("WIELOETAPOWE", "1")).minimumNumberShouldMatch(1);
				}
				else if(wieloetapowe.length()>1)
					boolQuery.must(QueryBuilders.matchQuery("WIELOETAPOWE", wieloetapowe));
			}

			
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("competition")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.addSort("DATA_ROZP", SortOrder.ASC)
				.addSort("CZAS_ROZP", SortOrder.ASC)
				.setFrom(0)
				.setSize(10000)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
			 result.add(utility.addId(hit, "COMPETITION_ID"));
		 }
		return result;	
		}catch(Throwable e)
		{
			return null;
		}	
	}

	public static Map<String, Object> getCompetition(String id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", id).execute().actionGet();
		if(getResponse.isExists())
		{
			Map<String, Object> source = utility.addId(getResponse, "COMPETITION_ID");			
			return source;
		}
		else
			return null;
	}

	public static message addToCompetition(String token, String competition_id, String category_name) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserExist(user_id))
			return new message("215", "Uzytkownik nie istnieje");
		if(!checkIfCompetitionsExist(competition_id))
			return new message("215", "Zawody nie istnieja");
		if(!checkIfComActive(competition_id))
			return new message("215", "Nie mozna zapisac sie na nieaktywne zawody");
		if(checkIfAlreadySigned(user_id, competition_id)==false)
		{
			boolean notFull = checkFreePlaces(competition_id);
			if(checkIfAlreadyHadPlace(competition_id, "competition") == false)
			{
				if(notFull == true)
				{
					client.prepareIndex("projektzespolowy", "event").setSource(signForCompetition(user_id, competition_id, category_name, token)).execute().actionGet().getId();
					return new message("200", "Ok");
				}
				else
				{
					return new message("215", "Brak wolnych miejsc");
				}
			}
			else
				return new message("215", "Zawody juz sie odbyly");
		}
		else
			return new message("215", "Juz zapisany na te zawody");
	}

	public static message makePayed(String token, String competition_id, String user_id) {
		String owner_id = getUserIdByToken(token);
		if(owner_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserOwnsCompetition(competition_id, owner_id))
				return new message("215", "You don't own this competition");

        String event_id = findEvent(user_id, competition_id);

        if(event_id != null)
        {
	        try {
				client.prepareUpdate("projektzespolowy", "event", event_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("OPLACONE", "tak")
				                    		.endObject())
				                    .get();
				
				return new message("200", "ok");
			} catch (IOException e) {
				return new message("215", "Something went wrong");
			}
        }
        else
        	return new message("215", "No such record");
	}

	public static message deletePart(String token, String competition_id, String user_id) {
		String owner_id = getUserIdByToken(token);
		if(owner_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserOwnsCompetition(competition_id, owner_id))
			return new message("215", "You don't own this competition");
		
		String event_id = findEvent(user_id, competition_id);
		
		if(event_id != null)
		{
			if(checkIfAlreadyHadPlace(event_id, "event") == false)
			{
				String user_mail = getEmailByEvent(event_id);
				String com_name = getCompetitionNameByEvent(event_id);
				DeleteResponse delete = client.prepareDelete("projektzespolowy", "event", event_id).execute().actionGet();
				if(delete.isFound())
				{
					mailSender.initialization();
					mailSender.sendMail(user_mail, "Usuniecie z zawodow", "Witaj.<br>Zostałeś usunięty z listy uczestników zawodów: " + com_name);
					return new message("200", "Ok");
				}
				else
					return new message("215", "Unable to find document.");
			}
			else
			{
				return new message("215", "Zawody juz sie odbyly");
			}
		}
		else
			return new message("215", "No such record");
		
	}

	public static message leavePart(String competition_id, String token) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		String event_id = findEvent(user_id, competition_id);
		
		if(event_id != null)
		{
			if(checkIfAlreadyHadPlace(event_id, "event") == false)
			{
				DeleteResponse delete = client.prepareDelete("projektzespolowy", "event", event_id).execute().actionGet();
				if(delete.isFound())
				{
					return new message("200", "Ok");
				}
				else
					return new message("215", "Unable to find document.");
			}
			else
				{
					return new message("215", "Zawody juz sie odbyly");
				}
		}
		else
			return new message("215", "No such record");
		
	}

	public static message sendNewPassword(String login, String email) {
		if(checkIfExist(email, login))
		{
			String user_id = getUserIdByLogin(login);
			String code = addCodeForPasswordRecorvery(user_id);
			mailSender.initialization();
			mailSender.sendMail(email, "Odzyskanie hasła", "Witaj " + login + "<br>By wygenerować nowe hasło kliknij w link poniżej:<br>" + "http://209785serwer.iiar.pwr.edu.pl/Rest/rest/recorvery/user?code=" + code);
			return new message("200", "Email sent");
		}
		else
			return new message("215", "Wrong login or email");
	}

	public static message deleteAccount(String token, String password) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserExist(user_id, password))
			return new message("215", "Wrong password");
		
		deactivateUser(user_id);
		
		return new message("200", "Account deleted");
	}
	
	private static boolean checkIfAlreadyHadPlace(String id, String what) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", what, id).execute().actionGet();
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
		 String date = getResponse.getSource().get("DATA_ROZP").toString();	
		 date = date + " " + getResponse.getSource().get("CZAS_ROZP").toString();
		 DateTime eventDate = formatter.parseDateTime(date);
		 
			if(eventDate.isBeforeNow())
			{
				return true;
			}
			else
				return false;			
}

	public static message deactivateCompetition(String token, String competition_id, String reason) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(checkIfCompetitionsExist(competition_id))
		{
			if(checkIfUserOwnsCompetition(competition_id, user_id))
			{
				return deactivateCom(competition_id, reason);
			}
			else
				return new message("215", "You don't own this competition");
		}
		else
			return new message("215", "Competitions doesn't exist");
	}

	private static message deactivateCom(String competition_id, String reason) {
		sendMails(competition_id, reason);
		return setInactive(competition_id, reason);
	}

	private static message setInactive(String competition_id, String reason) {
        try {
			client.prepareUpdate("projektzespolowy", "competition", competition_id) 
			                    .setDoc(jsonBuilder()
			                    		.startObject()
			                    			.field("DEACTIVATED", "true")
			                    			.field("REASON", reason)
			                    		.endObject())
			                    .get();
			
			return new message("200", "Competition deactivated");
		} catch (IOException e) {
			return new message("215", "Something went wrong. Conntact with administration");
		}
		
	}

	private static void sendMails(String competition_id, String reason) {
		
		
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id))
				.setSize(10000).setFrom(0)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		 
		mailSender.initialization();
		 for (SearchHit hit : results) {
			 mailSender.sendMail(hit.getSource().get("EMAIL").toString(), "Zawody: \"" + hit.getSource().get("NAME").toString() + "\" odwolane", "Zawody zostaly odwolane przez organizatora z powodu: " + reason);
		 }
	}

	private static boolean checkIfUserOwnsCompetition(String competition_id, String user_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("USER_ID").toString().equals(user_id))
				return true;
			else
				return false;
		}
		return false;
	}
	
	private static boolean checkIfComActive(String competition_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("DEACTIVATED")!=null)
				return false;
			else
				return true;
		}
		return false;
	}
	
	public static String getCompetitionCreator(String competition_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		
		return getResponse.getSource().get("USER_ID").toString();
	}

	public static message assignNumber(String token, String user_to_number_id, String competition_id, String number) {
		
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!user_id.equals(getCompetitionCreator(competition_id)))
			return new message("215", "Not authorized action");
		
		String event_id = findEvent(user_to_number_id, competition_id);
		if(event_id == null)
			return new message("215", "No such event");
		
		if(checkIfNumberFree(competition_id, number))
		{
	        try {
				client.prepareUpdate("projektzespolowy", "event", event_id) 
				                    .setDoc(jsonBuilder()
				                    		.startObject()
				                    			.field("EVENT_NR", number)
				                    		.endObject())
				                    .get();
				
				return new message("200", "Number set");
			} catch (IOException e) {
				return new message("215", "Something went wrong. Conntact with administration");
			}
		}
		else
			return new message("215", "Number already assign");
	}

	private static boolean checkIfNumberFree(String competition_id, String number) {
		
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("EVENT_NR", number));
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		long quantity = response.getHits().getTotalHits();
		if(quantity > 0)
			return false;
		
		else
			return true;
	}
	
	public static message addCategory(String token, String competition_id, String name, String description)
	{
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserOwnsCompetition(competition_id, user_id))
			return new message("215", "Action not authorized");
		
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		boolQuery.must(QueryBuilders.matchQuery("NAME", name));
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("category")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		long count = response.getHits().getTotalHits();
		if (count > 0)
			return new message("215", "Category with this name already exist");
		
		client.prepareIndex("projektzespolowy", "category").setSource(createCategory(competition_id, name, description)).execute().actionGet().getId();
		
		return new message("200", "Category successfully added");
	}
	
	private static Map<String, Object> createCategory(String id, String name, String desc) {
		Map<String, Object> jsonDoc = new HashMap<String, Object>();
		
		jsonDoc.put("COMPETITION_ID", id);	
		jsonDoc.put("NAME", name);
		jsonDoc.put("DESCRIPTION", desc);
		
		return jsonDoc;
	}

	public static Vector<Map<String, Object>> getCategories(String competition_id) {
		Vector<Map<String, Object>> result = new Vector<Map<String, Object>>();

		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("category")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id))
				.setSize(1000).setFrom(0)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
				 Map<String, Object> foo = new HashMap<String, Object>();
				 foo.put("NAME", hit.getSource().get("NAME").toString());
				 foo.put("DESCRIPTION", hit.getSource().get("DESCRIPTION").toString());
				 result.add(foo);
			 }
		return result;
	}

	public static message changeCategory(String competition_id, String token, String user_id, String category) {
		String owner_id = getUserIdByToken(token);
		if(owner_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserOwnsCompetition(competition_id, owner_id))
			return new message("215", "Action not authorized");
		
		String event_id = checkIfSignedReturnId(user_id, competition_id);
		if(event_id==null)
			return new message("215", "Not such user on event");
		
        try {
			client.prepareUpdate("projektzespolowy", "event", event_id) 
			                    .setDoc(jsonBuilder()
			                    		.startObject()
			                    			.field("CATEGORY", category)
			                    		.endObject())
			                    .get();
			
			return new message("200", "Category changed");
		} catch (IOException e) {
			return new message("215", "Something went wrong. Conntact with administration");
		}
	}

	public static message addRoute(String token, String competition_id, List<String> points) {
		
		String owner_id = getUserIdByToken(token);
		if(owner_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfCompetitionsExist(competition_id))
			return new message("215", "No such competitions");
		if(!checkIfUserOwnsCompetition(competition_id, owner_id))
			return new message("215", "Action not authorized");
		
		String route_id = getRouteByCompetitionId(competition_id);
		if(route_id == null)
		{
			route_id = client.prepareIndex("projektzespolowy", "route").setSource(createRoute()).execute().actionGet().getId();
			try {
				client.prepareUpdate("projektzespolowy", "competition", competition_id) 
				.setDoc(jsonBuilder()
						.startObject()
							.field("ROUTE_ID", route_id)
						.endObject())
				.get();
			} catch (IOException e) {
				return new message("215", e.getMessage());
			}
		}
		else
		{
				client.prepareDelete("projektzespolowy", "route", route_id).execute().actionGet();
				route_id = client.prepareIndex("projektzespolowy", "route").setSource().execute().actionGet().getId();
				try {
					client.prepareUpdate("projektzespolowy", "competition", competition_id) 
					.setDoc(jsonBuilder()
							.startObject()
								.field("ROUTE_ID", route_id)
							.endObject())
					.get();
				} catch (IOException e) {
					return new message("215", e.getMessage());
				}
		}
		
		XContentBuilder jBuilder;
		try {
			jBuilder = jsonBuilder();
		
		jBuilder.startObject();
		int counter = 0;
		for(int i = 0; i<points.size(); i++)
		{
			if(i == 0)
				jBuilder.field("START1x", points.get(i));
			else if(i == 1)
				jBuilder.field("START1y", points.get(i));
			else if(i == 2)
				jBuilder.field("START2x", points.get(i));
			else if(i == 3)
				jBuilder.field("START2y", points.get(i));
			else if(i == points.size()-4)
				jBuilder.field("META1x", points.get(i));
			else if(i == points.size()-3)
				jBuilder.field("META1y", points.get(i));
			else if(i == points.size()-2)
				jBuilder.field("META2x", points.get(i));
			else if(i == points.size()-1)
				jBuilder.field("META2y", points.get(i));
			else if(i%4==0)
				jBuilder.field("PUNKT"+counter+"Ax", points.get(i));
			else if(i%4==1)
				jBuilder.field("PUNKT"+counter+"Ay", points.get(i));
			else if(i%4==2)
				jBuilder.field("PUNKT"+counter+"Bx", points.get(i));
			else
			{
				jBuilder.field("PUNKT"+counter+"By", points.get(i));
				counter++;
			}
		}
		
		jBuilder.field("COUNT", counter);
		jBuilder.endObject();
		
		client.prepareUpdate("projektzespolowy", "route", route_id) 
        	.setDoc(jBuilder)
        	.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		return new message("200", "Route track saved");
	}

	private static String getRouteByCompetitionId(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("ROUTE_ID") != null)
				return getResponse.getSource().get("ROUTE_ID").toString();
			else
				return null;
		}
		else 
			return null;
	}
	
	private static String getRoutePOIIDByCompetitionId(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("ROUTEPOI_ID") != null)
				return getResponse.getSource().get("ROUTEPOI_ID").toString();
			else
				return null;
		}
		else 
			return null;
	}
	
	private static String getTrackIDByCompetitionId(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("TRACK_ID") != null)
				return getResponse.getSource().get("TRACK_ID").toString();
			else
				return null;
		}
		else 
			return null;
	}

	private static String getClassificationIDByCompetitionId(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("CLASSIFICATION_ID") != null)
				return getResponse.getSource().get("CLASSIFICATION_ID").toString();
			else
				return null;
		}
		else
			return null;
	}

	public static Map<String, Object> getRoute(String competition_id) {
		if(!checkIfCompetitionsExist(competition_id))
		{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "No such competitions");
			return res;
		}
		String route_id = getRouteByCompetitionId(competition_id);
		if(route_id==null)
			{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "Route is broken. Contact with administrator");
			return res;
			}
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "route", route_id).execute().actionGet();
		if(getResponse.isExists())
		{
				return getResponse.getSource();
		}
		return null;
	}

	public static message addPOI(String token, String competition_id, List<String> points) {
		String owner_id = getUserIdByToken(token);
		if(owner_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfCompetitionsExist(competition_id))
			return new message("215", "No such competitions");
		
		if(!checkIfUserOwnsCompetition(competition_id, owner_id))
			return new message("215", "Action not authorized");
		
		String routePOI_id = getRoutePOIIDByCompetitionId(competition_id);
		if(routePOI_id == null)
		{
		routePOI_id = client.prepareIndex("projektzespolowy", "routepoi").setSource().execute().actionGet().getId();
			try {
				client.prepareUpdate("projektzespolowy", "competition", competition_id) 
				.setDoc(jsonBuilder()
						.startObject()
							.field("ROUTEPOI_ID", routePOI_id)
						.endObject())
				.get();
			} catch (IOException e) {
				return new message("215", e.getMessage());
			}
		}
		else
		{
				client.prepareDelete("projektzespolowy", "routepoi", routePOI_id).execute().actionGet();
				routePOI_id = client.prepareIndex("projektzespolowy", "routepoi").setSource().execute().actionGet().getId();
				try {
					client.prepareUpdate("projektzespolowy", "competition", competition_id) 
					.setDoc(jsonBuilder()
							.startObject()
								.field("ROUTEPOI_ID", routePOI_id)
							.endObject())
					.get();
				} catch (IOException e) {
					return new message("215", e.getMessage());
				}
		}
		
		XContentBuilder jBuilder;
		try {
			jBuilder = jsonBuilder();
		
		jBuilder.startObject();
		int counter = 0;
		for(int i = 0; i<points.size(); i++)
		{
			if(i%3==0)
				jBuilder.field("POINT_POIX" + counter, points.get(i));
			else if(i%3==1)
				jBuilder.field("POINT_POIY" + counter, points.get(i));
			else
			{
				jBuilder.field("POINT_POINAME" + counter, points.get(i));
				counter++;
			}
		}
		
		jBuilder.field("COUNT", counter);
		jBuilder.endObject();
		
		client.prepareUpdate("projektzespolowy", "routepoi", routePOI_id) 
        	.setDoc(jBuilder)
        	.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		return new message("200", "POIs saved");
	}
	
	public static Map<String, Object> getPOI(String competition_id) {
		if(!checkIfCompetitionsExist(competition_id))
		{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "No such competitions");
			return res;
		}
		
		String routePOI_id = getRoutePOIIDByCompetitionId(competition_id);
		if(routePOI_id==null)
			{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "POIs are broken. Contact with administrator");
			return res;
			}
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "routepoi", routePOI_id).execute().actionGet();
		if(getResponse.isExists())
		{
				return getResponse.getSource();
		}
		return null;
	}

	public static message addTrack(String token, String competition_id, List<String> points) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfCompetitionsExist(competition_id))
			return new message("215", "No such competitions");
		
		if(!checkIfUserOwnsCompetition(competition_id, user_id))
			return new message("215", "Action not authorized");
		
		String track_id = getTrackIDByCompetitionId(competition_id);
		if(track_id == null)
		{
			track_id = client.prepareIndex("projektzespolowy", "track").setSource().execute().actionGet().getId();
			try {
				client.prepareUpdate("projektzespolowy", "competition", competition_id) 
				.setDoc(jsonBuilder()
						.startObject()
							.field("TRACK_ID", track_id)
						.endObject())
				.get();
			} catch (IOException e) {
				return new message("215", e.getMessage());
			}
		}
		else
		{
				client.prepareDelete("projektzespolowy", "track", track_id).execute().actionGet();
				track_id = client.prepareIndex("projektzespolowy", "track").setSource().execute().actionGet().getId();
				try {
					client.prepareUpdate("projektzespolowy", "competition", competition_id) 
					.setDoc(jsonBuilder()
							.startObject()
								.field("TRACK_ID", track_id)
							.endObject())
					.get();
				} catch (IOException e) {
					return new message("215", e.getMessage());
				}
		}
		
		XContentBuilder jBuilder;
		try {
			jBuilder = jsonBuilder();
		
		jBuilder.startObject();
		int counter = 0;
		for(int i = 0; i<points.size(); i++)
		{
			if(i%2==0)
				jBuilder.field("POINTX" + counter, points.get(i));
			else
			{
				jBuilder.field("POINTY" + counter, points.get(i));
				counter++;
			}
		}
		
		jBuilder.field("COUNT", counter);
		jBuilder.endObject();
		
		client.prepareUpdate("projektzespolowy", "track", track_id) 
        	.setDoc(jBuilder)
        	.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		return new message("200", "Track saved");
	}

	public static Map<String, Object> getTrack(String competition_id) {
		if(!checkIfCompetitionsExist(competition_id))
		{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "No such competitions");
			return res;
		}
		
		String track_id = getTrackIDByCompetitionId(competition_id);
		if(track_id==null)
			{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "Track is broken. Contact with administrator");
			return res;
			}
		
		GetResponse getResponse = client.prepareGet("projektzespolowy", "track", track_id).execute().actionGet();
		if(getResponse.isExists())
		{
				return getResponse.getSource();
		}
		return null;
	}

	public static Vector<Map<String, Object>> getAllGps(String competition_id) {
		Vector<Map<String, Object>> ret = new Vector<Map<String, Object>>();
		ret.add(getRoute(competition_id));
		ret.add(getPOI(competition_id));
		ret.add(getTrack(competition_id));
		return ret;
	}
	public static message makeClassification(String token, String competition_id, String typ, List<String> points, List<String> timebonus) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);

		if(!checkIfCompetitionsExist(competition_id))
			return new message("215", "No such competitions");

		if(!checkIfUserOwnsCompetition(competition_id, user_id))
			return new message("215", "Action not authorized");

		String classification_id = getClassificationIDByCompetitionId(competition_id);
		if(classification_id == null)
		{
			classification_id = client.prepareIndex("projektzespolowy", "classification").setSource().execute().actionGet().getId();
			try {
				client.prepareUpdate("projektzespolowy", "competition", competition_id)
						.setDoc(jsonBuilder()
								.startObject()
								.field("CLASSIFICATION_ID", classification_id)
								.endObject())
						.get();
			} catch (IOException e) {
				return new message("215", e.getMessage());
			}
		}
		else
		{
			client.prepareDelete("projektzespolowy", "classification", classification_id).execute().actionGet();
			classification_id = client.prepareIndex("projektzespolowy", "classification").setSource().execute().actionGet().getId();
			try {
				client.prepareUpdate("projektzespolowy", "competition", competition_id)
						.setDoc(jsonBuilder()
								.startObject()
								.field("CLASSIFICATION_ID", classification_id)
								.endObject())
						.get();
			} catch (IOException e) {
				return new message("215", e.getMessage());
			}
		}

		XContentBuilder jBuilder;
		try {
			jBuilder = jsonBuilder();

			jBuilder.startObject();
			jBuilder.field("TYP", typ);
			for(int i = 0; i<points.size(); i++)
			{
					String pkt = points.get(i);
					jBuilder.field("LINIA" + pkt.substring(0, pkt .indexOf("_"))+"_POINT_"+pkt.substring(pkt.indexOf("_")+1, pkt.lastIndexOf("_")), pkt.substring(pkt.lastIndexOf("_")+1, pkt.length()));
			}
			for(int i = 0; i<points.size(); i++)
			{
				String pkt = timebonus.get(i);
				jBuilder.field("LINIA" + pkt.substring(0, pkt .indexOf("_"))+"_BONUSTIME_"+pkt.substring(pkt.indexOf("_")+1, pkt.lastIndexOf("_")), pkt.substring(pkt.lastIndexOf("_")+1, pkt.length()));
			}
			//jBuilder.field("COUNT", counter);
			jBuilder.endObject();

			client.prepareUpdate("projektzespolowy", "classification", classification_id)
					.setDoc(jBuilder)
					.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		return new message("200", "Classification added");
	}

	public static Map<String, Object> getClassification(String competition_id) {
		if(!checkIfCompetitionsExist(competition_id))
		{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "No such competitions");
			return res;
		}

		String classification_id = getClassificationIDByCompetitionId(competition_id);
		if(classification_id==null)
		{
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("CODE", "215");
			res.put("content", "Classification is broken. Contact with administrator");
			return res;
		}

		GetResponse getResponse = client.prepareGet("projektzespolowy", "classification", classification_id).execute().actionGet();
		if(getResponse.isExists())
		{
			return getResponse.getSource();
		}
		return null;
	}

	public static message setTimeAtPoint(String competition_id, String token, List<String> point_nr, List<String> time) {
		
								//UNCOMENT AFTER ADD START TIME TO CREATOR
		
		/*if(!checkIfCompetitionStartedAlready(competition_id))
			return new message("215", "Competition not started yet");*/
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfCompetitionsExist(competition_id))
			return new message("215", "Competition don't exist");
		if(!checkIfAlreadySigned(user_id, competition_id))
			return new message("215", "User not signed for this competition");
		if(!checkIfUserReady(competition_id, user_id))
			return new message("215", "User not ready");
		
		String event_id = getEventIdByComIdUserId(competition_id, user_id);
		
		
		XContentBuilder jBuilder;
		try {
			jBuilder = jsonBuilder();
		
			jBuilder.startObject();
			for(int i = 0; i<point_nr.size(); i++)
			{
				jBuilder.field("POINT"+point_nr.get(i)+"_TIME", time.get(i));
			}
			jBuilder.endObject();

		
			client.prepareUpdate("projektzespolowy", "event", event_id) 
			.setDoc(jBuilder)
			.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		
		return new message("200", "Ok.");
	}

	private static boolean checkIfUserReady(String competition_id, String user_id) {
		String event_id = getEventIdByComIdUserId(competition_id, user_id);
		GetResponse getResponse = client.prepareGet("projektzespolowy", "event", event_id).execute().actionGet();
		if(getResponse.isExists())
		{
				if(getResponse.getSource().get("USER_READY") == null)
					return false;
				else
					return true;
		}
		else
			return false;
		
	}

	/*private static boolean checkIfCompetitionStartedAlready(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
				if(getResponse.getSource().get("STARTED") == null)
					return false;
				
				String startTime = getResponse.getSource().get("STARTED").toString();
				DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
				String date = getResponse.getSource().get("DATA_ROZP").toString();	
				date = date + " " + startTime;
				DateTime eventDate = formatter.parseDateTime(date);
					 
					 
				if(eventDate.isBeforeNow())
				{
					return true;
				}
				else
					return false;
		}
		else
		return false;
	}*/
	
	private static boolean checkIfActualTimeIsBeforeComStartTime(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
				if(getResponse.getSource().get("STARTED") == null)
					return false;
				
				String startTime = getResponse.getSource().get("STARTED").toString();
				DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
				String date = getResponse.getSource().get("DATA_ROZP").toString();	
				date = date + " " + startTime;
				DateTime eventDate = formatter.parseDateTime(date);
					 
					 
				if(eventDate.isAfterNow())
				{
					return true;
				}
				else
					return false;
		}
		else
		return false;
	}
	
	private static boolean checkIfCompetitionStarted(String competition_id)
	{
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("STARTED") == null)
				return false;
			else
				return true;
		}
		else
		return false;
	}

	private static String getEventIdByComIdUserId(String competition_id, String user_id) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
		SearchResponse response = client.prepareSearch("projektzespolowy")
				.setTypes("event")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)
				.setSize(1).setFrom(0)
				.execute()
				.actionGet();
		
		return response.getHits().getHits()[0].getId().toString();
	}

	public static message setCompetitionStart(String competition_id, String token, String time) {
		String owner_id = getUserIdByToken(token);
		if(owner_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfUserOwnsCompetition(competition_id, owner_id))
			return new message("215", "Action not authorized");
		
		try {
			client.prepareUpdate("projektzespolowy", "competition", competition_id) 
			.setDoc(jsonBuilder()
					.startObject()
						.field("STARTED", time)
					.endObject())
			.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		
		return new message("200", "Start set.");		
	}

	public static message setUserStarted(String competition_id, String token) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		if(!checkIfAlreadySigned(user_id, competition_id))
			return new message("215", "Your are not signed for this competition");
		if(!checkIfComActive(competition_id))
			return new message("215", "Competition disactivated");
		if(!checkIfCompetitionStarted(competition_id))
			return new message("215", "Competition don't have start time set yet");
		if(!checkIfActualTimeIsBeforeComStartTime(competition_id))
			return new message("215", "Competition already started");
		if(!checkIfUserHaveNumber(user_id, competition_id))
			return new message("215", "Number not set");
		
		String event_id = getEventIdByComIdUserId(competition_id, user_id);
		
		
		try {
			client.prepareUpdate("projektzespolowy", "event", event_id) 
			.setDoc(jsonBuilder()
					.startObject()
						.field("USER_READY", "true")
					.endObject())
			.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		
		return new message("200", "User set as ready.");		
	}

	private static boolean checkIfUserHaveNumber(String user_id, String competition_id) {
			BoolQueryBuilder boolQuery = new BoolQueryBuilder();
			boolQuery.must(QueryBuilders.matchQuery("USER_ID", user_id));
			boolQuery.must(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
			
			
			SearchResponse response = client.prepareSearch("projektzespolowy")
					.setTypes("event")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(boolQuery)
					.setSize(1).setFrom(0)
					.execute()
					.actionGet();
			Object nr = response.getHits().getHits()[0].getSource().get("EVENT_NR");
			if(nr!=null)
			{
				if(!nr.toString().equals(""))
					return true;
				else
					return false;
			}
			else
			return false;
	}

	public static Vector<Map<String, Object>> getCompetitionResults(String competition_id) {
		if(checkIfListPublished(competition_id))
			return getPublishedList(competition_id);
		else
		{
			return getUnpublishedList(competition_id);
		}
	}

	private static Vector<Map<String, Object>> getUnpublishedList(String competition_id) {
		if(!checkIfAnyoneTimed(competition_id))
			return null;
		Vector<Map<String, Object>> ret = new Vector<Map<String, Object>>();
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("OFFICIAL", "false");
		

		
		int count;
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("ROUTE_ID") == null)
				return null;
			else
			{
				GetResponse gResponse = client.prepareGet("projektzespolowy", "route", getResponse.getSource().get("ROUTE_ID").toString()).execute().actionGet();
				count = Integer.parseInt(gResponse.getSource().get("COUNT").toString());
			}
		}
		else
			return null;
		res.put("POINTS_COUNT", count+1);
		ret.add(res);
		
		SearchRequestBuilder req = client.prepareSearch("projektzespolowy");
		
		req.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		req.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id));
		req.setSize(1000).setFrom(0);
		
		
		for(int i = count+1; i>0; i--)
		req.addSort("POINT"+i+"_TIME", SortOrder.ASC);

		
		SearchResponse response = req.execute().actionGet();

		
		SearchHit[] results = response.getHits().getHits();
		 
		 for (SearchHit hit : results) {
			 ret.add(utility.addId(hit, "EVENT_ID"));
		 }
		 
		 return ret;
	}

	private static boolean checkIfAnyoneTimed(String competition_id) {
		
			SearchResponse response = client.prepareSearch("projektzespolowy")
					.setTypes("event")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id))
					.setQuery(QueryBuilders.existsQuery("POINT1_TIME"))
					.setSize(0).setFrom(0)
					.execute()
					.actionGet();
			
			
			if(response.getHits().getTotalHits() > 0)
				return true;
			else 
				return false;
	}

	private static Vector<Map<String, Object>> getPublishedList(String competition_id) {
		Vector<Map<String, Object>> ret = new Vector<Map<String, Object>>();
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("OFFICIAL", "true");
		
		int count;
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("ROUTE_ID") == null)
				return null;
			else
			{
				GetResponse gResponse = client.prepareGet("projektzespolowy", "route", getResponse.getSource().get("ROUTE_ID").toString()).execute().actionGet();
				count = Integer.parseInt(gResponse.getSource().get("COUNT").toString());
			}
		}
		else
			return null;
		res.put("POINTS_COUNT", count+1);
		ret.add(res);
		
		
			SearchResponse response = client.prepareSearch("projektzespolowy")
					.setTypes("result")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.matchQuery("COMPETITION_ID", competition_id))
					.addSort("MIEJSCE", SortOrder.ASC)
					.setSize(1000).setFrom(0)
					.execute()
					.actionGet();
			
			SearchHit[] results = response.getHits().getHits();
			 
			 for (SearchHit hit : results) {
				 ret.add(utility.addId(hit, "EVENT_ID"));
			 }
			 
			 return ret;
	}

	private static boolean checkIfListPublished(String competition_id) {
		GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();
		if(getResponse.isExists())
		{
			if(getResponse.getSource().get("RESULT_LIST") == null)
				return false;
			else
				return true;
		}
		else
			return false;
	}

	public static message publishList(String token, String competition_id, List<String> parts,
			List<String> banned) {
		
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
			
		try {
			client.prepareUpdate("projektzespolowy", "competition", competition_id) 
			.setDoc(jsonBuilder()
					.startObject()
						.field("RESULT_LIST", "true")
					.endObject())
			.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		
		for(int i = 0; i<parts.size(); i++)
		{
			GetResponse getResponse = client.prepareGet("projektzespolowy", "event", parts.get(i)).execute().actionGet();
			if(!getResponse.isExists())
			{
				return new message ("215", "No such user on competitions");
			}
			if(banned.contains(parts.get(i)))
				client.prepareIndex("projektzespolowy", "result").setSource(createResultEnity(-1, competition_id, parts.get(i), 1)).execute().actionGet().getId();
			else
				client.prepareIndex("projektzespolowy", "result").setSource(createResultEnity(i+1, competition_id, parts.get(i), 0)).execute().actionGet().getId();
		}
		
		return new message("200", "OK");
		
	}

	private static Map<String, Object> createResultEnity(int poz, String competition_id, String event_id, int ban) {
			Map<String, Object> jsonDoc = new HashMap<String, Object>();


			
			GetResponse getResponse = client.prepareGet("projektzespolowy", "event", event_id).execute().actionGet();
			if(getResponse.isExists())
			{
				jsonDoc.putAll(getResponse.getSource());
			}

			jsonDoc.put("MIEJSCE", poz);
			if(ban==1)
				jsonDoc.put("BANNED", "true");
			else
				jsonDoc.put("BANNED", "false");	
			jsonDoc.put("EVENT_ID", event_id);
			jsonDoc.put("COMPETITION_ID", competition_id);

			
			return jsonDoc;
		}

	public static message banUserBySystem(String token, String competition_id) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		String event_id = getEventIdByComIdUserId(competition_id, user_id);
		
		try {
			client.prepareUpdate("projektzespolowy", "event", event_id) 
			.setDoc(jsonBuilder()
					.startObject()
						.field("BANNED", "true")
					.endObject())
			.get();
		} catch (IOException e) {
			return new message("215", e.getMessage());
		}
		
		return new message("200", "YOU GOT BANNED!");
	}

	public static message checkIfPart(String token, String competition_id) {
		String user_id = getUserIdByToken(token);
		if(user_id == null)
			return new message("230", actionForbidden);
		
		String event_id = getEventIdByComIdUserId(competition_id, user_id);
		GetResponse getResponse = client.prepareGet("projektzespolowy", "event", event_id).execute().actionGet();
		
		if(getResponse.getSource().get("POINT1_TIME") == null)
			return new message("200", "Ok!");	
		else
			return new message("215", "Already timed. Action forbidden");
		
	}



	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/
	/******************************  Praca inzynierka ***************************/
	/****************************************************************************/
	/****************************************************************************/
	/****************************************************************************/

	public static void actualPos(String competition_id, String token, String len, String lgt, String distance)
	{
        String user_id = getUserIdByToken(token);
        if(user_id == null)
            return;

		String event_id = getEventIdByComIdUserId(competition_id, user_id);

        if(event_id==null)
            return;

        GetResponse getResponse = client.prepareGet("projektzespolowy", "competition", competition_id).execute().actionGet();

        String refr;
        if((refr = getResponse.getSource().get("refresh_time").toString()) == null)
            return;




        //calculate speed
        Float speed = Float.parseFloat(distance) / Float.parseFloat(refr);

        try {
            client.prepareUpdate("projektzespolowy", "event", event_id)
                    .setDoc(jsonBuilder()
                            .startObject()
                            .field("actual_len", len)
                            .field("actual_lgt", lgt)
                            .field("speed", speed.toString())
                            .endObject())
                    .get();
        } catch (IOException e) {
            return;
        }
	}

}