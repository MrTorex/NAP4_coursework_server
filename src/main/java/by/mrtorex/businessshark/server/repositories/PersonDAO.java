package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Person;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO для работы с сущностью Person.
 * Обеспечивает операции сохранения, обновления, удаления и поиска.
 */
public class PersonDAO implements DAO<Person> {
    private static final Logger logger = LogManager.getLogger(PersonDAO.class);
    private final SessionFactory sessionFactory;

    /**
     * Конструктор. Инициализирует SessionFactory.
     */
    public PersonDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
        logger.info("Инициализирован PersonDAO с SessionFactory");
    }

    /**
     * Сохраняет нового человека в БД.
     *
     * @param obj объект Person для сохранения
     * @throws IllegalArgumentException если obj равен null
     */
    @Override
    public void save(Person obj) {
        if (obj == null) {
            logger.error("Попытка сохранить null объект Person");
            throw new IllegalArgumentException("Объект Person не может быть null");
        }
        executeTransaction(sessionFactory, Session::persist, obj);
        logger.info("Сохранён объект Person с ID {}", obj.getId());
    }

    /**
     * Обновляет данные существующего человека.
     *
     * @param obj объект Person с обновлёнными данными
     * @throws IllegalArgumentException если obj равен null
     */
    @Override
    public void update(Person obj) {
        if (obj == null) {
            logger.error("Попытка обновить null объект Person");
            throw new IllegalArgumentException("Объект Person не может быть null");
        }
        executeTransaction(sessionFactory, Session::merge, obj);
        logger.info("Обновлён объект Person с ID {}", obj.getId());
    }

    /**
     * Удаляет человека из БД.
     *
     * @param obj объект Person для удаления
     * @throws IllegalArgumentException если obj равен null
     */
    @Override
    public void delete(Person obj) {
        if (obj == null) {
            logger.error("Попытка удалить null объект Person");
            throw new IllegalArgumentException("Объект Person не может быть null");
        }
        executeTransaction(sessionFactory, (session, person) ->
                session.remove(session.contains(person) ? person : session.merge(person)), obj);
        logger.info("Удалён объект Person с ID {}", obj.getId());
    }

    /**
     * Находит человека по ID.
     *
     * @param id идентификатор Person
     * @return найденный объект Person или null если не найден
     * @throws IllegalArgumentException если id <= 0
     */
    @Override
    public Person findById(int id) {
        if (id <= 0) {
            logger.error("Некорректный ID для поиска Person: {}", id);
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }
        try (Session session = sessionFactory.openSession()) {
            Person person = session.get(Person.class, id);
            if (person == null) {
                logger.warn("Person с ID {} не найден", id);
            } else {
                logger.info("Найден Person с ID {}", id);
            }
            return person;
        } catch (Exception e) {
            logger.error("Ошибка при поиске Person с ID {}", id, e);
            throw e;
        }
    }

    /**
     * Получает список всех людей.
     *
     * @return список объектов Person
     */
    @Override
    public List<Person> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Person> criteriaQuery = criteriaBuilder.createQuery(Person.class);
            Root<Person> root = criteriaQuery.from(Person.class);
            criteriaQuery.select(root);
            Query<Person> query = session.createQuery(criteriaQuery);
            List<Person> results = query.getResultList();
            logger.info("Получен список всех Person, найдено {} записей", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка Person", e);
            throw e;
        }
    }
}
