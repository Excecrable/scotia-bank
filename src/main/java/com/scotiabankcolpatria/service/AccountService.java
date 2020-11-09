package com.scotiabankcolpatria.service;

import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Client;
import com.scotiabankcolpatria.domain.enums.AccountStatus;
import com.scotiabankcolpatria.domain.repository.AccountRepository;
import com.scotiabankcolpatria.domain.repository.ClientRepository;
import com.scotiabankcolpatria.domain.to.Request;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.exceptions.EntityFoundException;
import com.scotiabankcolpatria.exceptions.EntityNotFoundException;
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

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.scotiabankcolpatria.helper.ValidatorHelper.Validations.*;

@Slf4j
@Service
@AllArgsConstructor
public final class AccountService implements Serializable {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    // Listar todos los cuentas
    public Mono<Response> getAccounts() {
        StopWatch watch = new StopWatch();
        try {
            watch.start();
            log.info("----- Operación: Listar cuentas ----");
            Iterable<Account> accounts = accountRepository.findAll();
            return Mono.just(Response
                    .builder()
                    .code(ConstantsHelper.OK_CODE)
                    .message(ConstantsHelper.OK_MESSAGE)
                    .accounts(accounts)
                    .build());

        } catch (Exception e) {
            log.error(e.getMessage());
            return Mono.just(Response
                    .builder()
                    .code(ConstantsHelper.FAIL_CODE)
                    .message(e.getMessage())
                    .build());
        } finally {
            watch.stop();
            log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
            log.info("----- Transacción finalizada ----");
        }
    }

    // Consultar una cuenta
    public Mono<Response> getAccount(Mono<String> accountNumber) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Consultar cuenta ----");
        return accountNumber
                .flatMap(number -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(number, NULL, EMPTY, ACCOUNT_PATTERN)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }
                    number = number.toUpperCase();

                    // Buscar la cuenta
                    Optional<Account> optionalAccount = accountRepository.findByNumber(number);

                    // Validar la existencia de la cuenta
                    if (optionalAccount.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede consultar la cuenta especificada. Causa: No está registrado"));
                    }
                    Account account = optionalAccount.get();

                    // Devolver la cuenta
                    log.info("Cuenta '{}', consultada de manera satisfactoria", account.getNumber());
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

    // Crear un nuevo cuenta
    public Mono<Response> create(Mono<Request> mRequest) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Registrar cuenta ----");
        return mRequest
                .flatMap(request -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(request, NULL) || ValidatorHelper.validate(request.getAccount(), NULL)) {
                        return Mono.error(new MandatoryFieldException("El campo 'account' es obligatorio"));
                    }
                    Account account = request.getAccount();

                    // Validar datos obligatorios
                    if (INVALID_ACCOUNT_DATA.test(account)) {
                        return Mono.error(new MandatoryFieldException("No se proporcionó alguno de los datos obligatorios"));
                    }

                    // Validar la existencia del cliente
                    Optional<Client> optionalClient = findClient(account.getClient());
                    if (optionalClient.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede registrar la cuenta especificada. Causa: El cliente no está registrado"));
                    }
                    Client client = optionalClient.get();

                    // Validar la cuenta en BD
                    Optional<Account> optionalAccount = accountRepository.findByNumber(account.getNumber());
                    if (optionalAccount.isPresent()) {
                        return Mono.error(new EntityFoundException("No se puede registrar la cuenta especificada. Causa: Ya está registrada"));
                    }

                    // Registrar la cuenta
                    account.setClient(client);
                    account.setStatus(AccountStatus.ENABLED);
                    account = accountRepository.save(account);
                    log.info("Cuenta '{}', registrada de manera satisfactoria", account.getNumber());
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

    // Actualizar la información de una cuenta
    public Mono<Response> modify(final String number, Mono<Request> mRequest) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Modificar cuenta ----");
        return mRequest
                .flatMap(request -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(number, NULL, EMPTY, ACCOUNT_PATTERN)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }

                    if (ValidatorHelper.validate(request, NULL) || ValidatorHelper.validate(request.getAccount(), NULL)) {
                        return Mono.error(new MandatoryFieldException("El campo 'account' es obligatorio"));
                    }

                    // Obtener la entrada
                    Account account = request.getAccount();

