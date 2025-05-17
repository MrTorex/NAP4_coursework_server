package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.model.entities.User;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Deserializer;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.PersonService;
import by.mrtorex.businessshark.server.services.RoleService;
import by.mrtorex.businessshark.server.services.UserService;
import by.mrtorex.businessshark.server.utils.Pair;

import java.util.Objects;

public class UserController {
    private final UserService userService;
    private final PersonService personService;
    private final RoleService roleService;

    public UserController(UserService userService, PersonService personService, RoleService roleService) {
        this.userService = userService;
        this.personService = personService;
        this.roleService = roleService;
    }

    public UserController() {
        this.userService = new UserService();
        this.personService = new PersonService();
        this.roleService = new RoleService();
    }

    public Response login(Request request) {
        Deserializer deserializer = new Deserializer();
        User user = (User) deserializer.extractData(request);

        try {
            User existingUser = userService.login(user);
            String loggedInUser = Serializer.toJson(existingUser);

            return new Response(true, "Login Successful", loggedInUser);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            return new Response(false, "An unexpected error occurred", null);
        }
    }


    public Response register(Request request) {
        Deserializer deserializer = new Deserializer();
        Object extractedData;

        try {
            extractedData = deserializer.extractData(request);
        } catch (IllegalArgumentException e) {
            return new Response(false, "Invalid user data", null);
        }

        if (!(extractedData instanceof User user)) {
            return new Response(false, "Invalid user data", null);
        }

        try {
            String registeredUserJson = Serializer.toJson(
                    userService.register(user, personService, roleService)
            );
            return new Response(true, "Registration Successful", registeredUserJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllUsers() {
        try {
            String users = Serializer.toJson(userService.findAllEntities());
            return new Response(true, "Users retrieved successfully", users);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve users", null);
        }
    }


    public Response deleteUser(Request request) {
        Deserializer deserializer = new Deserializer();
        String login = (String) deserializer.extractData(request);

        try {
            User foundUser = userService.findByUsername(login);
            if (foundUser != null) {
                personService.deleteEntity(foundUser.getPerson());

                return new Response(true, "User deleted successfully", null);
            } else {
                return new Response(false, "User not found", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }


    public Response updateEntity(Request request) {
        Object extractData = new Deserializer().extractData(request);

        if (extractData instanceof Pair<?,?> pair &&
                pair.getKey() instanceof User userToUpdate &&
                pair.getValue() instanceof User userThatOperate) {
            User existingUserToUpdate = userService.findEntity(userToUpdate.getId());
            User existingUserThatOperate = userService.findEntity(userThatOperate.getId());

            if (existingUserToUpdate == null || existingUserThatOperate == null) {
                return new Response(false, "Some of the users don't exist", null);
            }

            try {
                userService.updateEntity(userToUpdate, personService);
                if (Objects.equals(userToUpdate.getId(), userThatOperate.getId()))
                    return new Response(true, "User and associated Person updated successfully. It was your user, so you need to login again.", null);
                else
                    return new Response(true, "User and associated Person updated successfully", null);
            } catch (ResponseException e) {
                return new Response(false, e.getMessage(), null);
            }
        } else {
            return new Response(false, "Bad information from client!", null);

        }
    }

    public Response readEntity(Request request) {
        Deserializer deserializer = new Deserializer();
        String username = (String) deserializer.extractData(request);

        try {
            User user = userService.findByUsername(username);
            String userJson = Serializer.toJson(user);
            if (user != null) {
                return new Response(true, "User retrieved successfully", userJson);
            } else {
                return new Response(false, "User not found", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }
}
