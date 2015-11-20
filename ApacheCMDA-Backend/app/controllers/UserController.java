/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.util.JSONPObject;
import models.User;
import models.UserBean;
import models.UserRepository;
import play.data.Form;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import scala.util.parsing.json.JSONObject;
import views.html.user;

/**
 * The main set of web services.
 */
@Named
@Singleton
public class UserController extends Controller {

	private final UserRepository userRepository;

	// We are using constructor injection to receive a repository to support our
	// desire for immutability.
	@Inject
	public UserController(final UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Result addUser() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			System.out.println("User not created, expecting Json data");
			return badRequest("User not created, expecting Json data");
		}

		// Parse JSON file
		String userName = json.path("userName").asText();
		String password = json.path("password").asText();
		String firstName = json.path("firstName").asText();
		String lastName = json.path("lastName").asText();
		String middleInitial = json.path("middleInitial").asText();
	    String affiliation = json.path("affiliation").asText();
	    String title = json.path("title").asText();
	    String email = json.path("email").asText();
	    String mailingAddress = json.path("mailingAddress").asText();
	    String phoneNumber = json.path("phoneNumber").asText();
	    String faxNumber = json.path("faxNumber").asText();
	    String researchFields = json.path("researchFields").asText();
	    String highestDegree = json.path("highestDegree").asText();

