package by.mrtorex.businessshark.server.interfaces;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Базовый интерфейс для DAO (Data Access Object) операций.
 *
 * @param <T> тип сущности, с которой работает DAO
 */
public interface DAO<T> {
    Logger logger = LogManager.getLogger(DAO.class);

    /**
     * Сохраняет объект в базе данных.
     *
     * @param obj объект для сохранения
     */
    void save(T obj);

    /**
     * Обновляет объект в базе данных.
     *
     * @param obj объект для обновления
     */
    void update(T obj);

    /**
     * Удаляет объект из базы данных.
     *
     * @param obj объект для удаления
     */
    void delete(T obj);

    /**
     * Находит объект по идентификатору.
     *
     * @param id идентификатор объекта
     * @return найденный объект или null, если не найден
     */
    T findById(int id);

    /**
     * Получает все объекты данного типа из базы данных.
     *
     * @return список всех объектов
     */
    List<T> findAll();

    /**
     * Выполняет операцию в транзакции.
     *
     * @param sessionFactory фабрика сессий Hibernate
     * @param action действие для выполнения
     * @param obj объект, над которым выполняется действие
     * @throws RuntimeException если произошла ошибка при выполнении транзакции
     */
    default void executeTransaction(SessionFactory sessionFactory,
                                    TransactionConsumer<T> action,
                                    T obj) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            logger.info("Начата транзакция для объекта {}", obj.getClass().getSimpleName());

            action.accept(session, obj);

            transaction.commit();
            logger.info("Транзакция успешно завершена");
        } catch (Exception e) {
            logger.error("Ошибка выполнения транзакции", e);

            if (transaction != null && transaction.isActive()) {
                logger.warn("Откат активной транзакции");
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка выполнения транзакции: " + e.getMessage(), e);
        }
    }

    /**
     * Функциональный интерфейс для выполнения действий в транзакции.
     *
     * @param <T> тип объекта, над которым выполняется действие
     */
    @FunctionalInterface
    interface TransactionConsumer<T> {
        /**
         * Выполняет действие с объектом в контексте сессии Hibernate.
         *
         * @param session сессия Hibernate
         * @param obj объект для обработки
         */
        void accept(Session session, T obj);
    }
}