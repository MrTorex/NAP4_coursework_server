package by.mrtorex.businessshark.server.network;

import by.mrtorex.businessshark.server.enums.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Request implements Serializable {
    @NonNull
    private Operation operation;

    private String data;
}
