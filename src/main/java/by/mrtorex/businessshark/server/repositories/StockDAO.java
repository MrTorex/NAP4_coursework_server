package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Stock;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class StockDAO implements DAO<Stock> {
    private final SessionFactory sessionFactory;

    public StockDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Stock obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(Stock obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(Stock stock) {
        executeTransaction(sessionFactory, (session, s) ->
                session.remove(session.contains(s) ? s : session.merge(s)), stock);
    }

    public Stock findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Stock.class, id);
        }
    }

    @Override
    public List<Stock> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Stock> criteriaQuery = criteriaBuilder.createQuery(Stock.class);
            Root<Stock> root = criteriaQuery.from(Stock.class);
            criteriaQuery.select(root);
            Query<Stock> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public Stock findByTicket(String ticket) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Stock> criteriaQuery = criteriaBuilder.createQuery(Stock.class);
            Root<Stock> root = criteriaQuery.from(Stock.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("ticket"), ticket));
            Query<Stock> query = session.createQuery(criteriaQuery);
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }
}
