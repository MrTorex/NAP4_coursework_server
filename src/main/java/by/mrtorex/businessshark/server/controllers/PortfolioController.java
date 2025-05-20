package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Deserializer;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.PortfolioService;
import by.mrtorex.businessshark.server.utils.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Контроллер для управления портфелями пользователей.
 * Обеспечивает операции с акциями пользователей и их счетами.
 */
public class PortfolioController {
    private static final Logger logger = LogManager.getLogger(PortfolioController.class);
    private final PortfolioService portfolioService;

    /**
     * Конструктор с внедрением зависимости сервиса.
     *
     * @param portfolioService сервис для работы с портфелями
     */
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
        logger.info("Инициализирован PortfolioController с внешним сервисом");
    }

    /**
     * Конструктор по умолчанию.
     */
    public PortfolioController() {
        this.portfolioService = new PortfolioService();
        logger.info("Инициализирован PortfolioController со стандартным сервисом");
    }

    /**
     * Добавляет акцию в портфель пользователя.
     *
     * @param request запрос с данными акции и пользователя
     * @return результат операции
     */
    public Response addUserStock(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Pair<?,?> data) ||
                    !(data.getKey() instanceof Pair<?, ?> pair) ||
                    !(pair.getKey() instanceof Stock stock) ||
                    !(pair.getValue() instanceof Integer amount) ||
                    !(data.getValue() instanceof Integer userId)) {
                logger.warn("Некорректный формат данных для добавления акции");
                return new Response(false, "Некорректный формат данных акции", null);
            }

            portfolioService.saveEntity(new Pair<>(stock, amount), userId);
            logger.info("Добавлена акция ID {} для пользователя ID {}, количество: {}",
                    stock.getId(), userId, amount);
            return new Response(true, "Акция успешно добавлена", null);
        } catch (ResponseException e) {
            logger.error("Ошибка добавления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Получает акцию из портфеля пользователя.
     *
     * @param request запрос с ID пользователя и акции
     * @return данные об акции
     */
    public Response getUserStock(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Pair<?, ?> data) ||
                    !(data.getValue() instanceof Integer userId) ||
                    !(data.getKey() instanceof Integer stockId)) {
                logger.warn("Некорректный формат данных для получения акции");
                return new Response(false, "Некорректный формат запроса", null);
            }

            Pair<Stock, Integer> result = portfolioService.findEntity(userId, stockId);
            String json = Serializer.toJson(result);
            logger.info("Получена акция ID {} для пользователя ID {}", stockId, userId);
            return new Response(true, "Данные акции получены", json);
        } catch (ResponseException e) {
            logger.error("Ошибка получения акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Обновляет данные акции в портфеле пользователя.
     *
     * @param request запрос с обновленными данными
     * @return результат операции
     */
    public Response updateUserStock(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Pair<?,?> data) ||
                    !(data.getKey() instanceof Pair<?, ?> pair) ||
                    !(pair.getKey() instanceof Stock stock) ||
                    !(pair.getValue() instanceof Integer amount) ||
                    !(data.getValue() instanceof Integer userId)) {
                logger.warn("Некорректный формат данных для обновления акции");
                return new Response(false, "Некорректный формат данных", null);
            }

            portfolioService.updateEntity(new Pair<>(stock, amount), userId);
            logger.info("Обновлена акция ID {} для пользователя ID {}, новое количество: {}",
                    stock.getId(), userId, amount);
            return new Response(true, "Акция успешно обновлена", null);
        } catch (ResponseException e) {
            logger.error("Ошибка обновления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Удаляет акцию из портфеля пользователя.
     *
     * @param request запрос с ID пользователя и акции
     * @return результат операции
     */
    public Response deleteUserStock(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Object[] arr) || arr.length != 2 ||
                    !(arr[0] instanceof Integer userId) ||
                    !(arr[1] instanceof Integer stockId)) {
                logger.warn("Некорректный формат данных для удаления акции");
                return new Response(false, "Некорректный формат запроса", null);
            }

            portfolioService.deleteEntity(userId, stockId);
            logger.info("Удалена акция ID {} из портфеля пользователя ID {}", stockId, userId);
            return new Response(true, "Акция успешно удалена", null);
        } catch (ResponseException e) {
            logger.error("Ошибка удаления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Получает все акции пользователя.
     *
     * @param request запрос с ID пользователя
     * @return список акций
     */
    public Response getAllUserStocks(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Integer userId)) {
                logger.warn("Некорректный формат ID пользователя");
                return new Response(false, "Некорректный ID пользователя", null);
            }

            List<Pair<Stock, Integer>> stocks = portfolioService.findAllUserStocks(userId);
            String json = Serializer.toJson(stocks);
            logger.info("Получено {} акций для пользователя ID {}", stocks.size(), userId);
            return new Response(true, "Список акций получен", json);
        } catch (Exception e) {
            logger.error("Ошибка получения списка акций", e);
            return new Response(false, "Ошибка при получении списка акций", null);
        }
    }

    /**
     * Получает все связи пользователей и акций.
     *
     * @return список всех связей
     */
    public Response getAllUserStockIds() {
        try {
            List<Pair<Integer, Integer>> all = portfolioService.findAll();
            String json = Serializer.toJson(all);
            logger.info("Получено {} связей пользователей и акций", all.size());
            return new Response(true, "Список связей получен", json);
        } catch (Exception e) {
            logger.error("Ошибка получения списка связей", e);
            return new Response(false, "Ошибка при получении списка связей", null);
        }
    }

    /**
     * Получает баланс счета пользователя.
     *
     * @param request запрос с ID пользователя
     * @return баланс счета
     */
    public Response getAccount(Request request) {
        try {
            Integer userId = (Integer) new Deserializer().extractData(request);
            Double account = portfolioService.getAccount(userId);
            String json = Serializer.toJson(account);
            logger.info("Получен баланс для пользователя ID {}: {}", userId, account);
            return new Response(true, "Баланс получен", json);
        } catch (Exception e) {
            logger.error("Ошибка получения баланса", e);
            return new Response(false, "Ошибка при получении баланса", null);
        }
    }

    /**
     * Устанавливает баланс счета пользователя.
     *
     * @param request запрос с ID пользователя и новым балансом
     * @return результат операции
     */
    public Response setAccount(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Pair<?,?> pair) ||
                    !(pair.getKey() instanceof Integer userId) ||
                    !(pair.getValue() instanceof Double account)) {
                logger.warn("Некорректный формат данных для установки баланса");
                return new Response(false, "Некорректный формат данных", null);
            }

            portfolioService.setAccount(userId, account);
            logger.info("Установлен баланс {} для пользователя ID {}", account, userId);
            return new Response(true, "Баланс успешно обновлен", null);
        } catch (ResponseException e) {
            logger.error("Ошибка обновления баланса: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Получает доступное количество акции.
     *
     * @param request запрос с ID акции
     * @return доступное количество
     */
    public Response getStockAvailableAmount(Request request) {
        try {
            Integer stockId = (Integer) new Deserializer().extractData(request);
            int amount = portfolioService.getStockAvailableAmount(stockId);
            String json = Serializer.toJson(amount);
            logger.info("Получено доступное количество акции ID {}: {}", stockId, amount);
            return new Response(true, "Доступное количество получено", json);
        } catch (Exception e) {
            logger.error("Ошибка получения доступного количества акции", e);
            return new Response(false, "Ошибка при получении количества акций", null);
        }
    }
}