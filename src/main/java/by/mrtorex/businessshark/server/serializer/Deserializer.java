package by.mrtorex.businessshark.server.serializer;

import com.google.gson.reflect.TypeToken;
import by.mrtorex.businessshark.server.model.entities.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import by.mrtorex.businessshark.server.network.Request;
import by.mrtorex.businessshark.server.utils.Pair;

public class Deserializer {
    public Object extractData(Request request) {
        Gson gson = new Gson();

        try {
            return switch (request.getOperation()) {
                // User operations
                case LOGIN, CREATE_USER, REGISTER -> gson.fromJson(request.getData(), User.class);
                case READ_USER, DELETE_USER -> gson.fromJson(request.getData(), String.class);
                case UPDATE_USER -> gson.fromJson(request.getData(), new TypeToken<Pair<User, User>>() {}.getType());

                // Company operations
                case CREATE_COMPANY, UPDATE_COMPANY -> gson.fromJson(request.getData(), Company.class);
                case READ_COMPANY_DATA, DELETE_COMPANY -> gson.fromJson(request.getData(), String.class);

                // Stock operations
                case CREATE_STOCK, UPDATE_STOCK -> gson.fromJson(request.getData(), Stock.class);
                case READ_STOCK_DATA, DELETE_STOCK -> gson.fromJson(request.getData(), Integer.class);

                // Many-to-many relations
                case JOIN_STOCK_COMPANY, SEPARATE_STOCK_COMPANY ->
                        gson.fromJson(request.getData(), new TypeToken<Pair<Stock, Company>>() {}.getType());
                case GET_STOCK_BY_COMPANY -> gson.fromJson(request.getData(), Company.class);
                case GET_COMPANY_BY_STOCK -> gson.fromJson(request.getData(), Stock.class);

                // Default case
                default -> null;
            };
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON string", e);
        }
    }
}
