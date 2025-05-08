package by.mrtorex.businessshark.server.network;

import by.mrtorex.businessshark.server.config.SessionConfig;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;

public class Server {
    private static int clientCount = 0;
    private static long lastClientConnectedTime = System.currentTimeMillis();

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        int serverPort = Integer.parseInt(bundle.getString("SERVER_PORT"));

        System.out.println("Сервер запускается...");

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Сервер запущен на порте " + serverPort + "!");

            startMonitoring();
            connectToDatabase();

            while (true) {
                Socket clientAccepted = serverSocket.accept();

                clientCount++;
                lastClientConnectedTime = System.currentTimeMillis(); // Обновляем время последнего подключения
                System.out.println("Соединение установлено... Текущее количество клиентов: " + clientCount);

                new Thread(new ClientThread(clientAccepted)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startMonitoring() {
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        long monitoringInterval = Long.parseLong(bundle.getString("MONITORING_INTERVAL"));
        long shutdownTime = Long.parseLong(bundle.getString("SHUTDOWN_TIME"));

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(monitoringInterval); // Логирование количества клиентов каждый интервал

                    long currentTime = System.currentTimeMillis();
                    if (clientCount == 0 && (currentTime - lastClientConnectedTime) >= shutdownTime) {
                        System.out.println("Нет подключенных клиентов. Сервер завершает работу.");
                        System.exit(0); // Завершение работы сервера
                    }

                    System.out.println("Текущее количество клиентов: " + clientCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public static synchronized void decrementClientCount() {
        clientCount--;
    }

    private static void connectToDatabase() {
        new Thread(() -> {
            System.out.println("Попытка подключения к БД...");
            try {
                System.out.println("Соединение с БД установлено: " + SessionConfig.getInstance());
            } catch (Exception e) {
                System.err.println("Соединение с БД установить не вышло: " + e.getMessage());
            }
        }).start();
    }
}
