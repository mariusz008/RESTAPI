package controller;

import java.util.Map;
import java.util.Vector;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.elastic;
import model.message;

@RestController
public class EventController {
	
	@RequestMapping(value="/rest/competition/event", method = RequestMethod.PUT)
	public message addToCompetition(@RequestParam(value="user_id") String token,
			@RequestParam(value="competition_id") String competition_id,
			@RequestParam(value="category_name") String category_name)
	{
		return elastic.addToCompetition(token, competition_id, category_name);
	}
	
	@RequestMapping(value="/rest/competition/event/payment", method = RequestMethod.POST)
	public message makePayed(@RequestParam(value="owner_id") String token,
			@RequestParam(value="competition_id") String competition_id,
			@RequestParam(value="user_id") String user_id)
	{
		return elastic.makePayed(token, competition_id, user_id);
	}
	
	@RequestMapping(value="/rest/competition/event", method = RequestMethod.DELETE)
	public message deletePart(@RequestParam(value="owner_id") String token,
			@RequestParam(value="competition_id") String competition_id,
			@RequestParam(value="user_id") String user_id)
	{
		return elastic.deletePart(token, competition_id, user_id);
	}
	
	@RequestMapping(value="/rest/competition/event/leave", method = RequestMethod.DELETE)
	public message leavePart(@RequestParam(value="competition_id") String competition_id,
			@RequestParam(value="user_id") String token)
	{
		return elastic.leavePart(competition_id, token);
	}
	
	@RequestMapping(value="/rest/competition/user/list", method = RequestMethod.GET)
	public Vector<Map<String, Object>> showUserEvents(@RequestParam(value="user_id") String token,
			@RequestParam(value="type") String type,
			@RequestParam(value="name") String name,
			@RequestParam(value="place") String place,
			@RequestParam(value="wieloetapowe") String wieloetapowe)
	{
		return elastic.getUserEvents(token, type, name, place, wieloetapowe);
	}
	
	@RequestMapping(value="/rest/competition/event/category", method = RequestMethod.POST)
	public message changeCategory(@RequestParam(value="competition_id") String competition_id,
			@RequestParam(value="owner_id") String token,
			@RequestParam(value="user_id") String user_id,
			@RequestParam(value="category") String category)
	{
		return elastic.changeCategory(competition_id, token, user_id, category);
	}
	
	@RequestMapping(value="/rest/competition/event/user/start", method = RequestMethod.PUT)
	public message setStarted(@RequestParam(value="competition_id") String competition_id,
			@RequestParam(value="user_id") String token)
	{
		return elastic.setUserStarted(competition_id, token);
	}

	@RequestMapping(value="/rest/competition/event/user/actual", method = RequestMethod.POST)
	public void setStarted(@RequestParam(value="competition_id") String competition_id,
						   @RequestParam(value="token") String token,
						   @RequestParam(value="x") String len,
                           @RequestParam(value="y") String lgt,
                           @RequestParam(value="distance") String distance)
	{
		elastic.actualPos(competition_id, token, len, lgt, distance);
	}
	
}