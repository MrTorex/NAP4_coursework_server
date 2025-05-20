package by.mrtorex.businessshark.server.validators;

import by.mrtorex.businessshark.server.interfaces.Validatable;
import by.mrtorex.businessshark.server.model.entities.Role;

/**
 * Валидатор для проверки корректности объекта Role.
 */
public class RoleValidator implements Validatable<Role> {

    /**
     * Проверяет, что объект Role не null и содержит непустое имя роли.
     *
     * @param role проверяемый объект Role
     * @return true, если объект корректен; false в противном случае
     */
    @Override
    public boolean isValid(Role role) {
        return role != null &&
                role.getName() != null &&
                !role.getName().isEmpty();
    }
}
