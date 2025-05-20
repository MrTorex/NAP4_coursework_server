package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Person;
import by.mrtorex.businessshark.server.repositories.PersonDAO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Сервис для управления сущностью Person.
 * Выполняет операции CRUD с проверками.
 */
public class PersonService implements Service<Person> {
    private static final Logger logger = LogManager.getLogger(PersonService.class);
    private final PersonDAO personDAO = new PersonDAO();

    /**
     * Находит персону по ID.
     *
     * @param id идентификатор персоны
     * @return объект Person или null, если не найден
     */
    @Override
    public Person findEntity(int id) {
        return personDAO.findById(id);
    }

    /**
     * Сохраняет новую персону.
     *
     * @param person объект Person для сохранения
     */
    @Override
    public void saveEntity(Person person) {
        personDAO.save(person);
        logger.info("Персона сохранена: {}", person);
    }

    /**
     * Удаляет персону.
     *
     * @param person объект Person для удаления
     * @throws ResponseException если передан null
     */
    @Override
    public void deleteEntity(Person person) throws ResponseException {
        if (person == null) {
            logger.error("Ошибка удаления: объект Person равен null");
            throw new ResponseException("Person not found.");
        }
        personDAO.delete(person);
        logger.info("Персона удалена: {}", person);
    }

    /**
     * Обновляет данные персоны.
     *
     * @param person объект Person с обновлёнными данными
     */
    @Override
    public void updateEntity(Person person) {
        personDAO.update(person);
        logger.info("Данные персоны обновлены: {}", person);
    }

    /**
     * Получает список всех персон.
     *
     * @return список объектов Person
     */
    @Override
    public List<Person> findAllEntities() {
        return personDAO.findAll();
    }
}
