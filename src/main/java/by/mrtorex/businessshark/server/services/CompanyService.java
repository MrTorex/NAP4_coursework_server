package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Company;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.repositories.CompanyDAO;
import by.mrtorex.businessshark.server.repositories.StockDAO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * Сервис для управления бизнес-логикой компаний.
 * Выполняет операции CRUD и управление связью с акциями.
 */
public class CompanyService implements Service<Company> {
    private static final Logger logger = LogManager.getLogger(CompanyService.class);
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final StockDAO stockDAO = new StockDAO();

    /**
     * Находит компанию по ID.
     *
     * @param id идентификатор компании
     * @return найденная компания или null, если не найдена
     */
    @Override
    public Company findEntity(int id) {
        return companyDAO.findById(id);
    }

    /**
     * Сохраняет новую компанию.
     *
     * @param company компания для сохранения
     */
    @Override
    public void saveEntity(Company company) {
        companyDAO.save(company);
        logger.info("Компания сохранена: {}", company.getName());
    }

    /**
     * Удаляет компанию.
     *
     * @param companyToDelete компания для удаления
     * @throws ResponseException если компания не найдена
     */
    @Override
    public void deleteEntity(Company companyToDelete) throws ResponseException {
        Company existingCompany = companyDAO.findById(companyToDelete.getId());
        if (existingCompany == null) {
            logger.error("Удаление компании не выполнено: компания с ID {} не найдена", companyToDelete.getId());
            throw new ResponseException("Компания не найдена.");
        }
        companyDAO.delete(existingCompany);
        logger.info("Компания удалена: ID {}", companyToDelete.getId());
    }

    /**
     * Обновляет данные компании.
     *
     * @param company компания с обновлёнными данными
     * @throws ResponseException если компания не найдена или имя уже занято
     */
    @Override
    public void updateEntity(Company company) throws ResponseException {
        Company existingCompany = companyDAO.findById(company.getId());
        if (existingCompany == null) {
            logger.error("Обновление компании не выполнено: компания с ID {} не существует", company.getId());
            throw new ResponseException("Ошибка обновления: компания не существует");
        }

        Company companyWithSuchName = companyDAO.findByName(company.getName());
        if (companyWithSuchName != null && !Objects.equals(companyWithSuchName.getId(), existingCompany.getId())) {
            logger.error("Обновление компании не выполнено: имя '{}' уже занято", company.getName());
            throw new ResponseException("Ошибка обновления: компания с таким именем уже существует");
        }

        companyDAO.update(company);
        logger.info("Данные компании обновлены: ID {}", company.getId());
    }

    /**
     * Получает список всех компаний.
     *
     * @return список компаний
     */
    @Override
    public List<Company> findAllEntities() {
        return companyDAO.findAll();
    }

    /**
     * Находит компанию по имени.
     *
     * @param name имя компании
     * @return компания или null, если не найдена
     */
    public Company findByName(String name) {
        return companyDAO.findByName(name);
    }

    /**
     * Создаёт новую компанию.
     *
     * @param company новая компания
     * @return созданная компания
     * @throws ResponseException если компания с таким именем уже существует
     */
    public Company create(Company company) throws ResponseException {
        if (companyDAO.findByName(company.getName()) != null) {
            logger.error("Создание компании не выполнено: имя '{}' уже занято", company.getName());
            throw new ResponseException("Ошибка создания: компания с таким именем уже существует");
        }
        companyDAO.save(company);
        logger.info("Создана новая компания: {}", company.getName());
        return company;
    }

    /**
     * Находит компанию по ID акции.
     *
     * @param stockId ID акции
     * @return компания
     * @throws ResponseException если компания не найдена
     */
    public Company findByStockId(int stockId) throws ResponseException {
        Company company = companyDAO.findByStockId(stockId);
        if (company == null) {
            logger.error("Компания не найдена для акции с ID {}", stockId);
            throw new ResponseException("Компания для данной акции не найдена");
        }
        return company;
    }

    /**
     * Добавляет акцию к компании.
     *
     * @param stockId ID акции
     * @param companyId ID компании
     * @throws ResponseException если компания или акция не найдены
     */
    public void addStockToCompany(int stockId, int companyId) throws ResponseException {
        Company company = companyDAO.findById(companyId);
        if (company == null) {
            logger.error("Добавление акции не выполнено: компания с ID {} не найдена", companyId);
            throw new ResponseException("Компания не найдена");
        }

        Stock stock = stockDAO.findById(stockId);
        if (stock == null) {
            logger.error("Добавление акции не выполнено: акция с ID {} не найдена", stockId);
            throw new ResponseException("Акция не найдена");
        }

        companyDAO.addStockToCompany(companyId, stockId);
        logger.info("Акция с ID {} добавлена в компанию с ID {}", stockId, companyId);
    }

    /**
     * Удаляет связь акции с компанией.
     *
     * @param stockId ID акции
     * @throws ResponseException если операция не удалась
     */
    public void removeStockFromCompany(int stockId) throws ResponseException {
        companyDAO.removeStockFromCompany(stockId);
        logger.info("Акция с ID {} отвязана от компании", stockId);
    }

    /**
     * Получает список акций компании.
     *
     * @param companyId ID компании
     * @return список акций
     * @throws ResponseException если операция не удалась
     */
    public List<Stock> getCompanyStocks(int companyId) throws ResponseException {
        return companyDAO.getCompanyStocks(companyId);
    }
}
