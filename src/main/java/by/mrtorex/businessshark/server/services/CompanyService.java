package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Company;
import by.mrtorex.businessshark.server.repositories.CompanyDAO;

import java.util.List;
import java.util.Objects;

public class CompanyService implements Service<Company> {
    private final CompanyDAO companyDAO = new CompanyDAO();

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

        // Check for name uniqueness if needed
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
}
