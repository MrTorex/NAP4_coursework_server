package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Role;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Objects;

/**
 * DAO-класс для работы с сущностью Role.
 * Обеспечивает CRUD операции и поиск по имени роли.
 */
public class RoleDAO implements DAO<Role> {
    private static final Logger logger = LogManager.getLogger(RoleDAO.class);
    private final SessionFactory sessionFactory;

    /**
     * Инициализация DAO с сессией из конфигурации.
     */
    public RoleDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
        logger.info("Инициализирован RoleDAO");
    }

    /**
     * Ищет роль по имени.
     *
     * @param name имя роли
     * @return найденная роль или null, если не найдена
     * @throws IllegalArgumentException если имя роли пустое или null
     */
    public Role findRoleByName(String name) {
        if (name == null || name.isBlank()) {
            logger.error("Попытка поиска роли с пустым или null именем");
            throw new IllegalArgumentException("Имя роли не может быть пустым");
        }

        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Role> cq = cb.createQuery(Role.class);
            Root<Role> root = cq.from(Role.class);
            cq.select(root).where(cb.equal(root.get("name"), name));
            Query<Role> query = session.createQuery(cq);
            Role role = query.uniqueResult();
            if (role == null) {
                logger.info("Роль с именем '{}' не найдена", name);
            } else {
                logger.info("Роль с именем '{}' найдена", name);
            }
            return role;
        } catch (Exception e) {
            logger.error("Ошибка при поиске роли по имени '{}'", name, e);
            throw e;
        }
    }

    /**
     * Сохраняет новую роль в базе.
     *
     * @param role объект роли для сохранения
     * @throws IllegalArgumentException если роль null
     */
    @Override
    public void save(Role role) {
        Objects.requireNonNull(role, "Роль не может быть null");
        executeTransaction(session -> session.persist(role));
        logger.info("Роль сохранена: {}", role);
    }

    /**
     * Обновляет существующую роль.
     *
     * @param role объект роли для обновления
     * @throws IllegalArgumentException если роль null
     */
    @Override
    public void update(Role role) {
        Objects.requireNonNull(role, "Роль не может быть null");
        executeTransaction(session -> session.merge(role));
        logger.info("Роль обновлена: {}", role);
    }

    /**
     * Удаляет роль из базы.
     *
     * @param role объект роли для удаления
     * @throws IllegalArgumentException если роль null
     */
    @Override
    public void delete(Role role) {
        Objects.requireNonNull(role, "Роль не может быть null");
        executeTransaction(session -> {
            Role managedRole = session.contains(role) ? role : session.merge(role);
            session.remove(managedRole);
        });
        logger.info("Роль удалена: {}", role);
    }

    /**
     * Ищет роль по ID.
     *
     * @param id идентификатор роли
     * @return найденная роль или null, если не найдена
     * @throws IllegalArgumentException если id меньше или равен 0
     */
    @Override
    public Role findById(int id) {
        if (id <= 0) {
            logger.error("Неверный ID роли: {}", id);
            throw new IllegalArgumentException("ID роли должен быть положительным числом");
        }
        try (Session session = sessionFactory.openSession()) {
            Role role = session.get(Role.class, id);
            if (role == null) {
                logger.info("Роль с ID {} не найдена", id);
            } else {
                logger.info("Роль с ID {} была найдена", id);
            }
            return role;
        } catch (Exception e) {
            logger.error("Ошибка при поиске роли по ID {}", id, e);
            throw e;
        }
    }

    /**
     * Возвращает список всех ролей.
     *
     * @return список ролей, не может быть null
     */
    @Override
    public List<Role> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Role> cq = cb.createQuery(Role.class);
            Root<Role> root = cq.from(Role.class);
            cq.select(root);
            Query<Role> query = session.createQuery(cq);
            List<Role> roles = query.getResultList();
            logger.info("Найдено ролей: {}", roles.size());
            return roles;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка ролей", e);
            throw e;
        }
    }

    /**
     * Универсальный метод выполнения транзакции.
     *
     * @param action действие с сессией
     */
    @SuppressWarnings("DuplicatedCode")
    private void executeTransaction(SessionAction action) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            action.execute(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка в транзакции", e);
            throw e;
        }
    }

    /**
     * Функциональный интерфейс для действий с сессией в транзакции.
     */
    @FunctionalInterface
    private interface SessionAction {
        void execute(Session session);
    }
}
