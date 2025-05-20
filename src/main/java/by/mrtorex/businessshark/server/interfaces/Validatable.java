package by.mrtorex.businessshark.server.interfaces;

/**
 * Интерфейс для проверки корректности объектов.
 *
 * @param <T> тип проверяемого объекта
 */
public interface Validatable<T> {

    /**
     * Проверяет, является ли объект допустимым.
     *
     * @param t объект для проверки
     * @return true, если объект допустим; false в противном случае
     */
    boolean isValid(T t);
}
