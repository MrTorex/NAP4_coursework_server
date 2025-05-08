package by.mrtorex.businessshark.server.interfaces;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;

import java.util.List;

public interface DAO<T> {
    void save(T obj);
    void update(T obj);
    void delete(T obj);
    T findById(int id);
    List<T> findAll();

    default void executeTransaction(SessionFactory sessionFactory, TransactionConsumer<T> action, T obj) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            action.accept(session, obj);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @FunctionalInterface
    interface TransactionConsumer<T> {
        void accept(Session session, T obj);
    }
}
