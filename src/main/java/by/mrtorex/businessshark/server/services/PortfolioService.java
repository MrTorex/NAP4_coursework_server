package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.repositories.PortfolioDAO;
import by.mrtorex.businessshark.server.utils.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Сервис для управления портфелем пользователя.
 * Обеспечивает операции по работе с акциями и счетом пользователя.
 */
public class PortfolioService {
    private static final Logger logger = LogManager.getLogger(PortfolioService.class);
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();

    /**
     * Находит запись портфеля по ID пользователя и ID акции.
     *
     * @param userId  ID пользователя
     * @param stockId ID акции
     * @return пара (акция, количество)
     * @throws IllegalArgumentException если userId или stockId некорректны
     */
    public Pair<Stock, Integer> findEntity(int userId, int stockId) {
        if (userId <= 0 || stockId <= 0) {
            logger.error("Некорректные ID пользователя {} или акции {}", userId, stockId);
            throw new IllegalArgumentException("ID пользователя и акции должны быть положительными");
        }
        return portfolioDAO.findByIds(userId, stockId);
    }

    /**
     * Сохраняет запись портфеля для пользователя.
     *
     * @param obj    пара (акция, количество)
     * @param userId ID пользователя
     * @throws IllegalArgumentException если obj или userId некорректны
     */
    public void saveEntity(Pair<Stock, Integer> obj, int userId) {
        if (obj == null || userId <= 0) {
            logger.error("Попытка сохранить некорректные данные: obj={}, userId={}", obj, userId);
            throw new IllegalArgumentException("Данные для сохранения некорректны");
        }
        portfolioDAO.save(obj, userId);
        logger.info("Сохранена запись портфеля для пользователя ID {}", userId);
    }

    /**
     * Удаляет запись портфеля по ID пользователя и ID акции.
     *
     * @param userId  ID пользователя
     * @param stockId ID акции
     * @throws IllegalArgumentException если userId или stockId некорректны
     */
    public void deleteEntity(int userId, int stockId) {
        if (userId <= 0 || stockId <= 0) {
            logger.error("Некорректные ID для удаления: userId={}, stockId={}", userId, stockId);
            throw new IllegalArgumentException("ID пользователя и акции должны быть положительными");
        }
        portfolioDAO.delete(userId, stockId);
        logger.info("Удалена запись портфеля пользователя ID {} для акции ID {}", userId, stockId);
    }

    /**
     * Обновляет запись портфеля для пользователя.
     *
     * @param obj    пара (акция, количество)
     * @param userId ID пользователя
     * @throws IllegalArgumentException если obj или userId некорректны
     */
    public void updateEntity(Pair<Stock, Integer> obj, int userId) {
        if (obj == null || userId <= 0) {
            logger.error("Попытка обновления некорректных данных: obj={}, userId={}", obj, userId);
            throw new IllegalArgumentException("Данные для обновления некорректны");
        }
        portfolioDAO.update(obj, userId);
        logger.info("Обновлена запись портфеля пользователя ID {}", userId);
    }

    /**
     * Получает список всех акций пользователя с количеством.
     *
     * @param userId ID пользователя
     * @return список пар (акция, количество)
     * @throws IllegalArgumentException если userId некорректен
     */
    public List<Pair<Stock, Integer>> findAllUserStocks(int userId) {
        if (userId <= 0) {
            logger.error("Некорректный ID пользователя для получения списка акций: {}", userId);
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }
        return portfolioDAO.findAllUserStocks(userId);
    }

    /**
     * Получает список всех записей портфеля всех пользователей.
     *
     * @return список пар (ID акции, количество)
     */
    public List<Pair<Integer, Integer>> findAll() {
        return portfolioDAO.findAll();
    }

    /**
     * Получает текущий баланс счета пользователя.
     *
     * @param userId ID пользователя
     * @return баланс счета или null, если пользователь не найден
     * @throws IllegalArgumentException если userId некорректен
     */
    public Double getAccount(Integer userId) {
        if (userId == null || userId <= 0) {
            logger.error("Некорректный ID пользователя для получения баланса: {}", userId);
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }
        return portfolioDAO.getAccount(userId);
    }

    /**
     * Устанавливает баланс счета пользователя.
     *
     * @param userId  ID пользователя
     * @param account новое значение баланса
     * @throws IllegalArgumentException если userId некорректен или account отрицателен
     */
    public void setAccount(Integer userId, Double account) {
        if (userId == null || userId <= 0) {
            logger.error("Некорректный ID пользователя для установки баланса: {}", userId);
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }
        if (account == null || account < 0) {
            logger.error("Некорректное значение баланса для пользователя ID {}: {}", userId, account);
            throw new IllegalArgumentException("Баланс должен быть неотрицательным");
        }
        portfolioDAO.setAccount(userId, account);
        logger.info("Установлен баланс {} для пользователя ID {}", account, userId);
    }

    /**
     * Получает доступное количество акций по ID акции.
     *
     * @param stockId ID акции
     * @return доступное количество
     * @throws IllegalArgumentException если stockId некорректен
     */
    public int getStockAvailableAmount(Integer stockId) {
        if (stockId == null || stockId <= 0) {
            logger.error("Некорректный ID акции для получения доступного количества: {}", stockId);
            throw new IllegalArgumentException("ID акции должен быть положительным");
        }
        return portfolioDAO.getAvailableAmount(stockId);
    }
}
