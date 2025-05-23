package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.utils.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import java.util.Collections;
import java.util.List;

/**
 * DAO для работы с портфелем пользователя.
 * Предоставляет методы для управления акциями пользователя и их балансом.
 */
public class PortfolioDAO {

    private static final Logger logger = LogManager.getLogger(PortfolioDAO.class);
    private final SessionFactory sessionFactory = SessionConfig.getInstance().getSessionFactory();

    /**
     * Сохраняет или добавляет акции в портфель пользователя.
     * При наличии акций у пользователя их количество увеличивается.
     *
     * @param obj    объект {@link Pair}, содержащий акцию и количество
     * @param userId идентификатор пользователя
     */
    public void save(Pair<Stock, Integer> obj, int userId) {
        executeTransaction(session -> {
            String sql = """
                INSERT INTO User_Stock (user_id, stock_id, amount)
                VALUES (:userId, :stockId, :amount)
                ON CONFLICT (user_id, stock_id) DO UPDATE SET amount = User_Stock.amount + :amount
            """;
            @SuppressWarnings({"deprecated", "deprecation"})
            NativeQuery<?> query = session.createNativeQuery(sql);
            query.setParameter("userId", userId);
            query.setParameter("stockId", obj.getKey().getId());
            query.setParameter("amount", obj.getValue());
            query.executeUpdate();
            logger.info("Добавлено {} акций {} для пользователя {}", obj.getValue(), obj.getKey().getTicket(), userId);
        });
    }

    /**
     * Обновляет количество акций в портфеле пользователя.
     * Если итоговое количество акций <= 0, они удаляются из портфеля.
     *
     * @param obj    объект {@link Pair}, содержащий акцию и количество для вычитания
     * @param userId идентификатор пользователя
     */
    public void update(Pair<Stock, Integer> obj, int userId) {
        executeTransaction(session -> {
            Pair<Stock, Integer> current = findByIds(userId, obj.getKey().getId());
            if (current == null) {
                logger.warn("Акции {} пользователя {} не найдены для обновления", obj.getKey().getTicket(), userId);
                return;
            }
            int newAmount = current.getValue() - obj.getValue();
            if (newAmount <= 0) {
                String sqlDelete = "DELETE FROM User_Stock WHERE user_id = :userId AND stock_id = :stockId";
                @SuppressWarnings({"deprecated", "deprecation"})
                NativeQuery<?> deleteQuery = session.createNativeQuery(sqlDelete);
                deleteQuery.setParameter("userId", userId);
                deleteQuery.setParameter("stockId", obj.getKey().getId());
                deleteQuery.executeUpdate();
                logger.info("Акции {} пользователя {} удалены из портфеля", obj.getKey().getTicket(), userId);
            } else {
                String sqlUpdate = "UPDATE User_Stock SET amount = :amount WHERE user_id = :userId AND stock_id = :stockId";
                @SuppressWarnings({"deprecated", "deprecation"})
                NativeQuery<?> updateQuery = session.createNativeQuery(sqlUpdate);
                updateQuery.setParameter("amount", newAmount);
                updateQuery.setParameter("userId", userId);
                updateQuery.setParameter("stockId", obj.getKey().getId());
                updateQuery.executeUpdate();
                logger.info("Обновлено количество акций {} до {} у пользователя {}", obj.getKey().getTicket(), newAmount, userId);
            }
        });
    }

    /**
     * Удаляет акции из портфеля пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param stockId идентификатор акции
     */
    public void delete(int userId, int stockId) {
        executeTransaction(session -> {
            String sql = "DELETE FROM User_Stock WHERE user_id = :userId AND stock_id = :stockId";
            @SuppressWarnings({"deprecated", "deprecation"})
            NativeQuery<?> query = session.createNativeQuery(sql);
            query.setParameter("userId", userId);
            query.setParameter("stockId", stockId);
            query.executeUpdate();
            logger.info("Удалены акции с id={} у пользователя {}", stockId, userId);
        });
    }

