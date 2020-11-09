package com.scotiabankcolpatria.controller;

import com.scotiabankcolpatria.domain.to.Request;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.helper.ConstantsHelper;
import com.scotiabankcolpatria.service.ClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "${spring.application.root}/client")
public final class ClientAPI implements Serializable {

    private final ClientService clientService;

    @GetMapping(value = {"", "/"}, name = "Listar clientes")
    public Mono<ResponseEntity<Mono<Response>>> getClients() {
        return Mono.just(
                new ResponseEntity<>(
                        clientService.getClients(),
                        HttpStatus.OK)
        );
    }

    @GetMapping(value = ConstantsHelper.PARAM_CLIENT_NAME, name = "Consultar cliente")
    public Mono<ResponseEntity<Mono<Response>>> getClient(@PathVariable @NotNull String clientName) {
        return Mono.just(
                new ResponseEntity<>(
                        clientService.getClient(Mono.just(clientName)),
                        HttpStatus.OK)
        );
    }

    @PutMapping(value = {"/", ""}, name = "Registrar nuevo cliente")
    public Mono<ResponseEntity<Mono<Response>>> createClient(@RequestBody @NotNull Mono<Request> request) {
        return Mono.just(new ResponseEntity<>(clientService.create(request), HttpStatus.OK));
    }

    @PostMapping(value = ConstantsHelper.PARAM_CLIENT_NAME, name = "Modificar cliente")
    public Mono<ResponseEntity<Mono<Response>>> updateClient(
            @PathVariable @NotNull String clientName,
            @RequestBody @NotNull Mono<Request> request) {
        return Mono.just(
                new ResponseEntity<>(
                        clientService.modify(clientName, request),
                        HttpStatus.OK)
        );
    }

    @DeleteMapping(value = ConstantsHelper.PARAM_CLIENT_NAME, name = "Eliminar cliente")
    public Mono<ResponseEntity<Mono<Response>>> removeClient(@PathVariable @NotNull String clientName) {
        return Mono.just(
                new ResponseEntity<>(
                        clientService.remove(Mono.just(clientName)),
                        HttpStatus.OK)
        );
    }

    @PatchMapping(value = ConstantsHelper.PARAM_CLIENT_NAME, name = "Activar cliente")
    public Mono<ResponseEntity<Mono<Response>>> enableClient(@PathVariable @NotNull String clientName) {
        return Mono.just(
                new ResponseEntity<>(
                        clientService.enable(Mono.just(clientName)),
                        HttpStatus.OK)
        );
    }

}

