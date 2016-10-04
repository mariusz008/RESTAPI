package controller;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.elastic;
import model.message;

@RestController
public class CompetitionController {
	
		@RequestMapping(value="/rest/competition", method = RequestMethod.PUT)
		public message addCompetition(@RequestParam(value="user_id") String token,
				@RequestParam(value="name") String name,
				@RequestParam(value="data_rozp") String data_rozp,
				@RequestParam(value="czas_rozp") String czas_rozp,
				@RequestParam(value="data_zak") String data_zak,
				@RequestParam(value="czas_zak") String czas_zak,
				@RequestParam(value="typ") String typ,
				@RequestParam(value="limit_ucz") String limit_ucz,
				@RequestParam(value="miejscowosc") String miejscowosc,
				@RequestParam(value="oplata") String oplata,
				@RequestParam(value="opis") String opis,
				@RequestParam(value="image") String image,
				@RequestParam(value="wieloetapowe") String wieloetapowe)
		{
			return elastic.addCompetition(token, name, data_rozp, czas_rozp, data_zak, czas_zak, typ, limit_ucz, miejscowosc, oplata, opis, image, wieloetapowe);
		}	
		
		@RequestMapping(value="/rest/competition/all", method = RequestMethod.GET)
		public Vector<Map<String, Object>> showCompetitions(@RequestParam(value="type") String type,
				@RequestParam(value="name") String name,
				@RequestParam(value="place") String place,
				@RequestParam(value="wieloetapowe") String wieloetapowe)
		{
			return elastic.showCompetitions(type, name, place, wieloetapowe);
		}
		
		@RequestMapping(value="/rest/competition/my", method = RequestMethod.GET)
		public Vector<Map<String, Object>> showMyCompetitions(@RequestParam("user_id") String token,
				@RequestParam(value="type") String type,
				@RequestParam(value="name") String name,
				@RequestParam(value="place") String place,
				@RequestParam(value="wieloetapowe") String wieloetapowe)
		{
			return elastic.showMyCompetitions(token, type, name, place, wieloetapowe);
		}
		
		@RequestMapping(value="/rest/competition", method = RequestMethod.GET)
		public Map<String, Object> getCompetition(@RequestParam(value="id") String id)
		{
			return elastic.getCompetition(id);
		}
		

		@RequestMapping(value="/rest/competition/event/list", method = RequestMethod.GET)
		public Vector<Map<String, Object>> showParticipantsFiltered(@RequestParam(value="competition_id") String competition_id,
				@RequestParam(value="sex") String sex,
				@RequestParam(value="age") String age,
				@RequestParam(value="phrase") String phrase,
				@RequestParam(value="category") String category)
		{
			return elastic.getParticipantsFiltrated(competition_id, sex, age, phrase, category);
		}
		
		@RequestMapping(value="/rest/competition", method = RequestMethod.POST)
		public message editCompetition(@RequestParam(value="user_id") String token,
				@RequestParam(value="competition_id") String competition_id,
				@RequestParam(value="name") String name,
				@RequestParam(value="data_rozp") String data_rozp,
				@RequestParam(value="czas_rozp") String czas_rozp,
				@RequestParam(value="data_zak") String data_zak,
				@RequestParam(value="czas_zak") String czas_zak,
				@RequestParam(value="typ") String typ,
				@RequestParam(value="limit_ucz") String limit_ucz,
				@RequestParam(value="miejscowosc") String miejscowosc,
				@RequestParam(value="oplata") String oplata,
				@RequestParam(value="opis") String opis,
				@RequestParam(value="wieloetapowe") String wieloetapowe)
		{
			
			return elastic.updateCompetition(token, competition_id, name, data_rozp, czas_rozp, data_zak, czas_zak, typ, limit_ucz, miejscowosc, oplata, opis, wieloetapowe);
			
		}
		
		@RequestMapping(value="/rest/competition/deactivate", method = RequestMethod.POST)
		public message deactivateCompetition(@RequestParam(value="user_id") String token,
				@RequestParam(value="competition_id") String competition_id,
				@RequestParam(value="reason") String reason)
		{
			return elastic.deactivateCompetition(token, competition_id, reason);
		}
		
		@RequestMapping(value="/rest/competition/user/number", method = RequestMethod.PUT)
		public message assignNumber(@RequestParam(value="user_id") String token,
				@RequestParam(value="user_to_number_id") String user_to_number_id,
				@RequestParam(value="competition_id") String competition_id,
				@RequestParam(value="number") String number)
		{
			return elastic.assignNumber(token, user_to_number_id, competition_id, number);
		}
		
		@RequestMapping(value="/rest/competition/category", method = RequestMethod.PUT)
		public message AddCategory(@RequestParam(value="user_id") String token,
				@RequestParam(value="competition_id") String competition_id,
				@RequestParam(value="name") String name,
				@RequestParam(value="description") String description)
		{
			return elastic.addCategory(token, competition_id, name, description);
		}
		
