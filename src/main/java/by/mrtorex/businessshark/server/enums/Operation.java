package by.mrtorex.businessshark.server.enums;

import lombok.Getter;

/**
 * Перечисление операций, поддерживаемых сервером.
 * Группирует операции по функциональным блокам.
 */
@Getter
public enum Operation {
    /* Операции с акциями */
    CREATE_STOCK("Создание акции"),
    READ_STOCK_DATA("Получение данных акции"),
    UPDATE_STOCK("Обновление акции"),
    DELETE_STOCK("Удаление акции"),

    /* Операции с компаниями */
    CREATE_COMPANY("Создание компании"),
    READ_COMPANY_DATA("Получение данных компании"),
    UPDATE_COMPANY("Обновление компании"),
    DELETE_COMPANY("Удаление компании"),

    /* Операции с пользователями */
    CREATE_USER("Создание пользователя"),
    READ_USER("Получение данных пользователя"),
    UPDATE_USER("Обновление пользователя"),
    DELETE_USER("Удаление пользователя"),

    /* Операции получения списков */
    GET_ALL_COMPANIES("Получение списка компаний"),
    GET_ALL_STOCKS("Получение списка акций"),
    GET_ALL_USERS("Получение списка пользователей"),
    GET_ALL_ROLES("Получение списка ролей"),
    GET_ALL_STOCKS_WITH_NO_COMPANY("Получение свободных акций"),

    /* Операции с отношениями компаний и акций */
    GET_STOCKS_BY_COMPANY("Получение акций компании"),
    GET_COMPANY_BY_STOCK("Получение компании по акции"),
    JOIN_STOCK_COMPANY("Привязка акции к компании"),
    SEPARATE_STOCK_COMPANY("Отвязка акции от компании"),

    /* Операции с портфелями пользователей */
    ADD_USER_STOCK("Добавление акции в портфель"),
    GET_USER_STOCK("Получение акции из портфеля"),
    UPDATE_USER_STOCK("Обновление акции в портфеле"),
    DELETE_USER_STOCK("Удаление акции из портфеля"),
    GET_ALL_USER_STOCKS("Получение всего портфеля"),
    GET_ALL_USER_STOCK_IDS("Получение ID связей пользователь-акция"),
    GET_USER_ACCOUNT("Получение баланса пользователя"),
    SET_USER_ACCOUNT("Установка баланса пользователя"),
    GET_STOCK_AVAILABLE_AMOUNT("Получение доступного количества акций"),

    /* Системные операции */
    LOGIN("Аутентификация пользователя"),
    REGISTER("Регистрация пользователя"),
    DISCONNECT("Отключение от сервера");

    private final String description;

    /**
     * Конструктор перечисления.
     * @param description Описание операции на русском языке
     */
    Operation(String description) {
        this.description = description;
    }

    /**
     * Проверяет, является ли операция операцией чтения.
     * @return true если операция только читает данные
     */
    @SuppressWarnings("unused")
    public boolean isReadOperation() {
        return this.name().startsWith("GET_") ||
                this.name().startsWith("READ_");
    }

    /**
     * Проверяет, является ли операция операцией изменения.
     * @return true если операция изменяет данные
     */
    @SuppressWarnings("unused")
    public boolean isModifyOperation() {
        return this.name().startsWith("CREATE_") ||
                this.name().startsWith("UPDATE_") ||
                this.name().startsWith("DELETE_") ||
                this.name().startsWith("ADD_") ||
                this.name().startsWith("JOIN_") ||
                this.name().startsWith("SEPARATE_") ||
                this.name().startsWith("SET_");
    }
}