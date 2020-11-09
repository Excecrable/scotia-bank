package com.scotiabankcolpatria.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import javax.validation.constraints.NotNull;

@Slf4j
@Controller
public final class ChangelogController implements WebFluxConfigurer {

    @Value("${spring.application.name}")
    private String name;

    public ChangelogController() {
        super();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/public", "classpath:/static/", "classpath:/");
    }

    @GetMapping(value = "/")
    public String get(final Model model) {
        return index(model);
    }

    @PostMapping
    public String post(final Model model) {
        return index(model);
    }

    @PutMapping
    public String put(final Model model) {
        return index(model);
    }

    @DeleteMapping
    public String delete(final Model model) {
        return index(model);
    }

    @PatchMapping
    public String patch(final Model model) {
        return index(model);
    }

    private String index(@NotNull Model model) {
        // Establecer la variables para el template ThymeLeaf
        model.addAttribute("name", name);
        return "index";
    }

}
