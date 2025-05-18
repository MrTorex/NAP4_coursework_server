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

public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    public CompanyController() {
        this.companyService = new CompanyService();
    }

    public Response createCompany(Request request) {
        Deserializer deserializer = new Deserializer();
        Object extractedData;

        try {
            extractedData = deserializer.extractData(request);
        } catch (IllegalArgumentException e) {
            return new Response(false, "Invalid company data", null);
        }

        if (!(extractedData instanceof Company company)) {
            return new Response(false, "Invalid company data", null);
        }

        try {
            String createdCompanyJson = Serializer.toJson(companyService.create(company));
            return new Response(true, "Company created successfully", createdCompanyJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllCompanies() {
        try {
            String companiesJson = Serializer.toJson(companyService.findAllEntities());
            return new Response(true, "Companies retrieved successfully", companiesJson);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve companies", null);
        }
    }

    public Response getCompanyByName(Request request) {
        Deserializer deserializer = new Deserializer();
        String companyName = (String) deserializer.extractData(request);

        try {
            Company company = companyService.findByName(companyName);
            if (company != null) {
                String companyJson = Serializer.toJson(company);
                return new Response(true, "Company retrieved successfully", companyJson);
            } else {
                return new Response(false, "Company not found", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getCompanyByStock(Request request) {
        Deserializer deserializer = new Deserializer();
        Integer stockId = (Integer) deserializer.extractData(request);

        try {
            Company company = companyService.findByStockId(stockId);
            if (company != null) {
                String companyJson = Serializer.toJson(company);
                return new Response(true, "Company retrieved successfully", companyJson);
            } else {
                return new Response(false, "Company not found for this stock", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response updateCompany(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Company companyToUpdate)) {
            return new Response(false, "Invalid company data", null);
        }

        try {
            Company existingCompany = companyService.findEntity(companyToUpdate.getId());
            if (existingCompany == null) {
                return new Response(false, "Company not found", null);
            }

            companyService.updateEntity(companyToUpdate);
            return new Response(true, "Company updated successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response deleteCompany(Request request) {
        Deserializer deserializer = new Deserializer();
        int companyToDeleteId = Integer.parseInt((String) deserializer.extractData(request));

        try {
            companyService.deleteEntity(companyService.findEntity(companyToDeleteId));
            return new Response(true, "Company deleted successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response addStockToCompany(Request request) {
        Deserializer deserializer = new Deserializer();
        Pair<Stock, Company> data = (Pair<Stock, Company>) deserializer.extractData(request);

        if (data == null) {
            return new Response(false, "Invalid request data", null);
        }

        Integer companyId = data.getKey().getId();
        Integer stockId = data.getValue().getId();

        try {
            companyService.addStockToCompany(companyId, stockId);
            return new Response(true, "Stock added to company successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response removeStockFromCompany(Request request) {
        Deserializer deserializer = new Deserializer();
        Pair<Stock, Company> data = (Pair<Stock, Company>) deserializer.extractData(request);

        if (data == null) {
            return new Response(false, "Invalid request data", null);
        }

        Integer companyId = data.getValue().getId();
        Integer stockId = data.getKey().getId();

        try {
            companyService.removeStockFromCompany(stockId);
            return new Response(true, "Stock removed from company successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getCompanyStocks(Request request) {
        Deserializer deserializer = new Deserializer();
        Company company = (Company) deserializer.extractData(request);

        try {
            String stocksJson = Serializer.toJson(companyService.getCompanyStocks(company.getId()));
            return new Response(true, "Company stocks retrieved successfully", stocksJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }
}