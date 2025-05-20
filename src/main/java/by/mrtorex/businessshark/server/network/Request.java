package by.mrtorex.businessshark.server.network;

import by.mrtorex.businessshark.server.enums.Operation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Запрос, передаваемый от клиента к серверу.
 * Содержит операцию и связанные с ней данные.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Request implements Serializable {

    /**
     * Операция, которую необходимо выполнить.
     */
    @NonNull
    private Operation operation;

    /**
     * Данные, необходимые для выполнения операции.
     */
    private String data;
}
