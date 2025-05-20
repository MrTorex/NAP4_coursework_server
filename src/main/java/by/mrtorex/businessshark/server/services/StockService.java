package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.repositories.StockDAO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * Сервис для управления сущностями акций.
 * Предоставляет операции поиска, создания, обновления и удаления акций.
 */
public class StockService implements Service<Stock> {
    private static final Logger logger = LogManager.getLogger(StockService.class);
    private final StockDAO stockDAO = new StockDAO();

    /**
     * Поиск акции по идентификатору.
     *
     * @param id идентификатор акции, должен быть положительным
     * @return найденная акция
     * @throws IllegalArgumentException если id <= 0
     */
    @Override
    public Stock findEntity(int id) {
        if (id <= 0) {
            logger.error("Некорректный ID акции: {}", id);
            throw new IllegalArgumentException("ID акции должен быть положительным числом");
        }
        return stockDAO.findById(id);
    }

    /**
     * Сохранение новой акции.
     *
     * @param stock объект акции для сохранения, не может быть null
     * @throws IllegalArgumentException если stock равен null
     */
    @Override
    public void saveEntity(Stock stock) {
        if (stock == null) {
            logger.error("Попытка сохранить null-акцию");
            throw new IllegalArgumentException("Акция не может быть null");
        }
        stockDAO.save(stock);
        logger.info("Сохранена новая акция: {}", stock.getTicket());
    }

    /**
     * Удаление акции.
     *
     * @param stockToDelete акция для удаления, должна иметь идентификатор
     * @throws IllegalArgumentException если акция null или без id
     * @throws ResponseException если акция не найдена
     */
    @Override
    public void deleteEntity(Stock stockToDelete) throws ResponseException {
        if (stockToDelete == null || stockToDelete.getId() == null) {
            logger.error("Попытка удалить акцию с null или отсутствующим ID");
            throw new IllegalArgumentException("Акция для удаления должна иметь ID");
        }
        Stock existingStock = stockDAO.findById(stockToDelete.getId());
        if (existingStock == null) {
            logger.warn("Удаление не удалось: акция с ID {} не найдена", stockToDelete.getId());
            throw new ResponseException("Акция не найдена.");
        }
        stockDAO.delete(existingStock);
        logger.info("Удалена акция с ID {}", stockToDelete.getId());
    }

    /**
     * Обновление существующей акции.
     *
     * @param stock акция с обновлёнными данными, должна иметь идентификатор
     * @throws IllegalArgumentException если акция null или без id
     * @throws ResponseException если акция не существует или тикет уже занят
     */
    public void updateEntity(Stock stock) throws ResponseException {
        if (stock == null || stock.getId() == null) {
            logger.error("Попытка обновить null-акцию или акцию без ID");
            throw new IllegalArgumentException("Акция для обновления должна иметь ID");
        }
        Stock existingStock = stockDAO.findById(stock.getId());
        if (existingStock == null) {
            logger.warn("Обновление не удалось: акция с ID {} не существует", stock.getId());
            throw new ResponseException("Ошибка обновления: акция не существует");
        }

        Stock stockWithSuchTicket = stockDAO.findByTicket(stock.getTicket());
        if (stockWithSuchTicket != null && !Objects.equals(stockWithSuchTicket.getId(), existingStock.getId())) {
            logger.warn("Обновление не удалось: акция с тикетом '{}' уже существует", stock.getTicket());
            throw new ResponseException("Ошибка обновления: акция с таким тикетом уже существует");
        }

        stockDAO.update(stock);
        logger.info("Обновлена акция с ID {}", stock.getId());
    }

    /**
     * Получить список всех акций.
     *
     * @return список акций
     */
    @Override
    public List<Stock> findAllEntities() {
        return stockDAO.findAll();
    }

    /**
     * Получить список всех акций, не связанных с компаниями.
     *
     * @return список акций без компании
     */
    public List<Stock> findAllEntitiesWithNoCompany() {
        return stockDAO.findAllWithNoCompany();
    }

    /**
     * Поиск акции по тикету.
     *
     * @param ticket тикет акции, не может быть пустым или null
     * @return найденная акция
     * @throws IllegalArgumentException если тикет пустой или null
     */
    public Stock findByTicket(String ticket) {
        if (ticket == null || ticket.trim().isEmpty()) {
            logger.error("Попытка найти акцию с пустым или null тикетом");
            throw new IllegalArgumentException("Тикет не может быть пустым или null");
        }
        return stockDAO.findByTicket(ticket);
    }

    /**
     * Создание новой акции.
     *
     * @param stock объект акции для создания, тикет не должен быть пустым или null
     * @return созданная акция
     * @throws IllegalArgumentException если акция или тикет null/пустые
     * @throws ResponseException если акция с таким тикетом уже существует
     */
    public Stock create(Stock stock) throws ResponseException {
        if (stock == null || stock.getTicket() == null || stock.getTicket().trim().isEmpty()) {
            logger.error("Попытка создать акцию с некорректным тикетом");
            throw new IllegalArgumentException("Акция и тикет не могут быть null или пустыми");
        }
        if (stockDAO.findByTicket(stock.getTicket()) != null) {
            logger.warn("Создание не удалось: акция с тикетом '{}' уже существует", stock.getTicket());
            throw new ResponseException("Ошибка создания: акция с таким тикетом уже существует");
        }
        stockDAO.save(stock);
        logger.info("Создана новая акция с тикетом {}", stock.getTicket());
        return stock;
    }
}
