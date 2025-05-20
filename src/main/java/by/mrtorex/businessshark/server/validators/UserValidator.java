package by.mrtorex.businessshark.server.validators;

import by.mrtorex.businessshark.server.interfaces.Validatable;
import by.mrtorex.businessshark.server.model.entities.User;

/**
 * Валидатор для проверки корректности объекта User.
 */
public class UserValidator implements Validatable<User> {

    /**
     * Полная проверка пользователя, включая вложенные объекты Person и Role.
     *
     * @param user объект пользователя для проверки
     * @return true, если все данные корректны, иначе false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFullValid(User user) {
        return isValid(user) &&
                new PersonValidator().isValid(user.getPerson()) &&
                new RoleValidator().isValid(user.getRole());
    }

    /**
     * Базовая проверка объекта User: проверка логина и хеша пароля.
     *
     * @param user объект пользователя для проверки
     * @return true, если логин и пароль заданы, иначе false
     */
    @Override
    public boolean isValid(User user) {
        return user != null &&
                user.getUsername() != null &&
                !user.getUsername().isEmpty() &&
                user.getPasswordHash() != null &&
                !user.getPasswordHash().isEmpty();
    }
}
