package com.scotiabankcolpatria.controller;

import com.scotiabankcolpatria.domain.to.Request;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.service.AccountService;
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
@RequestMapping(value = "${spring.application.root}/account")
public class AccountAPI implements Serializable {

    private final AccountService accountService;

    @GetMapping(value = {"", "/"}, name = "Listar cuentas")
    public Mono<ResponseEntity<Mono<Response>>> getAccounts() {
        return Mono.just(
                new ResponseEntity<>(
                        accountService.getAccounts(),
                        HttpStatus.OK)
        );
    }

    @GetMapping(value = "/{accountNumber}", name = "Consultar cuenta")
    public Mono<ResponseEntity<Mono<Response>>> getAccount(@PathVariable @NotNull String accountNumber) {
        return Mono.just(
                new ResponseEntity<>(
                        accountService.getAccount(Mono.just(accountNumber)),
                        HttpStatus.OK)
        );
    }

    @PutMapping(value = {"/", ""}, name = "Registrar nueva cuenta")
    public Mono<ResponseEntity<Mono<Response>>> createAccount(@RequestBody @NotNull Mono<Request> request) {
        return Mono.just(new ResponseEntity<>(accountService.create(request), HttpStatus.OK));
    }

    @PostMapping(value = "/{accountNumber}", name = "Modificar cuenta")
    public Mono<ResponseEntity<Mono<Response>>> updateAccount(
            @PathVariable @NotNull String accountNumber,
            @RequestBody @NotNull Mono<Request> request) {
        return Mono.just(
                new ResponseEntity<>(
                        accountService.modify(accountNumber, request),
                        HttpStatus.OK)
        );
    }

    @DeleteMapping(value = "/{accountNumber}", name = "Eliminar cuenta")
    public Mono<ResponseEntity<Mono<Response>>> removeAccount(@PathVariable @NotNull String accountNumber) {
        return Mono.just(
                new ResponseEntity<>(
                        accountService.remove(Mono.just(accountNumber)),
                        HttpStatus.OK)
        );
    }

    @PatchMapping(value = "/{accountNumber}", name = "Activar cuenta")
    public Mono<ResponseEntity<Mono<Response>>> enableAccount(@PathVariable @NotNull String accountNumber) {
        return Mono.just(
                new ResponseEntity<>(
                        accountService.enable(Mono.just(accountNumber)),
                        HttpStatus.OK)
        );
    }

}

