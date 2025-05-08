package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Role;
import by.mrtorex.businessshark.server.repositories.RoleDAO;

import java.util.List;

public class RoleService implements Service<Role> {
    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    public Role findEntity(int id) {
        return roleDAO.findById(id);
    }

    @Override
    public void saveEntity(Role role) {
        roleDAO.save(role);
    }

    @Override
    public void deleteEntity(Role role) {
        roleDAO.delete(role);
    }

    @Override
    public void updateEntity(Role role) {
        roleDAO.update(role);
    }

    @Override
    public List<Role> findAllEntities() {
        return roleDAO.findAll();
    }

    public Role findRoleByName(String name) {
        return roleDAO.findRoleByName(name);
    }
}

