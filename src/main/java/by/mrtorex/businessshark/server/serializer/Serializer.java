package by.mrtorex.businessshark.server.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {
    public static String toJson(Object obj) {
        try {
            //Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Gson gson = new GsonBuilder().create();
            return gson.toJson(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize object to JSON", e);
        }
    }
}

