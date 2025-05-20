package by.mrtorex.businessshark.server.network;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Ответ сервера на запрос клиента.
 * Содержит информацию об успешности операции, сообщение и данные.
 */
@Data
@AllArgsConstructor
public class Response implements Serializable {

    /**
     * Статус успешности выполнения операции.
     * true - операция выполнена успешно, false - произошла ошибка.
     */
    private boolean success;

    /**
     * Сообщение с описанием результата операции.
     */
    private String message;

    /**
     * Дополнительные данные, возвращаемые в ответе.
     */
    private String data;
}
