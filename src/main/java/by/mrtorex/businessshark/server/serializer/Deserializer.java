package by.mrtorex.businessshark.server.serializer;

import by.mrtorex.businessshark.server.model.entities.*;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.utils.Pair;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Класс Deserializer отвечает за десериализацию данных,
 * получаемых из объекта запроса Request.
 * Использует библиотеку Gson для преобразования JSON-строки
 * в объекты соответствующих классов.
 */
public class Deserializer {

    /**
     * Извлекает данные из запроса и преобразует JSON-строку
     * в объект нужного типа в зависимости от типа операции.
     *
     * @param request объект запроса, содержащий операцию и данные в формате JSON
     * @return объект соответствующего типа, десериализованный из JSON,
     *         или null, если операция неизвестна
     * @throws IllegalArgumentException если строка JSON некорректна
     */
    @SuppressWarnings("DuplicateBranchesInSwitch")
    public Object extractData(Request request) {
        Gson gson = new Gson();

        try {
            return switch (request.getOperation()) {
                case LOGIN, CREATE_USER, REGISTER -> gson.fromJson(request.getData(), User.class);
                case READ_USER, DELETE_USER -> gson.fromJson(request.getData(), String.class);
                case UPDATE_USER -> gson.fromJson(request.getData(), new TypeToken<Pair<User, User>>() {}.getType());

                case CREATE_COMPANY, UPDATE_COMPANY -> gson.fromJson(request.getData(), Company.class);
                case READ_COMPANY_DATA, DELETE_COMPANY -> gson.fromJson(request.getData(), String.class);

                case CREATE_STOCK, UPDATE_STOCK -> gson.fromJson(request.getData(), Stock.class);
                case READ_STOCK_DATA, DELETE_STOCK -> gson.fromJson(request.getData(), Integer.class);

                case JOIN_STOCK_COMPANY, SEPARATE_STOCK_COMPANY ->
                        gson.fromJson(request.getData(), new TypeToken<Pair<Stock, Company>>() {}.getType());
                case GET_STOCKS_BY_COMPANY -> gson.fromJson(request.getData(), Company.class);
                case GET_COMPANY_BY_STOCK -> gson.fromJson(request.getData(), Stock.class);

                case ADD_USER_STOCK, UPDATE_USER_STOCK ->
                        gson.fromJson(request.getData(), new TypeToken<Pair<Pair<Stock,Integer>, Integer>>() {}.getType());
                case GET_USER_STOCK, DELETE_USER_STOCK ->
                        gson.fromJson(request.getData(), new TypeToken<Pair<Integer, Integer>>() {}.getType());
                case GET_ALL_USER_STOCKS ->
                        gson.fromJson(request.getData(), Integer.class);
                case GET_USER_ACCOUNT -> gson.fromJson(request.getData(), Integer.class);
                case GET_STOCK_AVAILABLE_AMOUNT -> gson.fromJson(request.getData(), Integer.class);
                case SET_USER_ACCOUNT -> gson.fromJson(request.getData(), new TypeToken<Pair<Integer,Double>>() {}.getType());

                default -> null;
            };
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Некорректный JSON-формат", e);
        }
    }
}
