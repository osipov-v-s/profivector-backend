package com.profession.suggest.configuration.properties;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:enviroment.properties", ignoreResourceNotFound = true)
public class EnvPropertiesConfig {
    public static final String SECRET_KEY = "app.secret.key";
}