                    // Validar la existencia del cliente
                    if (account.getClient() != null) {
                        Optional<Client> optionalClient = findClient(account.getClient());
                        if (optionalClient.isEmpty()) {
                            return Mono.error(new EntityNotFoundException("No se puede modificar la cuenta especificada. Causa: El cliente no está registrado"));
                        }
                        account.setClient(optionalClient.get());
                    }

                    // Validar monto
                    if ((account.getBalance() != null) && (ValidatorHelper.validate(account.getBalance(), NEGATIVE))) {
                        return Mono.error(new EntityNotFoundException("No se puede modificar la cuenta especificada. Causa: El saldo es inválido"));
                    }

                    // Validar la existencia de la cuenta
                    Optional<Account> optionalAccount = accountRepository.findByNumber(number);
                    if (optionalAccount.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede modificar la cuenta especificada. Causa: No está registrado"));
                    }
                    Account accountBD = optionalAccount.get();

                    // Validar el estado de la cuenta
                    if (!AccountStatus.ENABLED.equals(accountBD.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede modificar la cuenta especificada. Causa: Estado inválido"));
                    }

                    // Actualizar los datos de la cuenta
                    accountBD.setClient((account.getClient() != null) ? account.getClient() : accountBD.getClient());
                    accountBD.setBalance((account.getBalance() != null) ? account.getBalance() : accountBD.getBalance());
                    accountBD = accountRepository.save(accountBD);
                    log.info("Cuenta '{}', modificada de manera satisfactoria", account.getNumber());
                    watch.stop();
                    log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .account(accountBD)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // Eliminar una cuenta
    public Mono<Response> remove(Mono<String> accountNumber) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Eliminar cuenta ----");
        return accountNumber
                .flatMap(number -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(number, NULL, ACCOUNT_PATTERN)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }
                    number = number.toUpperCase();

                    // Buscar la cuenta
                    Optional<Account> optionalAccount = accountRepository.findByNumber(number);

                    // Validar la existencia de la cuenta
                    if (optionalAccount.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar la cuenta especificada. Causa: No está registrado"));
                    }
                    Account account = optionalAccount.get();

                    // Validar el estado de la cuenta
                    if (!AccountStatus.ENABLED.equals(account.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar la cuenta especificada. Causa: Estado inválido"));
                    }

                    // Eliminar la cuenta
                    account.setStatus(AccountStatus.DISABLED);
                    accountRepository.save(account);
                    log.info("Cuenta '{}', eliminada de manera satisfactoria", account.getNumber());
                    watch.stop();
                    log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // Habilitar una cuenta
    public Mono<Response> enable(Mono<String> accountNumber) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Habilitar cuenta ----");
        return accountNumber
                .flatMap(number -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(number, NULL, EMPTY, ACCOUNT_PATTERN)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }
                    number = number.toUpperCase();

                    // Buscar la cuenta
                    Optional<Account> optionalAccount = accountRepository.findByNumber(number);

                    // Validar la existencia de la cuenta
                    if (optionalAccount.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar la cuenta especificada. Causa: No está registrado"));
                    }
                    Account account = optionalAccount.get();

                    // Validar el estado de la cuenta
                    if (!AccountStatus.DISABLED.equals(account.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar la cuenta especificada. Causa: Estado inválido"));
                    }

                    // Habilitar la cuenta
                    account.setStatus(AccountStatus.ENABLED);
                    accountRepository.save(account);
                    log.info("Cuenta '{}', habilitada de manera satisfactoria", account.getNumber());
                    watch.stop();
                    log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
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
            ValidatorHelper.validate(account.getClient(), NULL) ||
            ValidatorHelper.validate(account.getNumber(), NULL, EMPTY, ACCOUNT_PATTERN) ||
            ValidatorHelper.validate(account.getBalance(), NULL, NEGATIVE);

    private Optional<Client> findClient(Client client) {
        if (!ValidatorHelper.validate(client.getName(), NULL, EMPTY)) {
            return clientRepository.findByName(client.getName().toUpperCase());
        } else if (!ValidatorHelper.validate(client.getName(), NULL, NEGATIVE)) {
            return clientRepository.findById(client.getId());
        }

        return Optional.empty();
    }

}
