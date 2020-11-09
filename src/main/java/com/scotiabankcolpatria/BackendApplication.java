package com.scotiabankcolpatria;

import com.scotiabankcolpatria.properties.GlobalProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@EnableJpaRepositories
@EnableConfigurationProperties(value = {
        GlobalProperties.class
})
@AllArgsConstructor
@SpringBootApplication
public class BackendApplication implements ApplicationListener<ContextRefreshedEvent> {

    private static final String LOG_LINE = "----------------------------------------------------";

    private final GlobalProperties globalProperties;

    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(BackendApplication.class);
        application.run();
    }

    @Override
    public final void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            // Evidenciar en el LOG el inicio correcto de los servicios
            log.info("");
            log.info(LOG_LINE);
            log.info("{} application started", globalProperties.getName());
            log.info("Port: {}", globalProperties.getRestPort());
            log.info("Version: {}", globalProperties.getVersion());
            log.info("Context-Path: {}", globalProperties.getRoot());
            log.info("Launched [OK]");
            log.info(LOG_LINE);
            log.info("");

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
