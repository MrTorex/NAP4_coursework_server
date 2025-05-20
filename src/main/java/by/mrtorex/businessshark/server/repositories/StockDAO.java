package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.interfaces.DAO;
import by.mrtorex.businessshark.server.model.entities.Stock;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Objects;

/**
 * DAO-класс для работы с сущностью Stock.
 * Выполняет CRUD операции и специализированные запросы.
 */
public class StockDAO implements DAO<Stock> {
    private static final Logger logger = LogManager.getLogger(StockDAO.class);
    private final SessionFactory sessionFactory;

    /**
     * Конструктор StockDAO.
     * Инициализирует сессию Hibernate через SessionConfig.
     */
    public StockDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
        logger.info("Инициализирован StockDAO");
    }

    /**
     * Сохраняет новую акцию в базе данных.
     *
     * @param stock объект акции, не может быть null
     * @throws NullPointerException если stock равен null
     * @throws RuntimeException при ошибке работы с базой
     */
    @Override
    public void save(Stock stock) {
        Objects.requireNonNull(stock, "Акция не может быть null");
        executeTransaction(session -> session.persist(stock));
        logger.info("Акция сохранена: {}", stock);
    }

    /**
     * Обновляет существующую акцию в базе данных.
     *
     * @param stock объект акции, не может быть null
     * @throws NullPointerException если stock равен null
     * @throws RuntimeException при ошибке работы с базой
     */
    @Override
    public void update(Stock stock) {
        Objects.requireNonNull(stock, "Акция не может быть null");
        executeTransaction(session -> session.merge(stock));
        logger.info("Акция обновлена: {}", stock);
    }

    /**
     * Удаляет акцию из базы данных.
     *
     * @param stock объект акции, не может быть null
     * @throws NullPointerException если stock равен null
     * @throws RuntimeException при ошибке работы с базой
     */
    @Override
    public void delete(Stock stock) {
        Objects.requireNonNull(stock, "Акция не может быть null");
        executeTransaction(session -> {
            Stock managedStock = session.contains(stock) ? stock : session.merge(stock);
            session.remove(managedStock);
        });
        logger.info("Акция удалена: {}", stock);
    }

    /**
     * Находит акцию по её идентификатору.
     *
     * @param id идентификатор акции, должен быть положительным числом
     * @return найденная акция или null, если акция с таким ID отсутствует
     * @throws IllegalArgumentException если id <= 0
     * @throws RuntimeException при ошибке работы с базой
     */
    public Stock findById(int id) {
        if (id <= 0) {
            logger.error("Неверный ID акции: {}", id);
            throw new IllegalArgumentException("ID акции должен быть положительным числом");
        }
        try (Session session = sessionFactory.openSession()) {
            Stock stock = session.get(Stock.class, id);
            if (stock == null) {
                logger.info("Акция с ID {} не найдена", id);
            } else {
                logger.info("Акция с ID {} была найдена", id);
            }
            return stock;
        } catch (Exception e) {
            logger.error("Ошибка при поиске акции по ID {}", id, e);
            throw e;
        }
    }

    /**
     * Возвращает список всех акций из базы.
     *
     * @return список акций, может быть пустым
     * @throws RuntimeException при ошибке работы с базой
     */
    @Override
    public List<Stock> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
            Root<Stock> root = cq.from(Stock.class);
            cq.select(root);
            Query<Stock> query = session.createQuery(cq);
            List<Stock> stocks = query.getResultList();
            logger.info("Найдено акций: {}", stocks.size());
            return stocks;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка акций", e);
            throw e;
        }
    }

    /**
     * Получение списка акций, которые не связаны ни с одной компанией.
     *
     * @return список акций без компаний, пустой список при ошибке
     */
    public List<Stock> findAllWithNoCompany() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            String sql = """
                    SELECT * FROM public.Stocks
                    WHERE id NOT IN (SELECT stock_id FROM public.Company_Stock)
                    """;

            List<Stock> unassignedStocks = session
                    .createNativeQuery(sql, Stock.class)
                    .getResultList();

            session.getTransaction().commit();
            logger.info("Найдено акций без компаний: {}", unassignedStocks.size());
            return unassignedStocks;
        } catch (Exception e) {
            logger.error("Ошибка загрузки акций без компаний", e);
            return List.of();
        }
    }

    /**
     * Поиск акции по тикеру.
     *
     * @param ticket тикер акции, не может быть null или пустым
     * @return найденная акция или null, если акция не найдена
     * @throws IllegalArgumentException если ticket null или пустой
     * @throws RuntimeException при ошибке работы с базой
     */
    public Stock findByTicket(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            logger.error("Попытка поиска акции с пустым или null тикером");
            throw new IllegalArgumentException("Тикер не может быть пустым");
        }
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
            Root<Stock> root = cq.from(Stock.class);
            cq.select(root).where(cb.equal(root.get("ticket"), ticket));
            Query<Stock> query = session.createQuery(cq);
            Stock stock = query.uniqueResult();
            if (stock == null) {
                logger.info("Акция с тикером '{}' не найдена", ticket);
            } else {
                logger.info("Акция с тикером '{}' найдена", ticket);
            }
            return stock;
        } catch (Exception e) {
            logger.error("Ошибка при поиске акции по тикеру '{}'", ticket, e);
            throw e;
        }
    }

    /**
     * Универсальный метод выполнения транзакции с обработкой ошибок.
     *
     * @param action действие с сессией Hibernate
     * @throws RuntimeException при ошибке выполнения транзакции
     */
    @SuppressWarnings("DuplicatedCode")
    private void executeTransaction(SessionAction action) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            action.execute(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка в транзакции", e);
            throw e;
        }
    }

    /**
     * Функциональный интерфейс для действий с сессией Hibernate внутри транзакции.
     */
    @FunctionalInterface
    private interface SessionAction {
        void execute(Session session);
    }
}