    /**
     * Возвращает информацию об акциях пользователя по их ID.
     *
     * @param userId  идентификатор пользователя
     * @param stockId идентификатор акции
     * @return объект {@link Pair} с акцией и количеством или null, если не найдено
     */
    public Pair<Stock, Integer> findByIds(int userId, int stockId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
                SELECT s.id, s.ticket, s.price, s.amount, us.amount
                FROM Stocks s
                JOIN User_Stock us ON s.id = us.stock_id
                WHERE us.user_id = :userId AND us.stock_id = :stockId
            """;
            NativeQuery<Object[]> query = session.createNativeQuery(sql, Object[].class);
            query.setParameter("userId", userId);
            query.setParameter("stockId", stockId);
            Object[] row = query.uniqueResult();
            if (row == null) return null;

            return getStockIntegerPair(row);
        } catch (Exception e) {
            logger.error("Ошибка поиска акций {} пользователя {}", stockId, userId, e);
            return null;
        }
    }

    /**
     * Возвращает список всех акций пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список объектов {@link Pair}, содержащих акции и их количество
     */
    public List<Pair<Stock, Integer>> findAllUserStocks(int userId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
                SELECT s.id, s.ticket, s.price, s.amount, us.amount
                FROM Stocks s
                JOIN User_Stock us ON s.id = us.stock_id
                WHERE us.user_id = :userId
            """;
            NativeQuery<Object[]> query = session.createNativeQuery(sql, Object[].class);
            query.setParameter("userId", userId);
            List<Object[]> rows = query.getResultList();
            if (rows == null || rows.isEmpty()) return Collections.emptyList();

            return rows.stream().map(this::getStockIntegerPair).toList();
        } catch (Exception e) {
            logger.error("Ошибка получения акций пользователя {}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Преобразует строку результата запроса в объект {@link Pair} с акцией и количеством.
     *
     * @param row массив данных из результата SQL-запроса
     * @return объект {@link Pair} с акцией и количеством
     */
    private Pair<Stock, Integer> getStockIntegerPair(Object[] row) {
        Stock stock = new Stock();
        stock.setId(((Number) row[0]).intValue());
        stock.setTicket((String) row[1]);
        stock.setPrice(((Number) row[2]).doubleValue());
        stock.setAmount(((Number) row[3]).intValue());
        int userAmount = ((Number) row[4]).intValue();

        return new Pair<>(stock, userAmount);
    }

    /**
     * Возвращает список всех записей связей пользователей и акций.
     *
     * @return список объектов {@link Pair} с ID пользователя и ID акции
     */
    public List<Pair<Integer, Integer>> findAll() {
        try (Session session = sessionFactory.openSession()) {
            String sql = "SELECT user_id, stock_id FROM User_Stock";
            NativeQuery<Object[]> query = session.createNativeQuery(sql, Object[].class);
            List<Object[]> results = query.getResultList();
            return results.stream()
                    .map(row -> new Pair<>(((Number) row[0]).intValue(), ((Number) row[1]).intValue()))
                    .toList();
        } catch (Exception e) {
            logger.error("Ошибка получения всех записей User_Stock", e);
            return Collections.emptyList();
        }
    }

    /**
     * Возвращает баланс счета пользователя.
     *
     * @param userId идентификатор пользователя
     * @return баланс счета или -1.0 в случае ошибки
     */
    public Double getAccount(int userId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = "SELECT account FROM Accounts WHERE user_id = :userId";
            @SuppressWarnings({"deprecated", "deprecation"})
            NativeQuery<?> query = session.createNativeQuery(sql);
            query.setParameter("userId", userId);
            Object result = query.uniqueResult();

            if (result == null) return -1.0;
            if (result instanceof Number) return ((Number) result).doubleValue();
            return 0.0;
        } catch (Exception e) {
            logger.error("Ошибка получения баланса пользователя {}", userId, e);
            return -1.0;
        }
    }

    /**
     * Устанавливает или обновляет баланс счета пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param account новый баланс
     */
    public void setAccount(int userId, double account) {
        executeTransaction(session -> {
            String sql = """
                INSERT INTO Accounts (user_id, account)
                VALUES (:userId, :account)
                ON CONFLICT (user_id) DO UPDATE SET account = :account
            """;
            @SuppressWarnings({"deprecated", "deprecation"})
            NativeQuery<?> query = session.createNativeQuery(sql);
            query.setParameter("userId", userId);
            query.setParameter("account", Math.round(account * 100.0) / 100.0);
            query.executeUpdate();
            logger.info("Баланс пользователя {} обновлен до {}", userId, account);
        });
    }

    /**
     * Возвращает доступное количество акций для указанного ID акции.
     *
     * @param stockId идентификатор акции
     * @return доступное количество акций или 0 в случае ошибки
     */
    public int getAvailableAmount(int stockId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
                SELECT s.amount - COALESCE(SUM(us.amount), 0) AS available_amount
                FROM Stocks s
                LEFT JOIN User_Stock us ON s.id = us.stock_id
                WHERE s.id = :stockId
                GROUP BY s.amount
            """;
            @SuppressWarnings({"deprecated", "deprecation"})
            NativeQuery<?> query = session.createNativeQuery(sql);
            query.setParameter("stockId", stockId);
            Object result = query.uniqueResult();

            if (result == null) return 0;
            if (result instanceof Number) return ((Number) result).intValue();
            return 0;
        } catch (Exception e) {
            logger.error("Ошибка получения доступного количества акций {}", stockId, e);
            return 0;
        }
    }

    /**
     * Выполняет транзакцию с использованием предоставленного действия.
     *
     * @param action действие, выполняемое в рамках транзакции
     */
    private void executeTransaction(TransactionConsumer action) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (Exception e) {
            logger.error("Ошибка выполнения транзакции", e);
            throw e;
        }
    }

    /**
     * Функциональный интерфейс для выполнения операций в рамках транзакции.
     */
    @FunctionalInterface
    private interface TransactionConsumer {
        /**
         * Выполняет действие с использованием сессии Hibernate.
         *
         * @param session сессия Hibernate
         */
        void accept(Session session);
    }
}