package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class RoleDAO implements DAO<Role> {
    private final SessionFactory sessionFactory;

    public RoleDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    public Role findRoleByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);
            Root<Role> root = criteriaQuery.from(Role.class);
            criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), name));
            Query<Role> query = session.createQuery(criteriaQuery);
            return query.uniqueResult();
        }
    }

    @Override
    public void save(Role obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
    }

    @Override
    public void update(Role obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
    }

    @Override
    public void delete(Role obj) {
        executeTransaction(sessionFactory, (session, role) ->
                session.remove(session.contains(role) ? role : session.merge(role)), obj);
    }

    @Override
    public Role findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Role.class, id);
        }
    }

    @Override
    public List<Role> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);
            Root<Role> root = criteriaQuery.from(Role.class);
            criteriaQuery.select(root);
            Query<Role> query = session.createQuery(criteriaQuery);
            return query.getResultList();
        }
    }
}
