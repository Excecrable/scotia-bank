package com.scotiabankcolpatria.test;


import com.scotiabankcolpatria.ScotiabankTestApplication;
import com.scotiabankcolpatria.domain.to.Response;
import com.scotiabankcolpatria.helper.MessagesHelper;
import com.scotiabankcolpatria.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = ScotiabankTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientServiceTest {

    private static final int RESPONSE_STATUS_OK = 0;
    private static final int RESPONSE_STATUS_FAIL = 99;

    @Autowired
    private ClientService clientService;

    // LISTADO

    @Test
    void listAll() {
        log.info("--> Test: listAll: START");
        Response resp = clientService.getClients().block();

        assertNotNull(resp);
        assertNotNull(resp.getClients());
        assertEquals(RESPONSE_STATUS_OK, resp.getCode());
        assertTrue(resp.getClients().iterator().hasNext());

        log.info("--> Test: listAll: END");
    }


    // CONSULTA

    @Test
    void getClientOK() {
        log.info("--> Test: getClientOK: START");
        Response resp = clientService.getClient(Mono.just("Ronel")).block();

        assertNotNull(resp);
        assertNotNull(resp.getClient());
        assertEquals(RESPONSE_STATUS_OK, resp.getCode());
        assertEquals(1, resp.getClient().getId());

        log.info("--> Test: getClientOK: END");
    }

    @Test
    void getClientFAILMandatory() {
        log.info("--> Test: getClientFAILMandatory: START");
        Response resp = clientService.getClient(Mono.just("")).block();

        assertNotNull(resp);
        assertEquals(RESPONSE_STATUS_FAIL, resp.getCode());
        assertEquals(MessagesHelper.FIELD_NAME_MANDATORY, resp.getMessage());

        log.info("--> Test: getClientFAILMandatory: END");
    }

    @Test
    void getClientFAILNotExists() {
        log.info("--> Test: getClientFAILNotExists: START");
        Response resp = clientService.getClient(Mono.just("Luis")).block();

        assertNotNull(resp);
        assertEquals(RESPONSE_STATUS_FAIL, resp.getCode());
        assertEquals("No se puede consultar el cliente especificado. Causa: No está registrado", resp.getMessage());

        log.info("--> Test: getClientFAILNotExists: END");
    }
}
