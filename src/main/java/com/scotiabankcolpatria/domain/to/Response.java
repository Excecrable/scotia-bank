package com.scotiabankcolpatria.domain.to;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Client;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Response implements Serializable {

    // Estado general de la transaccion
    private int code;
    private String message;

    // Respuestas para el módulo: CLIENTES
    private Client client;
    private Iterable<Client> clients;

    // Respuestas para el módulo: CUENTAS
    private Account account;
    private Iterable<Account> accounts;

    // Respuestas para el módulo: REPORTE
    private Iterable<AccountReport> accountReports;

}
