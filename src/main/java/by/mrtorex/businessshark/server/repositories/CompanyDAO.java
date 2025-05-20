package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Company;
import by.mrtorex.businessshark.server.model.entities.Stock;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

/**
 * DAO-реализация для работы с сущностями компаний.
 * Предоставляет методы для CRUD-операций и управления связями с акциями.
 */
public class CompanyDAO implements DAO<Company> {
    private static final Logger logger = LogManager.getLogger(CompanyDAO.class);
    private final SessionFactory sessionFactory;

    public CompanyDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    /**
     * Сохраняет объект компании в базу данных.
     *
     * @param obj объект компании
     */
    @Override
    public void save(Company obj) {
        executeTransaction(sessionFactory, Session::persist, obj);
        logger.info("Компания сохранена: {}", obj.getName());
    }

    /**
     * Обновляет информацию о компании в базе данных.
     *
     * @param obj объект компании
     */
    @Override
    public void update(Company obj) {
        executeTransaction(sessionFactory, Session::merge, obj);
        logger.info("Компания обновлена: {}", obj.getName());
    }

    /**
     * Удаляет компанию из базы данных.
     *
     * @param company объект компании
     */
    @Override
    public void delete(Company company) {
        executeTransaction(sessionFactory, (session, c) ->
                session.remove(session.contains(c) ? c : session.merge(c)), company);
        logger.info("Компания удалена: {}", company.getName());
    }

    /**
     * Возвращает объект компании по её ID.
     *
     * @param id идентификатор компании
     * @return объект Company или null
     */
    public Company findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Company.class, id);
        } catch (Exception e) {
            logger.error("Ошибка при поиске компании по ID: {}", id, e);
            return null;
        }
    }

    /**
     * Возвращает список всех компаний.
     *
     * @return список компаний
     */
    @Override
    public List<Company> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Company> cq = cb.createQuery(Company.class);
            cq.select(cq.from(Company.class));
            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            logger.error("Ошибка при получении всех компаний", e);
            return List.of();
        }
    }

    /**
     * Ищет компанию по её названию.
     *
     * @param name название компании
     * @return объект Company или null
     */
    public Company findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Company> cq = cb.createQuery(Company.class);
            Root<Company> root = cq.from(Company.class);
            cq.select(root).where(cb.equal(root.get("name"), name));
            return session.createQuery(cq).uniqueResult();
        } catch (Exception e) {
            logger.warn("Компания с именем '{}' не найдена", name);
            return null;
        }
    }

    /**
     * Ищет компанию, к которой привязана акция по ID.
     *
     * @param stockId ID акции
     * @return объект Company или null
     */
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
        } catch (Exception e) {
            logger.error("Ошибка при поиске компании по ID акции: {}", stockId, e);
            return null;
        }
    }

    /**
     * Привязывает акцию к компании.
     *
     * @param companyId ID компании
     * @param stockId   ID акции
     */
    public void addStockToCompany(int companyId, int stockId) {
        executeTransaction(sessionFactory, (session, unused) -> {
            String sql = """
                INSERT INTO Company_Stock (company_id, stock_id)
                VALUES (:companyId, :stockId)
            """;
            session.createNativeMutationQuery(sql)
                    .setParameter("companyId", companyId)
                    .setParameter("stockId", stockId)
                    .executeUpdate();
        }, null);
        logger.info("Акция {} добавлена к компании {}", stockId, companyId);
    }

    /**
     * Удаляет привязку акции от компании.
     *
     * @param stockId ID акции
     */
    public void removeStockFromCompany(int stockId) {
        executeTransaction(sessionFactory, (session, unused) -> {
            String sql = """
                DELETE FROM Company_Stock
                WHERE stock_id = :stockId
            """;
            session.createNativeMutationQuery(sql)
                    .setParameter("stockId", stockId)
                    .executeUpdate();
        }, null);
        logger.info("Акция {} отвязана от компании", stockId);
    }

    /**
     * Получает список акций, принадлежащих компании.
     *
     * @param companyId ID компании
     * @return список акций
     */
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
        } catch (Exception e) {
            logger.error("Ошибка при получении акций компании {}", companyId, e);
            return List.of();
        }
    }
}
