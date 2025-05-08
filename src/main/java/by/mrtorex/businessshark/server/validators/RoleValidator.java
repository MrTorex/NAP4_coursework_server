package by.mrtorex.businessshark.server.validators;

import by.mrtorex.businessshark.server.interfaces.Validatable;
import by.mrtorex.businessshark.server.model.entities.Role;

public class RoleValidator implements Validatable<Role> {

    @Override
    public boolean isValid(Role role) {
        return role != null &&
                role.getName() != null &&
                !role.getName().isEmpty();
    }
}
