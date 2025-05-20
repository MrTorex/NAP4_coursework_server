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

/**
 * Сервис для управления сущностями пользователей.
 * Осуществляет операции аутентификации, регистрации, поиска, обновления и удаления пользователей.
 */
public class UserService implements Service<User> {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Аутентификация пользователя по логину и паролю.
     *
     * @param user объект пользователя с логином и хешем пароля
     * @return найденный пользователь
     * @throws ResponseException если данные невалидны или пользователь не найден/не совпадает пароль
     */
    public User login(User user) {
        if (!new UserValidator().isValid(user)) {
            throw new ResponseException("Ошибка входа: отсутствует логин или пароль");
        }

        String login = user.getUsername();
        String passwordHash = user.getPasswordHash();

        User existingUser = userDAO.findByLogin(login);

        if (existingUser != null && existingUser.getPasswordHash().equals(passwordHash)) {
            return existingUser;
        } else {
            throw new ResponseException("Ошибка входа: неверный логин или пароль");
        }
    }

    /**
     * Регистрация нового пользователя с привязкой к сущности Person и роли.
     *
     * @param user объект пользователя для регистрации
     * @param personService сервис для работы с персональными данными
     * @param roleService сервис для работы с ролями
     * @return зарегистрированный пользователь
     * @throws ResponseException при отсутствии данных, дублировании или неверной роли
     */
    public User register(User user, PersonService personService, RoleService roleService) throws ResponseException {
        if (!new UserValidator().isFullValid(user)) {
            throw new ResponseException("Ошибка регистрации: неполные данные");
        }

        User existingUser = userDAO.findByLogin(user.getUsername());
        if (existingUser != null) {
            throw new ResponseException("Ошибка регистрации: пользователь с таким логином уже существует");
        }

        Person person = user.getPerson();
        personService.saveEntity(person);
        user.setPerson(person);

        Role role = roleService.findRoleByName(user.getRole().getName());
        if (role == null) {
            throw new ResponseException("Ошибка регистрации: роль не найдена");
        }
        user.setRole(role);

        userDAO.save(user);

        return user;
    }

    /**
     * Поиск пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь или null
     */
    @Override
    public User findEntity(int id) {
        return userDAO.findById(id);
    }

    /**
     * Сохранение нового пользователя.
     *
     * @param user объект пользователя
     */
    @Override
    public void saveEntity(User user) {
        userDAO.save(user);
    }

    /**
     * Удаление пользователя по логину.
     *
     * @param userToDelete объект пользователя для удаления
     * @throws ResponseException если пользователь не найден
     */
    @Override
    public void deleteEntity(User userToDelete) throws ResponseException {
        User existingUser = userDAO.findByLogin(userToDelete.getUsername());
        if (existingUser == null) {
            throw new ResponseException("Пользователь не найден.");
        }
        userDAO.delete(existingUser);
    }

    /**
     * Обновление существующего пользователя.
     *
     * @param user объект пользователя с обновлёнными данными
     */
    @Override
    public void updateEntity(User user) {
        userDAO.update(user);
    }

    /**
     * Обновление пользователя с проверками и обновлением связанных данных Person.
     *
     * @param newUser объект пользователя с обновлёнными данными
     * @param personService сервис для обновления персональных данных
     * @throws ResponseException если пользователь не найден, данные невалидны или логин занят
     */
    public void updateEntity(User newUser, PersonService personService) {
        User existingUser = userDAO.findById(newUser.getId());

        if (existingUser == null) {
            throw new ResponseException("Ошибка обновления: пользователь не существует");
        }

        User userWithSuchUsername = userDAO.findByLogin(newUser.getUsername());

        if (userWithSuchUsername != null && !Objects.equals(userWithSuchUsername.getId(), existingUser.getId())) {
            throw new ResponseException("Ошибка обновления: пользователь с таким логином уже существует");
        }

        if (!new UserValidator().isFullValid(newUser)) {
            throw new ResponseException("Ошибка обновления: данные пользователя некорректны");
        }

        personService.updateEntity(newUser.getPerson());

        userDAO.update(newUser);
    }

    /**
     * Получение списка всех пользователей.
     *
     * @return список всех пользователей
     */
    @Override
    public List<User> findAllEntities() {
        return userDAO.findAll();
    }

    /**
     * Поиск пользователя по логину.
     *
     * @param username логин пользователя
     * @return найденный пользователь или null
     */
    public User findByUsername(String username) {
        return userDAO.findByLogin(username);
    }
}
