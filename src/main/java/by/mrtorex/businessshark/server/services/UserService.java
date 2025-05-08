package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Person;
import by.mrtorex.businessshark.server.model.entities.Role;
import by.mrtorex.businessshark.server.model.entities.User;
import by.mrtorex.businessshark.server.repositories.UserDAO;
import by.mrtorex.businessshark.server.validators.UserValidator;

import java.util.List;
import java.util.Objects;

public class UserService implements Service<User> {
    private final UserDAO userDAO = new UserDAO();

    public User login(User user) {
        if (!new UserValidator().isValid(user)) {
            throw new ResponseException("LOGIN_FAIL: Missing login or password");
        }

        String login = user.getUsername();
        String password = user.getPassword();

        // Fetch user from the database using login
        User existingUser = userDAO.findByLogin(login);

        if (existingUser != null && existingUser.getPassword().equals(password)) {
            return existingUser;
        } else {
            throw  new ResponseException("LOGIN_FAIL: Invalid login or password");
        }
    }

    public User register(User user, PersonService personService, RoleService roleService) throws ResponseException {
        if (!new UserValidator().isFullValid(user)) {
            throw new ResponseException("REGISTER_FAIL: Missing registration data");
        }

        User existingUser = userDAO.findByLogin(user.getUsername());
        if (existingUser != null) {
            throw new ResponseException("REGISTER_FAIL: User already exists");
        }

        Person person = user.getPerson();
        personService.saveEntity(person);
        user.setPerson(person);

        Role role = roleService.findRoleByName(user.getRole().getName());
        if (role == null) {
            throw new ResponseException("REGISTER_FAIL: Role not found");
        }

        user.setRole(role);

        userDAO.save(user);

        return user;
    }

    @Override
    public User findEntity(int id) {
        return userDAO.findById(id);
    }

    @Override
    public void saveEntity(User user) {
        userDAO.save(user);
    }

    @Override
    public void deleteEntity(User userToDelete) throws ResponseException {
        User existingUser = userDAO.findByLogin(userToDelete.getUsername());
        if (existingUser == null) {
            throw new ResponseException("User not found.");
        }

        userDAO.delete(existingUser);
    }

    @Override
    public void updateEntity(User user) {
        userDAO.update(user);
    }

    public void updateEntity(User newUser, PersonService personService) {
        User existingUser = userDAO.findById(newUser.getId());

        if (existingUser == null) {
            throw new ResponseException("UPDATE_FAIL: User doesn't exists");
        }

        User userWithSuchUsername = userDAO.findByLogin(newUser.getUsername());

        if (userWithSuchUsername != null && !Objects.equals(userWithSuchUsername.getId(), existingUser.getId())) {
            throw new ResponseException("UPDATE_FAIL: User with such username already exists");
        }

        if (!new UserValidator().isFullValid(newUser)) {
            throw new ResponseException("UPDATE_FAIL: User data not valid");
        }

        personService.updateEntity(newUser.getPerson());

        userDAO.update(newUser);
    }

    @Override
    public List<User> findAllEntities() {
        return userDAO.findAll();
    }

    public User findByUsername(String username) {
        return userDAO.findByLogin(username);
    }
}
