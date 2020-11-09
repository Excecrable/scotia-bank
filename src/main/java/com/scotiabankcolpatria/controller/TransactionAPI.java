package com.scotiabankcolpatria.controller;

import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Transaction;
import com.scotiabankcolpatria.domain.enums.TransactionType;
import com.scotiabankcolpatria.domain.to.Request;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "${spring.application.root}/transaction")
public final class TransactionAPI implements Serializable {

    private final TransactionService transactionService;

    @PutMapping(value = "/{accountNumber}/{amount}", name = "Registrar un crédito en cuenta")
    public Mono<ResponseEntity<Mono<Response>>> creditTransaction(
            @PathVariable @NotNull String accountNumber,
            @PathVariable @NotNull Double amount
    ) {
        return Mono.just(new ResponseEntity<>(
                transactionService.process(parse(TransactionType.CREDIT, accountNumber, amount)),
                HttpStatus.OK)
        );
    }

    @DeleteMapping(value = "/{accountNumber}/{amount}", name = "Registrar un débito en cuenta")
    public Mono<ResponseEntity<Mono<Response>>> debitTransaction(
            @PathVariable @NotNull String accountNumber,
            @PathVariable @NotNull Double amount
    ) {
        return Mono.just(new ResponseEntity<>(
                transactionService.process(parse(TransactionType.DEBIT, accountNumber, amount)),
                HttpStatus.OK)
        );
    }

    @PostMapping(value = "/report", name = "Reporte de cuentas y totales")
    public Mono<ResponseEntity<Mono<Response>>> report(@RequestBody @NotNull Mono<Request> request) {
        return Mono.just(new ResponseEntity<>(transactionService.generateReport(request), HttpStatus.OK));
    }

    private static Mono<Transaction> parse(@NotNull TransactionType type, @NotNull String accountNumber, @NotNull Double amount) {
        Account account = Account.builder()
                .number(accountNumber)
                .build();

        return Mono.just(Transaction.builder()
                .type(type)
                .account(account)
                .amount(amount)
                .date(LocalDateTime.now())
                .build());
    }

}

