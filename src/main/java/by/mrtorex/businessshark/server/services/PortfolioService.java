package by.mrtorex.businessshark.server.services;

import by.mrtorex.businessshark.server.model.entities.Stock;
import by.mrtorex.businessshark.server.repositories.PortfolioDAO;
import by.mrtorex.businessshark.server.utils.Pair;

import java.util.List;

public class PortfolioService {
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();

    public Pair<Stock, Integer> findEntity(int userId, int stockId) {
        return portfolioDAO.findByIds(userId, stockId);
    }

    public void saveEntity(Pair<Stock, Integer> obj, int userId) {
        portfolioDAO.save(obj, userId);
    }

    public void deleteEntity(int userId, int stockId) {
        portfolioDAO.delete(userId, stockId);
    }

    public void updateEntity(Pair<Stock, Integer> obj, int userId) {
        portfolioDAO.update(obj, userId);
    }

    public List<Pair<Stock, Integer>> findAllUserStocks(int userId) {
        return portfolioDAO.findAllUserStocks(userId);
    }

    public List<Pair<Integer, Integer>> findAll() {
        return portfolioDAO.findAll();
    }

    public Double getAccount(Integer userId) {
        return portfolioDAO.getAccount(userId);
    }

    public void setAccount(Integer userId, Double account) {
        portfolioDAO.setAccount(userId, account);
    }

    public int getStockAvailableAmount(Integer stockId) {
        return portfolioDAO.getAvailableAmount(stockId);
    }
}
