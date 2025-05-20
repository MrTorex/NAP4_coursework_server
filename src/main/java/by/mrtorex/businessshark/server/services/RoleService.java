package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Role;
import by.mrtorex.businessshark.server.repositories.RoleDAO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Сервис для управления ролями пользователей.
 */
public class RoleService implements Service<Role> {
    private static final Logger logger = LogManager.getLogger(RoleService.class);
    private final RoleDAO roleDAO = new RoleDAO();

    /**
     * Находит роль по её ID.
     *
     * @param id ID роли
     * @return объект Role или null, если роль не найдена
     * @throws IllegalArgumentException при некорректном ID
     */
    @Override
    public Role findEntity(int id) {
        if (id <= 0) {
            logger.error("Некорректный ID роли: {}", id);
            throw new IllegalArgumentException("ID роли должен быть положительным");
        }
        return roleDAO.findById(id);
    }

    /**
     * Сохраняет новую роль.
     *
     * @param role объект роли
     * @throws IllegalArgumentException при null-значении роли
     */
    @Override
    public void saveEntity(Role role) {
        if (role == null) {
            logger.error("Попытка сохранить null-роль");
            throw new IllegalArgumentException("Роль не может быть null");
        }
        roleDAO.save(role);
        logger.info("Сохранена новая роль: {}", role.getName());
    }

    /**
     * Удаляет роль.
     *
     * @param role объект роли
     * @throws IllegalArgumentException при null-значении роли
     */
    @Override
    public void deleteEntity(Role role) {
        if (role == null) {
            logger.error("Попытка удалить null-роль");
            throw new IllegalArgumentException("Роль не может быть null");
        }
        roleDAO.delete(role);
        logger.info("Удалена роль: {}", role.getName());
    }

    /**
     * Обновляет данные роли.
     *
     * @param role объект роли
     * @throws IllegalArgumentException при null-значении роли
     */
    @Override
    public void updateEntity(Role role) {
        if (role == null) {
            logger.error("Попытка обновить null-роль");
            throw new IllegalArgumentException("Роль не может быть null");
        }
        roleDAO.update(role);
        logger.info("Обновлена роль: {}", role.getName());
    }

    /**
     * Получает список всех ролей.
     *
     * @return список ролей
     */
    @Override
    public List<Role> findAllEntities() {
        return roleDAO.findAll();
    }

    /**
     * Находит роль по её названию.
     *
     * @param name название роли
     * @return объект Role или null, если роль не найдена
     * @throws IllegalArgumentException при пустом или null названии
     */
    public Role findRoleByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.error("Попытка найти роль с пустым или null именем");
            throw new IllegalArgumentException("Имя роли не может быть пустым или null");
        }
        return roleDAO.findRoleByName(name);
    }
}
