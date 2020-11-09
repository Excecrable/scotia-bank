package com.scotiabankcolpatria.domain.repository;

import com.scotiabankcolpatria.domain.entity.Transaction;
import com.scotiabankcolpatria.domain.to.AccountReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer>, Serializable {

    @Query(value = "SELECT (c.name) as clientName " +
            ", (a.number) as accountNumber" +
            ", (a.balance) as accountBalance" +
            ", (a.status) as accountStatus" +
            ", (t.type) as type" +
            ", COUNT(t.id) as cant" +
            ", SUM(t.amount) as amount " +
            "FROM client c " +
            " INNER JOIN account a ON a.client_id = c.id " +
            " INNER JOIN transaction t ON t.account_id = a.id " +
            "WHERE " +
            "c.name = :clientName AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.name, a.number, a.balance, t.type " +
            "ORDER BY c.name, a.number, a.balance, t.type", nativeQuery = true)
    List<AccountReport> findByClientAndDateBetween(String clientName, LocalDate startDate, LocalDate endDate);

}