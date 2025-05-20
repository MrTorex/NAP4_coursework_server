package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.RoleService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Контроллер для управления операциями с ролями пользователей.
 * Обеспечивает взаимодействие между сетевым интерфейсом и сервисом ролей.
 */
public class RoleController {
    private static final Logger logger = LogManager.getLogger(RoleController.class);
    private final RoleService roleService;

    /**
     * Конструктор с внедрением зависимости сервиса.
     *
     * @param roleService сервис для работы с ролями
     */
    public RoleController(final RoleService roleService) {
        this.roleService = roleService;
        logger.info("Инициализирован RoleController с внешним сервисом");
    }

    /**
     * Конструктор по умолчанию.
     */
    public RoleController() {
        this.roleService = new RoleService();
        logger.info("Инициализирован RoleController со стандартным сервисом");
    }

    /**
     * Получает список всех ролей.
     *
     * @return ответ со списком ролей в формате JSON
     */
    public Response getAllRoles() {
        try {
            logger.info("Запрос всех ролей");
            String rolesJson = Serializer.toJson(roleService.findAllEntities());
            logger.info("Успешно получено {} ролей", roleService.findAllEntities().size());
            return new Response(true, "Список ролей успешно получен", rolesJson);
        } catch (ResponseException e) {
            logger.error("Ошибка при получении списка ролей: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при получении списка ролей", e);
            return new Response(false, "Внутренняя ошибка сервера", null);
        }
    }
}