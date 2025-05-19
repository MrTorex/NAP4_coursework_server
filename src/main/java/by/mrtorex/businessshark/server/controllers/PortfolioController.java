package by.mrtorex.businessshark.server.controllers;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.network.Response;
import by.mrtorex.businessshark.server.serializer.Deserializer;
import by.mrtorex.businessshark.server.serializer.Serializer;
import by.mrtorex.businessshark.server.services.PortfolioService;
import by.mrtorex.businessshark.server.utils.Pair;

import java.util.List;

public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    public PortfolioController() {
        this.portfolioService = new PortfolioService();
    }

    public Response addUserStock(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Pair<?,?> data) ||
                !(data.getKey() instanceof Pair<?, ?> pair) ||
                !(pair.getKey() instanceof Stock stock) ||
                !(pair.getValue() instanceof Integer amount) ||
                !(data.getValue() instanceof Integer userId)) {
            return new Response(false, "Invalid data format for saving stock", null);
        }

        try {
            portfolioService.saveEntity(new Pair<>(stock, amount), userId);
            return new Response(true, "Stock saved successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getUserStock(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Pair<?, ?> data) ||
            !(data.getValue() instanceof Integer userId) ||
            !(data.getKey() instanceof Integer stockId)) {
            return new Response(false, "Invalid data format for retrieving stock", null);
        }

        try {
            Pair<Stock, Integer> result = portfolioService.findEntity(userId, stockId);
            String json = Serializer.toJson(result);
            return new Response(true, "Stock retrieved successfully", json);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response updateUserStock(Request request) {
        System.out.println(request);
        Object extractedData = new Deserializer().extractData(request);
        System.out.println(extractedData);

        if (!(extractedData instanceof Pair<?,?> data) ||
            !(data.getKey() instanceof Pair<?, ?> pair) ||
            !(pair.getKey() instanceof Stock stock) ||
            !(pair.getValue() instanceof Integer amount) ||
            !(data.getValue() instanceof Integer userId)) {
            return new Response(false, "Invalid data format for updating stock", null);
        }

        try {
            portfolioService.updateEntity(new Pair<>(stock, amount), userId);
            return new Response(true, "Stock updated successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response deleteUserStock(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Object[] arr) ||
            arr.length != 2 ||
            !(arr[0] instanceof Integer userId) ||
            !(arr[1] instanceof Integer stockId)) {
            return new Response(false, "Invalid data format for deleting stock", null);
        }

        try {
            portfolioService.deleteEntity(userId, stockId);
            return new Response(true, "Stock deleted successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getAllUserStocks(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Integer userId)) {
            return new Response(false, "Invalid user ID", null);
        }

        try {
            List<Pair<Stock, Integer>> stocks = portfolioService.findAllUserStocks(userId);
            System.out.println("ZALUPA: " + stocks);
            String json = Serializer.toJson(stocks);
            System.out.println("JSON: " + json);
            return new Response(true, "User stocks retrieved successfully", json);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(false, "Failed to retrieve user stocks", null);
        }
    }

    public Response getAllUserStockIds() {
        try {
            List<Pair<Integer, Integer>> all = portfolioService.findAll();
            String json = Serializer.toJson(all);
            return new Response(true, "All user-stock pairs retrieved", json);
        } catch (Exception e) {
            return new Response(false, "Failed to retrieve all stock relations", null);
        }
    }

    public Response getAccount(Request request) {
        try {
            Deserializer deserializer = new Deserializer();
            Integer userId = (Integer) deserializer.extractData(request);

            Double account = portfolioService.getAccount(userId);
            String json = Serializer.toJson(account);
            return new Response(true, "Account was sent", json);
        } catch (Exception e) {
            return new Response(false, "Failed to retrieve user account", null);
        }
    }

    public Response setAccount(Request request) {
        Object extractedData = new Deserializer().extractData(request);

        if (!(extractedData instanceof Pair<?,?> pair) || !(pair.getKey() instanceof Integer userId) ||
              !(pair.getValue() instanceof Double account)) {
            return new Response(false, "Invalid data format for setting account", null);
        }

        try {
            portfolioService.setAccount(userId, account);
            return new Response(true, "Account updated successfully", null);
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response getStockAvailableAmount(Request request) {
        try {
            Deserializer deserializer = new Deserializer();
            Integer stockId = (Integer) deserializer.extractData(request);

            int amount = portfolioService.getStockAvailableAmount(stockId);
            String json = Serializer.toJson(amount);
            return new Response(true, "Amount was sent", json);
        } catch (Exception e) {
            return new Response(false, "Failed to retrieve stock aviable amount", null);
        }
    }
}
