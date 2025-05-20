package by.mrtorex.businessshark.server.network;

import by.mrtorex.businessshark.server.config.SessionConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * Основной класс сервера, отвечающий за приём подключений клиентов, мониторинг активности и подключение к БД.
 */
public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static volatile int clientCount = 0;
    private static volatile long lastClientConnectedTime = System.currentTimeMillis();

    private static ServerSocket serverSocket;
    private static ExecutorService clientExecutor;
    private static ScheduledExecutorService monitorExecutor;
    private static volatile boolean running = true;

    /**
     * Точка входа в приложение.
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Server::shutdown, "Shutdown-Hook"));
        startServer();
    }

    /**
     * Запускает сервер, инициализирует мониторинг и подключение к БД.
     */
    private static void startServer() {
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        int serverPort;

        try {
            serverPort = Integer.parseInt(bundle.getString("SERVER_PORT"));
        } catch (NumberFormatException e) {
            logger.error("Неверный формат порта сервера в конфигурационном файле", e);
            throw new IllegalStateException("Невозможно запустить сервер");
        }

        clientExecutor = Executors.newCachedThreadPool();
        monitorExecutor = Executors.newSingleThreadScheduledExecutor();

        try {
            serverSocket = new ServerSocket(serverPort);
            logger.info("Сервер запущен на порте {}", serverPort);

            startMonitoring();
            connectToDatabase();

            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    incrementClientCount();
                    lastClientConnectedTime = System.currentTimeMillis();
                    logger.info("Клиент подключился. Текущее количество клиентов: {}", clientCount);
                    clientExecutor.submit(new ClientThread(client));
                } catch (IOException e) {
                    if (running) {
                        logger.error("Ошибка при приёме клиента", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка запуска сервера", e);
        } finally {
            shutdown();
        }
    }

    /**
     * Запускает мониторинг подключений и завершает сервер при длительном отсутствии клиентов.
     */
    private static void startMonitoring() {
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        long monitoringInterval;
        long shutdownTime;

        try {
            monitoringInterval = Long.parseLong(bundle.getString("MONITORING_INTERVAL"));
            shutdownTime = Long.parseLong(bundle.getString("SHUTDOWN_TIME"));
        } catch (NumberFormatException e) {
            logger.error("Неверный формат интервалов мониторинга", e);
            throw new IllegalStateException("Невозможно запустить мониторинг");
        }

        monitorExecutor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (clientCount == 0 && (now - lastClientConnectedTime) >= shutdownTime) {
                logger.info("Нет подключенных клиентов в течение {} мс. Завершение сервера.", shutdownTime);
                shutdown();
            } else {
                logger.info("Текущее количество клиентов: {}", clientCount);
            }
        }, 0, monitoringInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Потокобезопасное уменьшение счётчика клиентов.
     */
    public static synchronized void decrementClientCount() {
        if (clientCount > 0) {
            clientCount--;
            logger.info("Клиент отключился. Оставшиеся клиенты: {}", clientCount);
        } else {
            logger.warn("Попытка уменьшить счётчик клиентов ниже нуля");
        }
    }

    /**
     * Потокобезопасное увеличение счётчика клиентов.
     */
    private static synchronized void incrementClientCount() {
        clientCount++;
    }

    /**
     * Асинхронное подключение к базе данных.
     */
    private static void connectToDatabase() {
        new Thread(() -> {
            logger.info("Попытка подключения к базе данных...");
            try {
                SessionConfig config = SessionConfig.getInstance();
                logger.info("Соединение с БД установлено: {}", config);
            } catch (Exception e) {
                logger.error("Ошибка при подключении к БД: {}", e.getMessage());
            }
        }, "DB-Connection-Thread").start();
    }

    /**
     * Корректное завершение всех ресурсов: сокета, потоков и планировщиков.
     */
    private static synchronized void shutdown() {
        if (!running) return;

        running = false;
        logger.info("Начинается завершение работы сервера...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("Серверный сокет закрыт");
            }
        } catch (IOException e) {
            logger.warn("Ошибка при закрытии серверного сокета", e);
        }

        if (monitorExecutor != null && !monitorExecutor.isShutdown()) {
            monitorExecutor.shutdownNow();
            logger.info("Мониторинг остановлен");
        }

        if (clientExecutor != null && !clientExecutor.isShutdown()) {
            clientExecutor.shutdownNow();
            logger.info("Потоки клиентов завершены");
        }

        logger.info("Сервер завершил работу.");
    }
}