		@RequestMapping(value="/rest/competition/category/list", method = RequestMethod.GET)
		public Vector<Map<String, Object>> GetCategories(
				@RequestParam(value="competition_id") String competition_id)
		{
			return elastic.getCategories(competition_id);
		}
		
		@RequestMapping(value="/rest/competition/route", method = RequestMethod.POST)
		public message AddRoute(@RequestParam("owner_id") String token,
				@RequestParam("competition_id") String competition_id,
				@RequestParam("points") List<String> points)
		{
			return elastic.addRoute(token, competition_id, points);
		}
		
		@RequestMapping(value="/rest/competition/route", method = RequestMethod.GET)
		public Map<String, Object> getRoute(@RequestParam("competition_id") String competition_id)
		{
			return elastic.getRoute(competition_id);
		}
		
		@RequestMapping(value="/rest/competition/poi", method = RequestMethod.POST)
		public message AddPOI(@RequestParam("owner_id") String token,
				@RequestParam("competition_id") String competition_id,
				@RequestParam("points") List<String> points)
		{
			return elastic.addPOI(token, competition_id, points);
		}
		
		@RequestMapping(value="/rest/competition/poi", method = RequestMethod.GET)
		public Map<String, Object> getPOI(@RequestParam("competition_id") String competition_id)
		{
			return elastic.getPOI(competition_id);
		}
		
		@RequestMapping(value="/rest/competition/track", method = RequestMethod.POST)
		public message AddTrack(@RequestParam("owner_id") String token,
				@RequestParam("competition_id") String competition_id,
				@RequestParam("points") List<String> points)
		{
			return elastic.addTrack(token, competition_id, points);
		}
		
		@RequestMapping(value="/rest/competition/track", method = RequestMethod.GET)
		public Map<String, Object> getTrack(@RequestParam("competition_id") String competition_id)
		{
			return elastic.getTrack(competition_id);
		}

		@RequestMapping(value="/rest/competition/classification", method = RequestMethod.POST)
		public message makeClassification(@RequestParam("owner_id") String token,
								@RequestParam("competition_id") String competition_id,
								@RequestParam("typ") String typ,
								@RequestParam("points") List<String> points,
								@RequestParam("timebonus") List<String> timebonus)
		{
			return elastic.makeClassification(token, competition_id, typ, points, timebonus);
		}

		@RequestMapping(value="/rest/competition/classification", method = RequestMethod.GET)
		public Map<String, Object> getClassification(@RequestParam("competition_id") String competition_id)
		{
			return elastic.getClassification(competition_id);
		}

		@RequestMapping(value="/rest/competition/gps/all", method = RequestMethod.GET)
		public Vector<Map<String, Object>> getAllGps(@RequestParam("competition_id") String competition_id)
		{
			return elastic.getAllGps(competition_id);
		}
		
		@RequestMapping(value="/rest/competition/event/time", method = RequestMethod.PUT)
		public message setTimeAtPoint(@RequestParam("competition_id") String competition_id,
				@RequestParam("user_id") String token,
				@RequestParam("point_nr") List<String> point_nr,
				@RequestParam("time") List<String> time)
		{
			return elastic.setTimeAtPoint(competition_id, token, point_nr, time);
		}
		
		@RequestMapping(value="/rest/competition/event/start", method = RequestMethod.PUT)
		public message setStart(@RequestParam("competition_id") String competition_id,
				@RequestParam("owner_id") String token,
				@RequestParam("time") String time)
		{
			return elastic.setCompetitionStart(competition_id, token, time);
		}
		
		@RequestMapping(value="/rest/result/list", method = RequestMethod.GET)
		public Vector<Map<String, Object>> getResults(@RequestParam("competition_id") String competition_id)
		{
			return elastic.getCompetitionResults(competition_id);
		}
		
		@RequestMapping(value="/rest/result/publish", method = RequestMethod.PUT)
		public message publishResults(
				@RequestParam("user_id") String token,
				@RequestParam("competition_id") String competition_id,
				@RequestParam("parts") List<String> parts,
				@RequestParam("banned") List<String> banned)
		{
			return elastic.publishList(token, competition_id, parts, banned);
		}
		
		@RequestMapping(value="rest/competition/ban", method = RequestMethod.POST)
		public message banUserBySystem(
				@RequestParam("user_id") String token,
				@RequestParam("competition_id") String competition_id)
		{
			return elastic.banUserBySystem(token, competition_id);
		}
		
		@RequestMapping(value="rest/competition/checkpart", method = RequestMethod.GET)
		public message checkIfPart(
				@RequestParam("user_id") String token,
				@RequestParam("competition_id") String competition_id)
		{
			return elastic.checkIfPart(token, competition_id);
		}
}