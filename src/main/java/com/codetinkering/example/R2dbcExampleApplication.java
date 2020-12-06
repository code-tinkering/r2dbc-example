package com.codetinkering.example;

import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class R2dbcExampleApplication {

    private static final Logger logger = LoggerFactory.getLogger(R2dbcExampleApplication.class);

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("dbsetup.sql")));
        return initializer;
    }

    @Bean
    public CommandLineRunner reactiveDatabaseExample(VehicleRepository vehicleRepository) {
        return args -> {

            List<Vehicle> vehicles = Arrays.asList(
                new Vehicle("Ford", "Mustang", "Red"),
                new Vehicle("Ford", "Bronco", "Orange"),
                new Vehicle("Chevy", "Silverado", "Blue"),
                new Vehicle("Chevy", "Tahoe", "Black"),
                new Vehicle("Toyota", "Supra", "Green")
            );
            // Create some vehicles and insert them into the database, blocking for up to 5 seconds
            vehicleRepository.saveAll(vehicles).blockLast(Duration.ofSeconds(5));

            // Find one or more vehicles from the repository using a query
            vehicleRepository.findByMake("Chevy").doOnNext(vehicle -> {
                logger.info(vehicle.toString());
            }).blockLast(Duration.ofSeconds(5));

            // Find a single vehicle by an ID, returns a Mono
            vehicleRepository.findById(1).doOnNext(vehicle -> {
                logger.info(vehicle.toString());
            }).block(Duration.ofSeconds(5));

            // Print all vehicles from the repository
            vehicleRepository.findAll().doOnNext(vehicle -> {
                logger.info(vehicle.toString());
            }).blockLast(Duration.ofSeconds(5));

            // Print all vehicles that match the provided make "Ford"
            // block the thread until the mono is completed or the request times out (5 seconds)
            Mono<List<Vehicle>> vehicleListMono = vehicleRepository.findByMake("Ford").collectList();
            List<Vehicle> vehicleList = vehicleListMono.block(Duration.ofSeconds(5));

            for (Vehicle v : vehicleList) {
                logger.info(v.toString());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(R2dbcExampleApplication.class, args);
    }
}
