package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.exceptions.ResponseException;
import by.mrtorex.businessshark.server.interfaces.Service;
import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.repositories.StockDAO;

import java.util.List;
import java.util.Objects;

public class StockService implements Service<Stock> {
    private final StockDAO stockDAO = new StockDAO();

    @Override
    public Stock findEntity(int id) {
        return stockDAO.findById(id);
    }

    @Override
    public void saveEntity(Stock stock) {
        stockDAO.save(stock);
    }

    @Override
    public void deleteEntity(Stock stockToDelete) throws ResponseException {
        Stock existingStock = stockDAO.findById(stockToDelete.getId());
        if (existingStock == null) {
            throw new ResponseException("Stock not found.");
        }

        stockDAO.delete(existingStock);
    }

    public void updateEntity(Stock stock) {
        Stock existingStock = stockDAO.findById(stock.getId());

        if (existingStock == null) {
            throw new ResponseException("UPDATE_FAIL: Stock doesn't exist");
        }

        // Check for ticket uniqueness if needed
        Stock stockWithSuchTicket = stockDAO.findByTicket(stock.getTicket());
        if (stockWithSuchTicket != null && !Objects.equals(stockWithSuchTicket.getId(), existingStock.getId())) {
            throw new ResponseException("UPDATE_FAIL: Stock with such ticket already exists");
        }

        stockDAO.update(stock);
    }

    @Override
    public List<Stock> findAllEntities() {
        return stockDAO.findAll();
    }

    public Stock findByTicket(String ticket) {
        return stockDAO.findByTicket(ticket);
    }

    public Stock create(Stock stock) throws ResponseException {
        if (stockDAO.findByTicket(stock.getTicket()) != null) {
            throw new ResponseException("CREATE_FAIL: Stock with this ticket already exists");
        }

        stockDAO.save(stock);
        return stock;
    }
}