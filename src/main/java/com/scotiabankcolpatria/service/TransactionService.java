package com.scotiabankcolpatria.service;

import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Transaction;
import com.scotiabankcolpatria.domain.enums.AccountStatus;
import com.scotiabankcolpatria.domain.enums.TransactionType;
import com.scotiabankcolpatria.domain.repository.AccountRepository;
import com.scotiabankcolpatria.domain.repository.TransactionRepository;
import com.scotiabankcolpatria.domain.to.AccountReport;
import com.scotiabankcolpatria.domain.to.Request;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.exceptions.EntityNotFoundException;
import com.scotiabankcolpatria.exceptions.LogicalException;
import com.scotiabankcolpatria.exceptions.MandatoryFieldException;
import com.scotiabankcolpatria.helper.ConstantsHelper;
import com.scotiabankcolpatria.helper.MessagesHelper;
import com.scotiabankcolpatria.helper.ValidatorHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.scotiabankcolpatria.helper.ValidatorHelper.Validations.*;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public final class TransactionService implements Serializable {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Ejecutar una transacción
    public Mono<Response> process(Mono<Transaction> mTransaction) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Realizar transacción ----");
        return mTransaction
                .flatMap(transaction -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(transaction, NULL) || ValidatorHelper.validate(transaction.getAccount(), NULL)) {
                        return Mono.error(new MandatoryFieldException("El campo 'account' es obligatorio"));
                    }
                    Account account = transaction.getAccount();

                    // Validar la existencia y validez de la cuenta
                    Optional<Account> optionalAccount = accountRepository.findByNumber(account.getNumber());
                    if (optionalAccount.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede realizar la transacción. Causa: La cuenta no está registrada"));
                    }
                    account = optionalAccount.get();
                    if (!AccountStatus.ENABLED.equals(account.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede realizar la transacción. Causa: La cuenta no está activa"));
                    }

                    // Realizar la operación
                    Double value = account.getBalance();
                    if (TransactionType.DEBIT.equals(transaction.getType())) {
                        value -= transaction.getAmount();
                    } else {
                        value += transaction.getAmount();
                    }
                    account.setBalance(value);

                    // Validar el resultado
                    if (INVALID_ACCOUNT_DATA.test(account)) {
                        return Mono.error(new MandatoryFieldException("No se puede realizar la transacción. Causa: Monto inválido"));
                    }
                    transaction.setAccount(account);

                    // Actualizar la cuenta y guardar la transacción
                    account = accountRepository.save(account);
                    transactionRepository.save(transaction);
                    log.info("Transacción de '{}', registrada de manera satisfactoria sobre la cuenta: {}; por un monto de: {}",
                            transaction.getType(),
                            account.getNumber(),
                            transaction.getAmount()
                    );

                    watch.stop();
                    log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .account(account)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // Reporte de transacciones por cuenta
    public Mono<Response> generateReport(Mono<Request> mRequest) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Reporte de totales ----");
        return mRequest
                .flatMap(request -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(request, NULL)
                            || ValidatorHelper.validate(request.getClient(), NULL)
                            || ValidatorHelper.validate(request.getStartDate(), NULL)
                            || ValidatorHelper.validate(request.getEndDate(), NULL)) {
                        return Mono.error(new MandatoryFieldException("No se proporcionaron todos los parámetros obligatorios"));
                    }

                    // Buscar la Data
                    List<AccountReport> transactions = transactionRepository.findByClientAndDateBetween(request.getClient().getName().toUpperCase(), request.getStartDate(), request.getEndDate());
                    if (transactions == null || transactions.isEmpty()) {
                        return Mono.error(new LogicalException("No se encontraron resultados para los parámetros proporcionados"));
                    }

                    // Devolver el reporte
                    log.info("Se encontraron '{}'transacciones", transactions.size());
                    watch.stop();
                    log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .accountReports(transactions)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // FUNCIONES UTILITARIAS

    private static final transient Function<? super Throwable, ? extends Mono<? extends Response>> ERROR_HANDLER = err -> {
        Exception e = (Exception) err;
        log.error(e.getMessage());
        return Mono.just(Response
                .builder()
                .code(ConstantsHelper.FAIL_CODE)
                .message(e.getMessage())
                .build());
    };

    private static final transient Consumer<SignalType> ON_FINALLY = signal -> log.info("----- Transacción finalizada ----");

    private static final transient Predicate<Account> INVALID_ACCOUNT_DATA = account -> ValidatorHelper.validate(account, NULL) ||
            ValidatorHelper.validate(account.getNumber(), NULL, EMPTY, ACCOUNT_PATTERN) ||
            ValidatorHelper.validate(account.getBalance(), NULL, NEGATIVE);

}
