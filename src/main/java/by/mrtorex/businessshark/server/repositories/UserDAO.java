package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Person;
import by.mrtorex.businessshark.server.model.entities.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAO implements DAO<User> {
    private final SessionFactory sessionFactory;

    public UserDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    @Override
    public void save(User obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(User obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(User user) {
        executeTransaction(sessionFactory, (session, u) ->
                session.remove(session.contains(u) ? u : session.merge(u)), user);
    }

    public User findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, id);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            criteriaQuery.select(root);
            Query<User> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }

    public User findByLogin(String login) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            root.fetch("role", JoinType.LEFT);
            root.fetch("person", JoinType.LEFT);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("username"), login));
            Query<User> query = session.createQuery(criteriaQuery);
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }
}

