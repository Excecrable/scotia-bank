package com.scotiabankcolpatria.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(value = "spring.application")
public class GlobalProperties implements Serializable {

    private static final long serialVersionUID = -3395163916611319213L;

    private String name;
    private String version;
    private String log;
    private int restPort;
    private String root;

}
