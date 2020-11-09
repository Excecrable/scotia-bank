package com.scotiabankcolpatria.domain.to;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.scotiabankcolpatria.domain.enums.AccountStatus;
import com.scotiabankcolpatria.domain.enums.TransactionType;

@JsonPropertyOrder(value = {
        "clientName",
        "accountNumber",
        "accountBalance",
        "accountStatus",
        "type",
        "cant",
        "amount",
})
public interface AccountReport {

    String getClientName();

    String getAccountNumber();

    Double getAccountBalance();

    AccountStatus getAccountStatus();

    TransactionType getType();

    Integer getCant();

    Double getAmount();

}
