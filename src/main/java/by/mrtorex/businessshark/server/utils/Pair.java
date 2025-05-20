package by.mrtorex.businessshark.server.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс-пара для хранения двух связанных объектов.
 *
 * @param <K> тип ключа
 * @param <V> тип значения
 */
@Data
@AllArgsConstructor
public class Pair<K, V> {
    private K key;
    private V value;
}
