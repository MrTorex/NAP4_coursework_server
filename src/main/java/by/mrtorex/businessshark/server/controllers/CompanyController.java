package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.model.entities.Company;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Deserializer;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.CompanyService;
import by.mrtorex.businessshark.server.utils.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Контроллер для управления операциями с компаниями.
 * Обеспечивает взаимодействие между сетевым интерфейсом и сервисным слоем.
 */
public class CompanyController {
    private static final Logger logger = LogManager.getLogger(CompanyController.class);
    private final CompanyService companyService;

    /**
     * Конструктор с внедрением зависимости сервиса.
     *
     * @param companyService сервис для работы с данными компаний
     */
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
        logger.info("Инициализирован CompanyController с внешним сервисом");
    }

    /**
     * Конструктор по умолчанию.
     */
    public CompanyController() {
        this.companyService = new CompanyService();
        logger.info("Инициализирован CompanyController со стандартным сервисом");
    }

    /**
     * Создает новую компанию.
     *
     * @param request запрос с данными компании
     * @return ответ с результатом операции
     */
    public Response createCompany(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Company company)) {
                logger.warn("Попытка создания компании с некорректными данными");
                return new Response(false, "Некорректные данные компании", null);
            }

            String createdCompanyJson = Serializer.toJson(companyService.create(company));
            logger.info("Создана новая компания: ID {}", company.getId());
            return new Response(true, "Компания успешно создана", createdCompanyJson);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка десериализации данных компании", e);
            return new Response(false, "Ошибка формата данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка создания компании: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Возвращает список всех компаний.
     *
     * @return ответ со списком компаний
     */
    public Response getAllCompanies() {
        try {
            var companies = companyService.findAllEntities();
            String companiesJson = Serializer.toJson(companies);
            logger.info("Запрошен список компаний. Найдено {} записей", companies.size());
            return new Response(true, "Список компаний получен", companiesJson);
        } catch (Exception e) {
            logger.error("Ошибка получения списка компаний", e);
            return new Response(false, "Ошибка при получении списка компаний", null);
        }
    }

    /**
     * Находит компанию по названию.
     *
     * @param request запрос с названием компании
     * @return ответ с данными компании
     */
    public Response getCompanyByName(Request request) {
        try {
            String companyName = (String) new Deserializer().extractData(request);
            Company company = companyService.findByName(companyName);

            if (company == null) {
                logger.warn("Компания не найдена: {}", companyName);
                return new Response(false, "Компания не найдена", null);
            }

            String companyJson = Serializer.toJson(company);
            logger.info("Найдена компания: {}", companyName);
            return new Response(true, "Данные компании получены", companyJson);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат названия компании", e);
            return new Response(false, "Некорректный формат данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка поиска компании: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Находит компанию по идентификатору акции.
     *
     * @param request запрос с ID акции
     * @return ответ с данными компании
     */
    public Response getCompanyByStock(Request request) {
        try {
            Integer stockId = (Integer) new Deserializer().extractData(request);
            Company company = companyService.findByStockId(stockId);

            if (company == null) {
                logger.warn("Компания для акции {} не найдена", stockId);
                return new Response(false, "Компания не найдена", null);
            }

            String companyJson = Serializer.toJson(company);
            logger.info("Найдена компания для акции ID {}", stockId);
            return new Response(true, "Данные компании получены", companyJson);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат ID акции", e);
            return new Response(false, "Некорректный формат данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка поиска компании по акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Обновляет данные компании.
     *
     * @param request запрос с обновленными данными компании
     * @return результат операции
     */
    public Response updateCompany(Request request) {
        try {
            Object extractedData = new Deserializer().extractData(request);

            if (!(extractedData instanceof Company companyToUpdate)) {
                logger.warn("Попытка обновления компании с некорректными данными");
                return new Response(false, "Некорректные данные компании", null);
            }

            Company existingCompany = companyService.findEntity(companyToUpdate.getId());
            if (existingCompany == null) {
                logger.warn("Попытка обновления несуществующей компании ID {}", companyToUpdate.getId());
                return new Response(false, "Компания не найдена", null);
            }

            companyService.updateEntity(companyToUpdate);
            logger.info("Обновлены данные компании ID {}", companyToUpdate.getId());
            return new Response(true, "Данные компании обновлены", null);
        } catch (ResponseException e) {
            logger.error("Ошибка обновления компании: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Удаляет компанию.
     *
     * @param request запрос с ID компании для удаления
     * @return результат операции
     */
    public Response deleteCompany(Request request) {
        try {
            int companyId = Integer.parseInt((String) new Deserializer().extractData(request));
            companyService.deleteEntity(companyService.findEntity(companyId));
            logger.info("Удалена компания ID {}", companyId);
            return new Response(true, "Компания удалена", null);
        } catch (NumberFormatException e) {
            logger.error("Некорректный формат ID компании", e);
            return new Response(false, "Некорректный ID компании", null);
        } catch (ResponseException e) {
            logger.error("Ошибка удаления компании: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Добавляет акцию к компании.
     *
     * @param request запрос с данными акции и компании
     * @return результат операции
     */
    public Response addStockToCompany(Request request) {
        try {
            @SuppressWarnings("unchecked")
            Pair<Stock, Company> data = (Pair<Stock, Company>) new Deserializer().extractData(request);

            if (data == null) {
                logger.warn("Попытка добавления акции с некорректными данными");
                return new Response(false, "Некорректные данные", null);
            }

            Integer companyId = data.getValue().getId();
            Integer stockId = data.getKey().getId();

            companyService.addStockToCompany(stockId, companyId);
            logger.info("Акция {} добавлена к компании {}", stockId, companyId);
            return new Response(true, "Акция добавлена к компании", null);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат данных акции и компании", e);
            return new Response(false, "Некорректный формат данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка добавления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Удаляет акцию из компании.
     *
     * @param request запрос с данными акции и компании
     * @return результат операции
     */
    public Response removeStockFromCompany(Request request) {
        try {
            @SuppressWarnings("unchecked")
            Pair<Stock, Company> data = (Pair<Stock, Company>) new Deserializer().extractData(request);

            if (data == null) {
                logger.warn("Попытка удаления акции с некорректными данными");
                return new Response(false, "Некорректные данные", null);
            }

            Integer stockId = data.getKey().getId();
            companyService.removeStockFromCompany(stockId);
            logger.info("Акция {} удалена из компании", stockId);
            return new Response(true, "Акция удалена из компании", null);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат данных акции", e);
            return new Response(false, "Некорректный формат данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка удаления акции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }

    /**
     * Возвращает список акций компании.
     *
     * @param request запрос с данными компании
     * @return ответ со списком акций
     */
    public Response getCompanyStocks(Request request) {
        try {
            Company company = (Company) new Deserializer().extractData(request);
            String stocksJson = Serializer.toJson(companyService.getCompanyStocks(company.getId()));
            logger.info("Запрошены акции компании ID {}", company.getId());
            return new Response(true, "Список акций получен", stocksJson);
        } catch (ClassCastException e) {
            logger.error("Некорректный формат данных компании", e);
            return new Response(false, "Некорректный формат данных", null);
        } catch (ResponseException e) {
            logger.error("Ошибка получения акций компании: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        }
    }
}