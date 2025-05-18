package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Company;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.repositories.CompanyDAO;
import by.mrtorex.businessshark.server.repositories.StockDAO;

import java.util.List;
import java.util.Objects;

public class CompanyService implements Service<Company> {
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final StockDAO stockDAO = new StockDAO();

    @Override
    public Company findEntity(int id) {
        return companyDAO.findById(id);
    }

    @Override
    public void saveEntity(Company company) {
        companyDAO.save(company);
    }

    @Override
    public void deleteEntity(Company companyToDelete) throws ResponseException {
        Company existingCompany = companyDAO.findById(companyToDelete.getId());
        if (existingCompany == null) {
            throw new ResponseException("Company not found.");
        }

        companyDAO.delete(existingCompany);
    }

    @Override
    public void updateEntity(Company company) {
        Company existingCompany = companyDAO.findById(company.getId());

        if (existingCompany == null) {
            throw new ResponseException("UPDATE_FAIL: Company doesn't exist");
        }

        // Check for name uniqueness
        Company companyWithSuchName = companyDAO.findByName(company.getName());
        if (companyWithSuchName != null && !Objects.equals(companyWithSuchName.getId(), existingCompany.getId())) {
            throw new ResponseException("UPDATE_FAIL: Company with such name already exists");
        }

        companyDAO.update(company);
    }

    @Override
    public List<Company> findAllEntities() {
        return companyDAO.findAll();
    }

    public Company findByName(String name) {
        return companyDAO.findByName(name);
    }

    public Company create(Company company) throws ResponseException {
        if (companyDAO.findByName(company.getName()) != null) {
            throw new ResponseException("CREATE_FAIL: Company with this name already exists");
        }

        companyDAO.save(company);
        return company;
    }

    public Company findByStockId(int stockId) throws ResponseException {
        Company company = companyDAO.findByStockId(stockId);
        if (company == null) {
            throw new ResponseException("Company not found for this stock");
        }
        return company;
    }

    public void addStockToCompany(int stockId, int companyId) throws ResponseException {
        Company company = companyDAO.findById(companyId);
        if (company == null) {
            throw new ResponseException("Company not found");
        }

        Stock stock = stockDAO.findById(stockId);
        if (stock == null) {
            throw new ResponseException("Stock not found");
        }

        companyDAO.addStockToCompany(companyId, stockId);
    }

    public void removeStockFromCompany(int stockId) throws ResponseException {
        companyDAO.removeStockFromCompany(stockId);
    }

    public List<Stock> getCompanyStocks(int companyId) throws ResponseException {
        return companyDAO.getCompanyStocks(companyId);
    }
}