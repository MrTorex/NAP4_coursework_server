package by.mrtorex.businessshark.server.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Класс Serializer предоставляет методы для сериализации объектов
 * в JSON-строки с использованием библиотеки Gson.
 */
public class Serializer {

    /**
     * Преобразует объект в JSON-строку.
     *
     * @param obj объект для сериализации
     * @return JSON-строка, представляющая объект
     * @throws IllegalArgumentException если объект не удалось сериализовать
     */
    public static String toJson(Object obj) {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось сериализовать объект в JSON", e);
        }
    }
}