		try {
			if (userRepository.findByUserName(userName).size()>0) {
				System.out.println("UserName has been used: " + userName);
				return badRequest("UserName has been used");
			}
			User user = new User(userName, password, firstName, lastName, middleInitial, affiliation, title, email, mailingAddress, phoneNumber, faxNumber, researchFields, highestDegree);	
			userRepository.save(user);
			System.out.println("User saved: " + user.getId());
			return created(new Gson().toJson(user.getId()));
		} catch (PersistenceException pe) {
			pe.printStackTrace();
			System.out.println("User not saved: " + firstName + " " + lastName);
			return badRequest("User not saved: " + firstName + " " + lastName);
		}
	}

	public Result deleteUser(Long id) {
		User deleteUser = userRepository.findOne(id);
		if (deleteUser == null) {
			System.out.println("User not found with id: " + id);
			return notFound("User not found with id: " + id);
		}

		userRepository.delete(deleteUser);
		System.out.println("User is deleted: " + id);
		return ok("User is deleted: " + id);
	}

	public Result updateUser(long id) {
		JsonNode json = request().body().asJson();
		if (json == null) {
			System.out.println("User not saved, expecting Json data");
			return badRequest("User not saved, expecting Json data");
		}

		// Parse JSON file
		String firstName = json.path("firstName").asText();
		String lastName = json.path("lastName").asText();
		String middleInitial = json.path("middleInitial").asText();
	    String affiliation = json.path("affiliation").asText();
	    String title = json.path("title").asText();
	    String email = json.path("email").asText();
	    String mailingAddress = json.path("mailingAddress").asText();
	    String phoneNumber = json.path("phoneNumber").asText();
	    String faxNumber = json.path("faxNumber").asText();
	    String researchFields = json.path("researchFields").asText();
	    String highestDegree = json.path("highestDegree").asText();
		try {
			User updateUser = userRepository.findOne(id);

			updateUser.setFirstName(firstName);
			updateUser.setLastName(lastName);
			updateUser.setAffiliation(affiliation);
			updateUser.setEmail(email);
			updateUser.setFaxNumber(faxNumber);
			updateUser.setHighestDegree(highestDegree);
			updateUser.setMailingAddress(mailingAddress);
			updateUser.setMiddleInitial(middleInitial);
			updateUser.setPhoneNumber(phoneNumber);
			updateUser.setResearchFields(researchFields);
			updateUser.setTitle(title);
			
			User savedUser = userRepository.save(updateUser);
			System.out.println("User updated: " + savedUser.getFirstName()
					+ " " + savedUser.getLastName());
			return created("User updated: " + savedUser.getFirstName() + " "
					+ savedUser.getLastName());
		} catch (PersistenceException pe) {
			pe.printStackTrace();
			System.out.println("User not updated: " + firstName + " "
					+ lastName);
			return badRequest("User not updated: " + firstName + " " + lastName);
		}
	}

	public Result getUser(Long id, String format) {
		if (id == null) {
			System.out.println("User id is null or empty!");
			return badRequest("User id is null or empty!");
		}

		User user = userRepository.findOne(id);

		if (user == null) {
			System.out.println("User not found with with id: " + id);
			return notFound("User not found with with id: " + id);
		}
		String result = new String();
		if (format.equals("json")) {
			result = new Gson().toJson(user);
		}

		return ok(result);
	}
	
	public Result getAllUsers(String format) {
		Iterable<User> userIterable = userRepository.findAll();
		List<User> userList = new ArrayList<User>();
		for (User user : userIterable) {
			userList.add(user);
		}
		String result = new String();
		if (format.equals("json")) {
			result = new Gson().toJson(userList);
		}
		return ok(result);
	}
	
	public Result isUserValid() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			System.out.println("Cannot check user, expecting Json data");
			return badRequest("Cannot check user, expecting Json data");
		}
		String email = json.path("email").asText();
		String password = json.path("password").asText();
		User user = userRepository.findByEmail(email);
		if (user.getPassword().equals(password)) {
			System.out.println("User is valid");
			return ok("User is valid");
		} else {
			System.out.println("User is not valid");
			return badRequest("User is not valid");
		}
	}
	
	public Result deleteUserByUserNameandPassword(String userName, String password) {
		try {
			List<User> users = userRepository.findByUserName(userName);
			if (users.size()==0) {
				System.out.println("User is not existed");
				return badRequest("User is not existed");
			}
			User user = users.get(0);
			if (user.getPassword().equals(password)) {
				System.out.println("User is deleted: "+user.getId());
				userRepository.delete(user);
				return ok("User is deleted");
			}
			else {
				System.out.println("User is not deleted for wrong password");
				return badRequest("User is not deleted for wrong password");
			}
		}
		catch (PersistenceException pe) {
			pe.printStackTrace();
			System.out.println("User is not deleted");
			return badRequest("User is not deleted");
		}
		
	}


    // new user for test
    public Result newUser() {
        return ok(user.render(""));
    }

	// add user test
	public Result addUserForTest() {
		UserBean userBean = Form.form(UserBean.class).bindFromRequest().get();

		// Parse JSON file
		String userName = userBean.getUserName();
		String password = userBean.getPassword();
		String firstName = userBean.getFirstName();
		String lastName = userBean.getLastName();
		String middleInitial = userBean.getMiddleInitial();
		String affiliation = userBean.getAffiliation();
		String title = userBean.getTitle();
		String email = userBean.getEmail();
		String mailingAddress = userBean.getMailingAddress();
		String phoneNumber = userBean.getPhoneNumber();
		String faxNumber = userBean.getFaxNumber();
		String researchFields = userBean.getResearchFields();
		String highestDegree = userBean.getHighestDegree();


		try {
			if (userRepository.findByUserName(userName).size()>0) {
				System.out.println("UserName has been used: " + userName);
				return badRequest("UserName has been used");
			}
			User user = new User(userName, password, firstName, lastName, middleInitial, affiliation, title, email, mailingAddress, phoneNumber, faxNumber, researchFields, highestDegree);
			userRepository.save(user);
			System.out.println("User saved: " + user.getId());
			return created(new Gson().toJson(user.getId()));
		} catch (PersistenceException pe) {
			pe.printStackTrace();
			System.out.println("User not saved: " + firstName + " " + lastName);
			return badRequest("User not saved: " + firstName + " " + lastName);
		}

	}

    // get user by first name
    public Result getUserByFirstName(String firstName, String format) {

        List<User> users = userRepository.findByFirstName(firstName);

        if (users == null) {
            System.out.println("Users not found with user first name: " + firstName);
            return notFound("Users not found with user first name: " + firstName);
        }

        // package response as json
        String result = new String();
        if (format.equals("json"))
            result = new Gson().toJson(users);
        return ok(result);
    }

    // get user by last name
    public Result getUserByLastName(String lastName, String format) {

        List<User> users = userRepository.findByLastName(lastName);

        if (users == null) {
            System.out.println("Users not found with user last name: " + lastName);
            return notFound("Users not found with user last name: " + lastName);
        }

        // package response as json
        String result = new String();
        if (format.equals("json"))
            result = new Gson().toJson(users);
        return ok(result);
    }

    // get user by email
    public Result getUserByEmail(String email, String format) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            System.out.println("Users not found with user email: " + email);
            return notFound("Users not found with user email: " + email);
        }

        // package response as json
        String result = new String();
        if (format.equals("json"))
            result = new Gson().toJson(user);
        return ok(result);
    }

    public Result getUserByPartial(String partial, String format) {
        return ok("");
    }

}
