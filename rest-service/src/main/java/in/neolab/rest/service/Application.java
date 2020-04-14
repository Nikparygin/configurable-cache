package in.neolab.rest.service;

import in.neolab.configurable.cache.config.SystemHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

/**
 * Main application class
 * @author Nikita Parygin
 */
@SpringBootApplication
public class Application {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * Run application.
     * @param args Command line parameters.
     */
    public static void main(String[] args) {
        try {
            SystemHolder.init(args[0]);
            LOGGER.info("System holder successfully initialized with config: {}", args[0]);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize System Holder", e);
        }

        SpringApplication.run(Application.class, args);
        LOGGER.info("Application started");
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            LOGGER.info("Beans provided by Spring Boot:");
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                LOGGER.info(beanName);
            }
        };
    }
}
