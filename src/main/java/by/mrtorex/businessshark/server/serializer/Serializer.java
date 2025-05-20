package by.mrtorex.businessshark.server.serializer;

import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс для сериализации объектов в JSON формат.
 */
public class Serializer {
    private static final Logger logger = LogManager.getLogger(Serializer.class);

    /**
     * Сериализует объект в строку формата JSON.
     *
     * @param obj объект для сериализации
     * @return строка в формате JSON
     * @throws IllegalArgumentException если не удалось сериализовать объект
     */
    public static String toJson(Object obj) {
        try {
            Gson gson = new Gson();
            return gson.toJson(obj);
        } catch (Exception e) {
            logger.error("Ошибка сериализации объекта в JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Не удалось сериализовать объект в JSON", e);
        }
    }
}
