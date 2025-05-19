package by.mrtorex.businessshark.server.repositories;

import by.mrtorex.businessshark.server.config.SessionConfig;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.utils.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class PortfolioDAO {
    private final SessionFactory sessionFactory;

    public PortfolioDAO() {
        this.sessionFactory = SessionConfig.getInstance().getSessionFactory();
    }

    public void save(Pair<Stock, Integer> obj, int userId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Stock stock = obj.getKey();
            int amount = obj.getValue();

            // Обновим или вставим запись в User_Stock
            String sql = """
                INSERT INTO User_Stock (user_id, stock_id, amount)
                VALUES (:userId, :stockId, :amount)
                ON CONFLICT (user_id, stock_id)
                DO UPDATE SET amount = User_Stock.amount + :amount;
            """;

            session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("stockId", stock.getId())
                    .setParameter("amount", amount)
                    .executeUpdate();

            tx.commit();
        }
    }

    public void update(Pair<Stock, Integer> obj, int userId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Stock stock = obj.getKey();
            int amount = this.findByIds(userId, stock.getId()).getValue() - obj.getValue();
            String sql;

            if (amount == 0)
            {
                sql = """
                DELETE FROM User_Stock
                WHERE user_id = :userId AND stock_id = :stockId;
            """;
                session.createNativeQuery(sql)
                        .setParameter("userId", userId)
                        .setParameter("stockId", stock.getId())
                        .executeUpdate();
            }
            else
            {
                sql = """
                UPDATE User_Stock
                SET amount = :amount
                WHERE user_id = :userId AND stock_id = :stockId;
            """;
                session.createNativeQuery(sql)
                        .setParameter("userId", userId)
                        .setParameter("stockId", stock.getId())
                        .setParameter("amount", amount)
                        .executeUpdate();
            }



            tx.commit();
        }
    }

    public void delete(int userId, int stockId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            String sql = """
                DELETE FROM User_Stock
                WHERE user_id = :userId AND stock_id = :stockId;
            """;

            session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("stockId", stockId)
                    .executeUpdate();

            tx.commit();
        }
    }

    public Pair<Stock, Integer> findByIds(int userId, int stockId) {
        System.out.println("User: " + userId + " Stock: " + stockId);
        try (Session session = sessionFactory.openSession()) {
            String sql = """
            SELECT s.id, s.ticket, s.price, s.amount, us.amount AS user_amount
            FROM Stocks s
            JOIN User_Stock us ON s.id = us.stock_id
            WHERE us.user_id = :userId AND us.stock_id = :stockId
        """;

            Object[] row = (Object[]) session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("stockId", stockId)
                    .getSingleResult();

            // Восстанавливаем Stock вручную
            Stock stock = new Stock();
            stock.setId((Integer) row[0]);
            stock.setTicket((String) row[1]);
            stock.setPrice((Double) row[2]);
            stock.setAmount((Integer) row[3]);

            Integer userStockAmount = (Integer) row[4];

            return new Pair<>(stock, userStockAmount);
        }
    }

    public List<Pair<Stock, Integer>> findAllUserStocks(int userId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
            SELECT 
                s.id AS s_id,
                s.ticket AS s_ticket,
                s.price AS s_price,
                s.amount AS s_amount,
                us.amount AS user_amount
            FROM Stocks s
            JOIN User_Stock us ON s.id = us.stock_id
            WHERE us.user_id = :userId
        """;

            List<Object[]> results = session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .getResultList();

            List<Pair<Stock, Integer>> stocks = results.stream().map(row -> {
                Stock stock = new Stock();
                stock.setId(((Number) row[0]).intValue());
                stock.setTicket((String) row[1]);
                stock.setPrice(((Number) row[2]).doubleValue());
                stock.setAmount(((Number) row[3]).intValue());

                Integer userAmount = ((Number) row[4]).intValue();

                return new Pair<>(stock, userAmount);
            }).toList();

            System.out.println(stocks);
            return stocks;
        }
    }

    public List<Pair<Integer, Integer>> findAll() {
        try (Session session = sessionFactory.openSession()) {
            String sql = "SELECT user_id, stock_id FROM User_Stock";
            List<Object[]> results = session.createNativeQuery(sql).getResultList();

            return results.stream()
                    .map(row -> new Pair<>((Integer) row[0], (Integer) row[1]))
                    .toList();
        }
    }

    public Double getAccount(int userId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = "SELECT account FROM Accounts WHERE user_id = :userId";

            Object result = session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (result == null) {
                return 0.0;
            }

            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }

            return 0.0;
        }
    }

    public void setAccount(int userId, double account) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            System.out.println("User: " + userId + " Account: " + account);

            // Вставка или обновление записи в таблице Accounts
            String sql = """
            INSERT INTO Accounts (user_id, account)
            VALUES (:userId, :account)
            ON CONFLICT (user_id)
            DO UPDATE SET account = :account;
        """;

            session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("account", Math.round(account * 100.0) / 100.0)
                    .executeUpdate();

            tx.commit();
        }
    }

    public int getAvailableAmount(int stockId) {
        try (Session session = sessionFactory.openSession()) {
            String sql = """
            SELECT
                s.amount - COALESCE(SUM(us.amount), 0) AS available_amount
            FROM Stocks s
            LEFT JOIN User_Stock us ON s.id = us.stock_id
            WHERE s.id = :stockId
            GROUP BY s.amount
            """;

            Object result = session.createNativeQuery(sql)
                    .setParameter("stockId", stockId)
                    .uniqueResult();

            if (result == null) {
                return 0;
            }

            if (result instanceof Number) {
                return ((Number) result).intValue();
            }

            // Иногда Hibernate возвращает Object[] для выборок с несколькими колонками,
            // но здесь выбирается одна колонка, так что это скорее не понадобится.
            if (result instanceof Object[] array && array.length > 0 && array[0] instanceof Number) {
                return ((Number) array[0]).intValue();
            }

            return 0;
        }
    }
}
