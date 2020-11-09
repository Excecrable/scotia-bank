package com.scotiabankcolpatria.domain.repository;

import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Client;
import com.scotiabankcolpatria.domain.enums.AccountStatus;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Integer>, Serializable {

    Optional<Account> findByNumber(String number);

    List<Account> findByClientAndStatus(Client client, AccountStatus status);

}
