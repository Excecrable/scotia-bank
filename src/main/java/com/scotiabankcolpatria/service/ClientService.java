package com.scotiabankcolpatria.service;

import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Client;
import com.scotiabankcolpatria.domain.enums.AccountStatus;
import com.scotiabankcolpatria.domain.enums.ClientStatus;
import com.scotiabankcolpatria.domain.repository.AccountRepository;
import com.scotiabankcolpatria.domain.repository.ClientRepository;
import com.scotiabankcolpatria.domain.to.Request;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.exceptions.EntityFoundException;
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

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.scotiabankcolpatria.helper.ValidatorHelper.Validations.EMPTY;
import static com.scotiabankcolpatria.helper.ValidatorHelper.Validations.NULL;

@Slf4j
@Service
@AllArgsConstructor
public final class ClientService implements Serializable {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    // Listar todos los clientes
    public Mono<Response> getClients() {
        StopWatch watch = new StopWatch();
        try {
            watch.start();
            log.info("----- Operación: Listar clientes ----");
            Iterable<Client> clients = clientRepository.findAll();
            return Mono.just(Response
                    .builder()
                    .code(ConstantsHelper.OK_CODE)
                    .message(ConstantsHelper.OK_MESSAGE)
                    .clients(clients)
                    .build());

        } catch (Exception e) {
            log.error(e.getMessage());
            return Mono.just(Response
                    .builder()
                    .code(ConstantsHelper.FAIL_CODE)
                    .message(e.getMessage())
                    .build());
        } finally {
            if (watch.isRunning()) {
                watch.stop();
                log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
            }
            log.info("----- Transacción finalizada ----");
        }
    }

    // Consultar un cliente
    public Mono<Response> getClient(Mono<String> clientName) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Consultar cliente ----");
        return clientName
                .flatMap(name -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(name, NULL, EMPTY)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }
                    name = name.toUpperCase();

                    // Buscar el cliente
                    Optional<Client> optionalClient = clientRepository.findByName(name);

                    // Validar la existencia del cliente
                    if (optionalClient.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede consultar el cliente especificado. Causa: No está registrado"));
                    }
                    Client client = optionalClient.get();

                    // Devolver el cliente
                    log.info("Cliente '{}', consultado de manera satisfactoria", client.getName());
                    if (watch.isRunning()) {
                        watch.stop();
                        log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
                    }

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .client(client)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // Crear un nuevo cliente
    public Mono<Response> create(Mono<Request> mRequest) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Registrar cliente ----");
        return mRequest
                .flatMap(request -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(request, NULL) || ValidatorHelper.validate(request.getClient(), NULL)) {
                        return Mono.error(new MandatoryFieldException("El campo 'client' es obligatorio"));
                    }
                    Client client = request.getClient();

                    // Validar datos obligatorios
                    if (INVALID_CLIENT_DATA.test(client)) {
                        return Mono.error(new MandatoryFieldException("No se proporcionó alguno de los datos obligatorios"));
                    }

                    // Validar el cliente en BD
                    client.setName(client.getName().toUpperCase());
                    Optional<Client> optionalClient = clientRepository.findByName(client.getName());
                    if (optionalClient.isPresent()) {
                        return Mono.error(new EntityFoundException("No se puede registrar el cliente especificado. Causa: Ya está registrado"));
                    }

                    // Eliminar el cliente
                    client.setStatus(ClientStatus.ENABLED);
                    client = clientRepository.save(client);
                    log.info("Cliente '{}', registrado de manera satisfactoria", client.getName());
                    if (watch.isRunning()) {
                        watch.stop();
                        log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
                    }

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .client(client)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // Actualizar la información de un cliente
    public Mono<Response> modify(final String name, Mono<Request> mRequest) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Modificar cliente ----");
        return mRequest
                .flatMap(request -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(name, NULL, EMPTY)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }

                    if (ValidatorHelper.validate(request, NULL) || ValidatorHelper.validate(request.getClient(), NULL, EMPTY)) {
                        return Mono.error(new MandatoryFieldException("El campo 'client' es obligatorio"));
                    }

                    // Obtener la entrada
                    Client client = request.getClient();

