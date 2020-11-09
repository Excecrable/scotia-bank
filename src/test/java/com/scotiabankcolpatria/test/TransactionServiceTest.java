package com.scotiabankcolpatria.test;


import com.scotiabankcolpatria.ScotiabankTestApplication;
import com.scotiabankcolpatria.domain.entity.Account;
import com.scotiabankcolpatria.domain.entity.Transaction;
import com.scotiabankcolpatria.domain.enums.TransactionType;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest(classes = ScotiabankTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionServiceTest {

    private static final int RESPONSE_STATUS_OK = 0;
    private static final int RESPONSE_STATUS_FAIL = 99;

    @Autowired
    private TransactionService transactionService;


    // CREDIT

    @Test
    void creditOK() {
        log.info("--> Test: creditOK: START");

        // Transaction OK
        Transaction transaction = Transaction.builder()
                .type(TransactionType.CREDIT)
                .date(LocalDateTime.now())
                .account(Account.builder().number("1111-2222-3333-4444").build())
                .amount(1500.0)
                .build();

        // Ejecutar la transacción
        Response resp = transactionService.process(Mono.just(transaction)).block();

        // Verificar resultados
        assertNotNull(resp);
        assertEquals(RESPONSE_STATUS_OK, resp.getCode());

        log.info("--> Test: creditOK: END");
    }

    @Test
    void creditFAILMandatory() {
        log.info("--> Test: creditFAILMandatory: START");

        // Transaction OK
        Transaction transaction = Transaction.builder()
                .type(TransactionType.CREDIT)
                .amount(1500.0)
                .build();

        // Ejecutar la transacción
        Response resp = transactionService.process(Mono.just(transaction)).block();

        // Verificar resultados
        assertNotNull(resp);
        assertEquals(RESPONSE_STATUS_FAIL, resp.getCode());
        assertEquals("El campo 'account' es obligatorio", resp.getMessage());

        log.info("--> Test: creditFAILMandatory: END");
    }

    @Test
    void creditFAILNotExists() {
        log.info("--> Test: creditFAILNotExists: START");

        // Transaction OK
        Transaction transaction = Transaction.builder()
                .type(TransactionType.CREDIT)
                .date(LocalDateTime.now())
                .account(Account.builder().number("1111-2222-3333-5555").build())
                .amount(1500.0)
                .build();

        // Ejecutar la transacción
        Response resp = transactionService.process(Mono.just(transaction)).block();

        // Verificar resultados
        assertNotNull(resp);
        assertEquals(RESPONSE_STATUS_FAIL, resp.getCode());
        assertEquals("No se puede realizar la transacción. Causa: La cuenta no está registrada", resp.getMessage());

        log.info("--> Test: creditFAILNotExists: END");
    }

    @Test
    void creditFAILInvalidAmount() {
        log.info("--> Test: creditFAILInvalidAmount: START");

        // Transaction OK
        Transaction transaction = Transaction.builder()
                .type(TransactionType.CREDIT)
                .date(LocalDateTime.now())
                .account(Account.builder().number("1111-2222-3333-4444").build())
                .amount(-1500.0)
                .build();

        // Ejecutar la transacción
        Response resp = transactionService.process(Mono.just(transaction)).block();

        // Verificar resultados
        assertNotNull(resp);
        assertEquals(RESPONSE_STATUS_FAIL, resp.getCode());
        assertEquals("No se puede realizar la transacción. Causa: Los datos proporcionados no son correctos", resp.getMessage());

        log.info("--> Test: creditFAILInvalidAmount: END");
    }


}
