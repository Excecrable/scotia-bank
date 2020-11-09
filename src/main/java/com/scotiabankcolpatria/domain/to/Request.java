package com.scotiabankcolpatria.domain.to;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Client;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Request implements Serializable {

    private Client client;
    private Account account;
    private LocalDate startDate;
    private LocalDate endDate;

}
