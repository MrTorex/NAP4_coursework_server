package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Company;
import by.mrtorex.businessshark.server.model.entities.Stock;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

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

    public Company findByStockId(int stockId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
                SELECT c.* FROM Companies c
                JOIN Company_Stock cs ON c.id = cs.company_id
                WHERE cs.stock_id = :stockId
            """;
            NativeQuery<Company> query = session.createNativeQuery(sql, Company.class);
            query.setParameter("stockId", stockId);
            return query.uniqueResult();
        }
    }

    public void addStockToCompany(int companyId, int stockId) {
        executeTransaction(sessionFactory, (session, unused) -> {
            String sql = """
                INSERT INTO Company_Stock (company_id, stock_id)
                VALUES (:companyId, :stockId)
            """;
            session.createNativeQuery(sql)
                    .setParameter("companyId", companyId)
                    .setParameter("stockId", stockId)
                    .executeUpdate();
        }, null);
    }

    public void removeStockFromCompany(int stockId) {
        executeTransaction(sessionFactory, (session, unused) -> {
            String sql = """
                DELETE FROM Company_Stock
                WHERE stock_id = :stockId
            """;
            session.createNativeQuery(sql)
                    .setParameter("stockId", stockId)
                    .executeUpdate();
        }, null);
    }

    public List<Stock> getCompanyStocks(int companyId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
                SELECT s.* FROM Stocks s
                JOIN Company_Stock cs ON s.id = cs.stock_id
                WHERE cs.company_id = :companyId
            """;
            NativeQuery<Stock> query = session.createNativeQuery(sql, Stock.class);
            query.setParameter("companyId", companyId);
            return query.getResultList();
        }
    }
}
