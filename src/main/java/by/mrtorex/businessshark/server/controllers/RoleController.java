package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.RoleService;

public class RoleController {
    private final RoleService roleService;

    public RoleController(final RoleService roleService) {
        this.roleService = roleService;
    }

    public RoleController() {
        this.roleService = new RoleService();
    }

    public Response getAllRoles() {
        try {
            String roles = Serializer.toJson(roleService.findAllEntities());
            return new Response(true, "Roles retrieved successfully", roles);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve roles", null);
        }
    }
}