                    // Validar la existencia del cliente
                    Optional<Client> optionalClient = clientRepository.findByName(name.toUpperCase());
                    if (optionalClient.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede modificar el cliente especificado. Causa: No está registrado"));
                    }
                    Client clientBD = optionalClient.get();

                    // Validar el estado del cliente
                    if (!ClientStatus.ENABLED.equals(clientBD.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede modificar el cliente especificado. Causa: Estado inválido"));
                    }

                    // Actualizar los datos del cliente
                    clientBD.setAddress((client.getName() != null) ? client.getName().toUpperCase() : clientBD.getName());
                    clientBD.setAddress((client.getAddress() != null) ? client.getAddress() : clientBD.getAddress());
                    clientBD.setPhone((client.getPhone() != null) ? client.getPhone() : clientBD.getPhone());
                    clientBD = clientRepository.save(clientBD);
                    log.info("Cliente '{}', modificado de manera satisfactoria", client.getName());
                    if (watch.isRunning()) {
                        watch.stop();
                        log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
                    }

                    // Devolver la respuesta exitosa
                    return Mono.just(Response
                            .builder()
                            .code(ConstantsHelper.OK_CODE)
                            .message(ConstantsHelper.OK_MESSAGE)
                            .client(clientBD)
                            .build());

                })
                .onErrorResume(ERROR_HANDLER)
                .doFinally(ON_FINALLY);
    }

    // Eliminar un cliente
    public Mono<Response> remove(Mono<String> clientName) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Eliminar cliente ----");
        return clientName
                .flatMap(name -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(name, NULL)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }
                    name = name.toUpperCase();

                    // Buscar el cliente
                    Optional<Client> optionalClient = clientRepository.findByName(name);

                    // Validar la existencia del cliente
                    if (optionalClient.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar el cliente especificado. Causa: No está registrado"));
                    }
                    Client client = optionalClient.get();

                    // Validar el estado del cliente
                    if (!ClientStatus.ENABLED.equals(client.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar el cliente especificado. Causa: Estado inválido"));
                    }

                    // Validar que no tenga cuenta activas
                    List<Account> accounts = this.accountRepository.findByClientAndStatus(client, AccountStatus.ENABLED);
                    if (accounts != null && !accounts.isEmpty()) {
                        return Mono.error(new LogicalException("No se puede eliminar el cliente especificado. Causa: Tiene cuentas activas"));
                    }

                    // Eliminar el cliente
                    client.setStatus(ClientStatus.DISABLED);
                    clientRepository.save(client);
                    log.info("Cliente '{}', eliminado de manera satisfactoria", client.getName());
                    if (watch.isRunning()) {
                        watch.stop();
                        log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
                    }

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

    // Habilitar un cliente
    public Mono<Response> enable(Mono<String> clientName) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("----- Operación: Habilitar cliente ----");
        return clientName
                .flatMap(name -> {
                    // Validar entrada
                    if (ValidatorHelper.validate(name, NULL, EMPTY)) {
                        return Mono.error(new MandatoryFieldException(MessagesHelper.FIELD_NAME_MANDATORY));
                    }
                    name = name.toUpperCase();

                    // Buscar el cliente
                    Optional<Client> optionalClient = clientRepository.findByName(name);

                    // Validar la existencia del cliente
                    if (optionalClient.isEmpty()) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar el cliente especificado. Causa: No está registrado"));
                    }
                    Client client = optionalClient.get();

                    // Validar el estado del cliente
                    if (!ClientStatus.DISABLED.equals(client.getStatus())) {
                        return Mono.error(new EntityNotFoundException("No se puede eliminar el cliente especificado. Causa: Estado inválido"));
                    }

                    // Eliminar el cliente
                    client.setStatus(ClientStatus.ENABLED);
                    clientRepository.save(client);
                    log.info("Cliente '{}', habilitado de manera satisfactoria", client.getName());
                    if (watch.isRunning()) {
                        watch.stop();
                        log.info(MessagesHelper.EXECUTION_TIME, watch.getTotalTimeSeconds());
                    }

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

    private static final transient Predicate<Client> INVALID_CLIENT_DATA = client -> ValidatorHelper.validate(client, NULL) ||
            ValidatorHelper.validate(client.getName(), NULL, EMPTY) ||
            ValidatorHelper.validate(client.getAddress(), NULL, EMPTY);

}
