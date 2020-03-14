package com.tlvlp.iot.server.reporting.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * Parses Docker Secrets before SpringBoot starts and makes them available as new environment variables.
 * The main use-case is to parse Docker Swarm Secrets.
 * NOTE:
 * This class must be registered under resources/META-INF/spring.factories as an {@link EnvironmentPostProcessor}
 * eg.: org.springframework.boot.env.EnvironmentPostProcessor = PATH.IN.PROJECT.SecretsLoader
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class SecretsLoader implements EnvironmentPostProcessor {

    private static Boolean loaded = false;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (loaded) {
            return;
        }
        System.out.printf("%n%nLoading file-based Docker secrets before the service starts.%n");

        Path dockerSecretsFolder = Path.of("/run/secrets/");

        try (Stream<Path> pathStream = Files.walk(dockerSecretsFolder, 2, FileVisitOption.FOLLOW_LINKS)) {
            Map<String, Object> secrets = pathStream
                    .filter(path -> path.toFile().isFile())
                    .collect(Collectors.toMap(path -> path.toFile().getName(), this::readFile));

            environment.getPropertySources()
                    .addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, new MapPropertySource("secrets", secrets));
            System.out.println("Secret strings are now available at the following environment variables:");
            secrets.keySet().forEach(key -> System.out.printf("    %s%n", key));
            loaded = true;
        } catch (Exception e) {
            System.err.println(String.format("Unable to parse Docker Secrets from folder: %s %n%s", dockerSecretsFolder, e));
            System.err.println("Exiting application.");
            System.exit(1);
        }
    }

    private String readFile(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read Docker Secret file: %s \n%s", filePath, e));
        }
    }

}