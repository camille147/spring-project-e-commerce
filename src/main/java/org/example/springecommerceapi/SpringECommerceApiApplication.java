package org.example.springecommerceapi;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "org.example")
@EntityScan("org.example.shared.model.entity") // Pour trouver les entités
@EnableJpaRepositories("org.example.shared.repository") // Pour trouver les futurs repositories
public class SpringECommerceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringECommerceApiApplication.class, args);
    }

}
