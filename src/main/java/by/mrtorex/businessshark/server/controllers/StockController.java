package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Deserializer;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.StockService;

public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    public StockController() {
        this.stockService = new StockService();
    }

    public Response createStock(Request request) {
        Deserializer deserializer = new Deserializer();
        Object extractedData;

        try {
            extractedData = deserializer.extractData(request);
        } catch (IllegalArgumentException e) {
            return new Response(false, "Invalid stock data", null);
        }

        if (!(extractedData instanceof Stock stock)) {
            return new Response(false, "Invalid stock data", null);
        }

        try {
            String createdStockJson = Serializer.toJson(stockService.create(stock));
            return new Response(true, "Stock created successfully", createdStockJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getStockByTicker(Request request) {
        Deserializer deserializer = new Deserializer();
        String ticker = (String) deserializer.extractData(request);

        try {
            Stock stock = stockService.findByTicket(ticker);
            if (stock != null) {
                String stockJson = Serializer.toJson(stock);
                return new Response(true, "Stock retrieved successfully", stockJson);
            } else {
                return new Response(false, "Stock not found", null);
            }
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllStocks() {
        try {
            String stocksJson = Serializer.toJson(stockService.findAllEntities());
            return new Response(true, "Stocks retrieved successfully", stocksJson);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Failed to retrieve stocks", null);
        }
    }

    public Response getStocksByCompany(Request request) {
        /*Deserializer deserializer = new Deserializer();
        String companyName = (String) deserializer.extractData(request);

        try {
            String stocksJson = Serializer.toJson(stockService.findByCompanyName(companyName));
            return new Response(true, "Stocks retrieved successfully", stocksJson);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }*/
        return new Response(true, "INDEV", null);
    }

    public Response updateStock(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Stock stockToUpdate)) {
            return new Response(false, "Invalid stock data", null);
        }

        try {
            Stock existingStock = stockService.findEntity(stockToUpdate.getId());
            if (existingStock == null) {
                return new Response(false, "Stock not found", null);
            }

            stockService.updateEntity(stockToUpdate);
            return new Response(true, "Stock updated successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response deleteStock(Request request) {
        Deserializer deserializer = new Deserializer();
        Integer stockToDeleteId = (Integer) deserializer.extractData(request);

        try {
            stockService.deleteEntity(stockService.findEntity(stockToDeleteId));
            return new Response(true, "Stock deleted successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }
}
