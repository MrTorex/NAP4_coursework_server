package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Company;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class CompanyDAO implements DAO<Company> {
    private final SessionFactory sessionFactory;

    public CompanyDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(Company obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(Company obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(Company company) {
        executeTransaction(sessionFactory, (session, c) ->
                session.remove(session.contains(c) ? c : session.merge(c)), company);
    }

    public Company findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Company.class, id);
        }
    }

    @Override
    public List<Company> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Company> criteriaQuery = criteriaBuilder.createQuery(Company.class);
            Root<Company> root = criteriaQuery.from(Company.class);
            criteriaQuery.select(root);
            Query<Company> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public Company findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Company> criteriaQuery = criteriaBuilder.createQuery(Company.class);
            Root<Company> root = criteriaQuery.from(Company.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), name));
            Query<Company> query = session.createQuery(criteriaQuery);
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }
}
