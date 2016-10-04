package controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.elastic;
import model.message;

@RestController
public class UserController {

	//ok
	@RequestMapping(value ="/rest/user", method = RequestMethod.PUT)
	public message addUser(@RequestParam(value="name") String name,
						@RequestParam(value="surname") String surname,
						@RequestParam(value="login") String login,
						@RequestParam(value="password") String password,
						@RequestParam(value="email") String email,
						@RequestParam(value="age") String age,
						@RequestParam(value="sex") String sex,
						@RequestParam(value="club") String club,
						@RequestParam(value="obywatelstwo") String obywatelstwo,
						@RequestParam(value="nr_tel") String nr_tel,
						@RequestParam(value="ice") String ice)
	{
		return elastic.addUser(name, surname, login, password, email, age, sex, club, obywatelstwo, nr_tel, ice);
	}

	@RequestMapping(value="rest/activate/user", method = RequestMethod.GET)
	public String userActivation(@RequestParam(value ="code") String code)
	{
		return elastic.userActivation(code);
	}
	
	
	@RequestMapping(value="/rest/user", method = RequestMethod.GET)
	public Map<String, Object> getUser(@RequestParam(value="id") String token)
	{
		return elastic.getUser(token);
	}
	
	
	
	@RequestMapping(value="/rest/user/login", method = RequestMethod.GET)
	public Map<String, Object> login(@RequestParam(value="login") String login,
			@RequestParam(value="password") String password)
	{
		return elastic.login(login, password);
	}
	
	@RequestMapping(value="rest/user/password", method = RequestMethod.GET)
	public message sendNewPassword(@RequestParam(value ="login") String login,
			@RequestParam(value ="email") String email)
	{
		return elastic.sendNewPassword(login, email);
	}

	@RequestMapping(value="rest/recorvery/user", method = RequestMethod.GET)
	public String passwordRecorvery(@RequestParam(value ="code") String code)
	{
		return elastic.generateNewPassword(code);
	}
	
	@RequestMapping(value="/rest/user/password", method = RequestMethod.POST)
	public message changePassword(@RequestParam(value="user_id") String token,
			@RequestParam(value="old_password") String old_password,
			@RequestParam(value="new_password") String new_password)
	{
		
		return elastic.changePassword(token, old_password, new_password);
	}
	
	@RequestMapping(value="/rest/user/delete", method = RequestMethod.DELETE)
	public message deleteAccount(@RequestParam(value="user_id") String token,
			@RequestParam(value="password") String password)
	{
		return elastic.deleteAccount(token, password);

	}
	
	@RequestMapping(value="/rest/user", method = RequestMethod.POST)
	public message updateProfile(@RequestParam(value="user_id") String token,
			@RequestParam(value="name") String name,
			@RequestParam(value="surname") String surname,
			@RequestParam(value="email") String email,
			@RequestParam(value="age") String age,
			@RequestParam(value="club") String club,
			@RequestParam(value="nr_tel") String nr,
			@RequestParam(value="ICE") String ICE,
			@RequestParam(value="nationality") String nationality)
	{
		return elastic.updateProfile(name, surname, email, token, age, club, nr, ICE, nationality);
	}
}