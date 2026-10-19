package ai.oenexa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point of the OENEXA modular monolith. Lives in the root package so every module is scanned. */
@SpringBootApplication
public class OenexaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OenexaApplication.class, args);
    }
}
