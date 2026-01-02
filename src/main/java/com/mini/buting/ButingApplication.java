package com.mini.buting;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
public class ButingApplication {

    @Value("${spring.jpa.properties.hibernate.jdbc.time_zone}")
    private String timeZone;

    public static void main(String[] args) {
        SpringApplication.run(ButingApplication.class, args);
    }

    @PostConstruct
    public void setJvmTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

}
