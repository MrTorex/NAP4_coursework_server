package by.mrtorex.businessshark.server.network;

import by.mrtorex.businessshark.server.controllers.*;
import by.mrtorex.businessshark.server.enums.Operation;
import by.mrtorex.businessshark.server.exceptions.ResponseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Поток обработки клиентских подключений.
 * Обрабатывает запросы от клиента и взаимодействует с контроллерами для выполнения операций.
 */
public class ClientThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientThread.class);

    private final Socket clientSocket;
    private final StockController stockController;
    private final UserController userController;
    private final CompanyController companyController;
    private final RoleController roleController;
    private final PortfolioController portfolioController;

    /**
     * Конструктор потока с инициализацией контроллеров.
     *
     * @param socket сокет клиента
     */
    public ClientThread(Socket socket) {
        this.clientSocket = socket;
        stockController = new StockController();
        userController = new UserController();
        companyController = new CompanyController();
        roleController = new RoleController();
        portfolioController = new PortfolioController();
        logger.info("Инициализирован новый клиентский поток для сокета {}", socket);
    }

    /**
     * Основной цикл обработки запросов клиента.
     * Принимает запросы, обрабатывает их и отправляет ответы.
     */
    @Override
    public void run() {
        try (
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            boolean keepRunning = true;

            while (keepRunning) {
                try {
                    Object obj = input.readObject();
                    if (!(obj instanceof Request request)) {
                        logger.warn("Получен некорректный объект вместо запроса от клиента");
                        Response errorResponse = new Response(false, "Получен некорректный объект запроса", null);
                        output.writeObject(errorResponse);
                        output.flush();
                        continue;
                    }

                    Response response = processRequest(request);
                    if (request.getOperation() == Operation.DISCONNECT) {
                        keepRunning = false;
                    }

                    output.writeObject(response);
                    output.flush();
                } catch (IOException e) {
                    logger.error("Ошибка соединения с клиентом: {}", e.getMessage());
                    keepRunning = false;
                } catch (ClassNotFoundException e) {
                    logger.error("Класс запроса не найден: {}", e.getMessage());
                    keepRunning = false;
                } catch (Exception e) {
                    logger.error("Необработанная ошибка при обработке запроса", e);
                    keepRunning = false;
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка инициализации потоков ввода-вывода клиента", e);
            throw new RuntimeException("Ошибка инициализации клиентского потока", e);
        } finally {
            closeConnection();
        }
    }

    /**
     * Обрабатывает запрос, вызывая соответствующие методы контроллеров.
     *
     * @param request объект запроса от клиента
     * @return ответ на запрос
     */
    private Response processRequest(Request request) {
        try {
            return switch (request.getOperation()) {
                case CREATE_STOCK -> stockController.createStock(request);
                case READ_STOCK_DATA -> stockController.getStockByTicker(request);
                case UPDATE_STOCK -> stockController.updateStock(request);
                case DELETE_STOCK -> stockController.deleteStock(request);

                case CREATE_COMPANY -> companyController.createCompany(request);
                case READ_COMPANY_DATA -> companyController.getCompanyByName(request);
                case UPDATE_COMPANY -> companyController.updateCompany(request);
                case DELETE_COMPANY -> companyController.deleteCompany(request);

                case READ_USER -> userController.readEntity(request);
                case DELETE_USER -> userController.deleteUser(request);
                case UPDATE_USER -> userController.updateEntity(request);
                case CREATE_USER, REGISTER -> userController.register(request);

                case GET_ALL_COMPANIES -> companyController.getAllCompanies();
                case GET_ALL_USERS -> userController.getAllUsers();
                case GET_ALL_STOCKS -> stockController.getAllStocks();
                case GET_ALL_ROLES -> roleController.getAllRoles();
                case GET_ALL_STOCKS_WITH_NO_COMPANY -> stockController.getAllStocksWithNoCompany();

                case GET_COMPANY_BY_STOCK -> companyController.getCompanyByStock(request);
                case GET_STOCKS_BY_COMPANY -> companyController.getCompanyStocks(request);
                case JOIN_STOCK_COMPANY -> companyController.addStockToCompany(request);
                case SEPARATE_STOCK_COMPANY -> companyController.removeStockFromCompany(request);

                case ADD_USER_STOCK -> portfolioController.addUserStock(request);
                case GET_USER_STOCK -> portfolioController.getUserStock(request);
                case UPDATE_USER_STOCK -> portfolioController.updateUserStock(request);
                case DELETE_USER_STOCK -> portfolioController.deleteUserStock(request);
                case GET_ALL_USER_STOCKS -> portfolioController.getAllUserStocks(request);
                case GET_ALL_USER_STOCK_IDS -> portfolioController.getAllUserStockIds();
                case GET_USER_ACCOUNT -> portfolioController.getAccount(request);
                case SET_USER_ACCOUNT -> portfolioController.setAccount(request);
                case GET_STOCK_AVAILABLE_AMOUNT -> portfolioController.getStockAvailableAmount(request);

                case LOGIN -> userController.login(request);
                case DISCONNECT -> new Response(true, "Отключение выполнено успешно", null);
            };
        } catch (ResponseException e) {
            logger.warn("Ошибка при выполнении операции: {}", e.getMessage());
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при обработке запроса", e);
            return new Response(false, "Внутренняя ошибка сервера", null);
        }
    }

    /**
     * Закрывает клиентское соединение и обновляет счетчик подключений сервера.
     */
    private void closeConnection() {
        try {
            clientSocket.close();
            Server.decrementClientCount();
            logger.info("Клиентское соединение закрыто");
        } catch (IOException e) {
            logger.error("Ошибка при закрытии клиентского соединения", e);
        }
    }
}
