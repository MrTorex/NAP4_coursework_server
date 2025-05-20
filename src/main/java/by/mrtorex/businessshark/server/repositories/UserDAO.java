package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
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
 * DAO-класс для работы с сущностью User.
 */
public class UserDAO implements DAO<User> {
    private static final Logger logger = LogManager.getLogger(UserDAO.class);
    private final SessionFactory sessionFactory;

    /**
     * Конструктор UserDAO.
     * Инициализирует сессию Hibernate через SessionConfig.
     */
    public UserDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
        logger.info("Инициализирован UserDAO");
    }

    /**
     * Сохраняет нового пользователя в базу данных.
     *
     * @param user объект пользователя, не может быть null
     * @throws NullPointerException если user равен null
     * @throws RuntimeException в случае ошибки работы с базой
     */
    @Override
    public void save(User user) {
        Objects.requireNonNull(user, "Пользователь не может быть null");
        executeTransaction(session -> session.persist(user));
        logger.info("Пользователь сохранён: {}", user);
    }

    /**
     * Обновляет существующего пользователя в базе данных.
     *
     * @param user объект пользователя, не может быть null
     * @throws NullPointerException если user равен null
     * @throws RuntimeException в случае ошибки работы с базой
     */
    @Override
    public void update(User user) {
        Objects.requireNonNull(user, "Пользователь не может быть null");
        executeTransaction(session -> session.merge(user));
        logger.info("Пользователь обновлён: {}", user);
    }

    /**
     * Удаляет пользователя из базы данных.
     *
     * @param user объект пользователя, не может быть null
     * @throws NullPointerException если user равен null
     * @throws RuntimeException в случае ошибки работы с базой
     */
    @Override
    public void delete(User user) {
        Objects.requireNonNull(user, "Пользователь не может быть null");
        executeTransaction(session -> {
            User managedUser = session.contains(user) ? user : session.merge(user);
            session.remove(managedUser);
        });
        logger.info("Пользователь удалён: {}", user);
    }

    /**
     * Находит пользователя по его идентификатору.
     *
     * @param id положительный идентификатор пользователя
     * @return объект пользователя или null, если не найден
     * @throws IllegalArgumentException если id <= 0
     * @throws RuntimeException в случае ошибки работы с базой
     */
    public User findById(int id) {
        if (id <= 0) {
            logger.error("Неверный ID пользователя: {}", id);
            throw new IllegalArgumentException("ID пользователя должен быть положительным числом");
        }
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            if (user == null) {
                logger.info("Пользователь с ID {} не найден", id);
            } else {
                logger.info("Пользователь с ID {} был найден", id);
            }
            return user;
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID {}", id, e);
            throw e;
        }
    }

    /**
     * Возвращает список всех пользователей из базы.
     *
     * @return список пользователей, может быть пустым
     * @throws RuntimeException в случае ошибки работы с базой
     */
    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root);
            Query<User> query = session.createQuery(cq);
            List<User> users = query.getResultList();
            logger.info("Найдено пользователей: {}", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей", e);
            throw e;
        }
    }

    /**
     * Находит пользователя по логину с загрузкой связей роли и персоны.
     *
     * @param login логин пользователя, не может быть null или пустым
     * @return найденный пользователь или null, если не найден
     * @throws IllegalArgumentException если login null или пустой
     * @throws RuntimeException в случае ошибки работы с базой
     */
    public User findByLogin(String login) {
        if (login == null || login.isBlank()) {
            logger.error("Попытка поиска пользователя с пустым или null логином");
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            root.fetch("role", JoinType.LEFT);
            root.fetch("person", JoinType.LEFT);
            cq.select(root).where(cb.equal(root.get("username"), login));
            Query<User> query = session.createQuery(cq);
            User user = query.uniqueResult();
            if (user == null) {
                logger.info("Пользователь с логином '{}' не найден", login);
            } else {
                logger.info("Пользователь с логином '{}' найден", login);
            }
            return user;
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по логину '{}'", login, e);
            throw e;
        }
    }

    /**
     * Универсальный метод выполнения транзакции с обработкой ошибок.
     *
     * @param action действие с сессией Hibernate
     * @throws RuntimeException в случае ошибки выполнения транзакции
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
     * Функциональный интерфейс для действий с сессией Hibernate внутри транзакции.
     */
    @FunctionalInterface
    private interface SessionAction {
        void execute(Session session);
    }
}
