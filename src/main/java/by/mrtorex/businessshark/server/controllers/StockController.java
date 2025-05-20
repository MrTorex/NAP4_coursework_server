package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Deserializer;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.StockService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Контроллер для управления операциями с акциями.
 * Обеспечивает взаимодействие между сетевым интерфейсом и сервисом акций.
 */
public class StockController {
    private static final Logger logger = LogManager.getLogger(StockController.class);
    private final StockService stockService;

    /**
     * Конструктор с внедрением зависимости сервиса.
     *
     * @param stockService сервис для работы с акциями
     */
    public StockController(StockService stockService) {
        this.stockService = stockService;
        logger.info("Инициализирован StockController с внешним сервисом");
    }

    /**
     * Конструктор по умолчанию.
     */
    public StockController() {
        this.stockService = new StockService();
        logger.info("Инициализирован StockController со стандартным сервисом");
    }

    /**
     * Создает новую акцию.
     *
     * @param request запрос с данными акции
     * @return ответ с результатом операции
     */
    public Response createStock(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Stock stock)) {
                logger.warn("Попытка создания акции с некорректными данными");
                return new Response(false, "Некорректные данные акции", null);
            }

            String createdStockJson = Serializer.toJson(stockService.create(stock));
            logger.info("Создана новая акция: {}", stock.getTicket());
            return new Response(true, "Акция успешно создана", createdStockJson);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка формата данных при создании акции", e);
            return new Response(false, "Ошибка формата данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка создания акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Получает акцию по тикеру.
     *
     * @param request запрос с тикером акции
     * @return ответ с данными акции
     */
    public Response getStockByTicker(Request request) {
        try {
            String ticker = (String) new Deserializer().extractData(request);
            Stock stock = stockService.findByTicket(ticker);

            if (stock == null) {
                logger.warn("Акция с тикером {} не найдена", ticker);
                return new Response(false, "Акция не найдена", null);
            }

            String stockJson = Serializer.toJson(stock);
            logger.info("Найдена акция по тикеру: {}", ticker);
            return new Response(true, "Данные акции получены", stockJson);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат тикера", e);
            return new Response(false, "Некорректный формат тикера", null);
        } catch (ResponseException e) {
            logger.error("Ошибка поиска акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Получает список всех акций.
     *
     * @return ответ со списком акций
     */
    public Response getAllStocks() {
        try {
            var stocks = stockService.findAllEntities();
            String stocksJson = Serializer.toJson(stocks);
            logger.info("Запрошен список всех акций. Найдено {} записей", stocks.size());
            return new Response(true, "Список акций получен", stocksJson);
        } catch (Exception e) {
            logger.error("Ошибка получения списка акций", e);
            return new Response(false, "Ошибка при получении списка акций", null);
        }
    }

    /**
     * Получает список акций без привязки к компании.
     *
     * @return ответ со списком акций
     */
    public Response getAllStocksWithNoCompany() {
        try {
            var stocks = stockService.findAllEntitiesWithNoCompany();
            String stocksJson = Serializer.toJson(stocks);
            logger.info("Запрошен список свободных акций. Найдено {} записей", stocks.size());
            return new Response(true, "Список свободных акций получен", stocksJson);
        } catch (Exception e) {
            logger.error("Ошибка получения списка свободных акций", e);
            return new Response(false, "Ошибка при получении списка свободных акций", null);
        }
    }

    /**
     * Обновляет данные акции.
     *
     * @param request запрос с обновленными данными акции
     * @return результат операции
     */
    public Response updateStock(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Stock stockToUpdate)) {
                logger.warn("Попытка обновления акции с некорректными данными");
                return new Response(false, "Некорректные данные акции", null);
            }

            Stock existingStock = stockService.findEntity(stockToUpdate.getId());
            if (existingStock == null) {
                logger.warn("Попытка обновления несуществующей акции ID {}", stockToUpdate.getId());
                return new Response(false, "Акция не найдена", null);
            }

            stockService.updateEntity(stockToUpdate);
            logger.info("Обновлены данные акции ID {}", stockToUpdate.getId());
            return new Response(true, "Данные акции обновлены", null);
        } catch (ResponseException e) {
            logger.error("Ошибка обновления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Удаляет акцию.
     *
     * @param request запрос с ID акции для удаления
     * @return результат операции
     */
    public Response deleteStock(Request request) {
        try {
            Integer stockId = (Integer) new Deserializer().extractData(request);
            stockService.deleteEntity(stockService.findEntity(stockId));
            logger.info("Удалена акция ID {}", stockId);
            return new Response(true, "Акция удалена", null);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат ID акции", e);
            return new Response(false, "Некорректный ID акции", null);
        } catch (ResponseException e) {
            logger.error("Ошибка удаления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }
}