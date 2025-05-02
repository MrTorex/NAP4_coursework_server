package by.mrtorex.businessshark.server.network;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Response implements Serializable {
    private boolean success;
    private String message;
    private String data;
}
