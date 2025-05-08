package by.mrtorex.businessshark.server.network;

import by.mrtorex.businessshark.server.controllers.*;
import by.mrtorex.businessshark.server.enums.Operation;
import by.mrtorex.businessshark.server.exceptions.ResponseException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread implements Runnable {
    private final Socket clientSocket;

    private final StockController stockController;
    private final UserController userController;
    private final CompanyController companyController;
    private final RoleController roleController;
    private final CompanyStockController companyStockController;

    public ClientThread(Socket socket) {
        this.clientSocket = socket;
        stockController = new StockController();
        userController = new UserController();
        companyController = new CompanyController();
        roleController = new RoleController();
        companyStockController = new CompanyStockController();
    }

    public void run() {
        try (
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            boolean keepRunning = true;

            while (keepRunning) {
                try {
                    Request request = (Request) input.readObject();

                    if (request != null) {
                        Response response = processRequest(request);
                        if (request.getOperation() == Operation.DISCONNECT) {
                            keepRunning = false;
                        }

                        output.writeObject(response);
                        output.flush();
                    } else {
                        Response errorResponse = new Response(false, "Received invalid object", null);
                        output.writeObject(errorResponse);
                        output.flush();
                    }
                } catch (IOException e) {
                    System.err.println("Connection error: " + e.getMessage());
                    keepRunning = false;
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found: " + e.getMessage());
                    keepRunning = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    keepRunning = false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    private Response processRequest(Request request) {
        try {
            return switch (request.getOperation()) {
                // Stock CRUD
                case CREATE_STOCK -> stockController.createStock(request);
                case READ_STOCK_DATA -> stockController.getStockByTicker(request);
                case UPDATE_STOCK -> stockController.updateStock(request);
                case DELETE_STOCK -> stockController.deleteStock(request);

                // Company CRUD
                case CREATE_COMPANY -> companyController.createCompany(request);
                case READ_COMPANY_DATA -> companyController.getCompanyByName(request);
                case UPDATE_COMPANY -> companyController.updateCompany(request);
                case DELETE_COMPANY -> companyController.deleteCompany(request);

                // User CRUD
                case READ_USER -> userController.readEntity(request);
                case DELETE_USER -> userController.deleteUser(request);
                case UPDATE_USER -> userController.updateEntity(request);

                // GET ALL
                case GET_ALL_COMPANIES -> companyController.getAllCompanies();
                case GET_ALL_USERS -> userController.getAllUsers();
                case GET_ALL_STOCKS -> stockController.getAllStocks();
                case GET_ALL_ROLES -> roleController.getAllRoles();

                // MANY-TO-MANY RELATIONS
                case GET_COMPANY_BY_STOCK -> companyController.getCompanyByStock(request);
                case GET_STOCK_BY_COMPANY -> stockController.getStocksByCompany(request);
                case SEPARATE_STOCK_COMPANY, JOIN_STOCK_COMPANY ->
                        companyStockController.processCompanyStockRelationship(request);

                // SYSTEM
                case LOGIN -> userController.login(request);
                case REGISTER -> userController.register(request);
                case DISCONNECT -> new Response(true, "Disconnected successfully", null);
                default -> new Response(false, "Received unknown operation", null);
            };
        } catch (ResponseException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();

            return new Response(false, "Something went wrong on the server side!", null);
        }
    }

    private void closeConnection() {
        try {
            clientSocket.close();
            Server.decrementClientCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

