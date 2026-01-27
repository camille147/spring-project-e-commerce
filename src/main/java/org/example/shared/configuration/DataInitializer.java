package org.example.shared.configuration;

import com.github.javafaker.Faker;
import org.example.shared.model.entity.*;
import org.example.shared.model.enumeration.UserRole;
import org.example.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PictureRepository pictureRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AddressRepository addressRepository;
    @Autowired private OrderLineRepository orderLineRepository;
    @Autowired private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) return;

        Faker faker = new Faker(new Locale("fr"));

        try {
            Category tech = new Category();
            tech.setName("High-Tech");
            tech = categoryRepository.save(tech);

            Picture img = new Picture();
            img.setName("Macbook Pro");
            img.setPictureUrl("https://images.unsplash.com/photo-1517336712468-0776482cb48f");
            img.setIsActive(true);
            img = pictureRepository.save(img);

            for (int i = 0; i < 25; i++) {
                Product p = new Product();
                p.setProductName(faker.commerce().productName() + " " + i);
                p.setDescription(faker.lorem().sentence(5));
                p.setBrand(faker.company().name());
                p.setColor(faker.color().name());
                p.setReference("REF" + faker.number().digits(6));
                p.setPrice(faker.number().randomDouble(2, 100, 2000));
                p.setQuantity(faker.number().numberBetween(1, 100));
                p.setIsEnabled(true);
                p.setCategory(tech);
                p.setDefaultPicture(img);
                p = productRepository.save(p);

                User u = new User();
                u.setEmail(faker.internet().emailAddress() + i);
                u.setPassword(passwordEncoder.encode("password"));
                u.setFirstName(faker.name().firstName());
                u.setLastName(faker.name().lastName());
                u.setRole(UserRole.USER);
                u.setIsActivated(true);
                u.setCreatedAt(LocalDateTime.now());
                u.setBirthDate(faker.date().birthday(18, 60).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                u = userRepository.save(u);

                Address a = new Address();
                a.setStreet(faker.address().streetAddress());
                a.setCity(faker.address().city());
                a.setZipCode(faker.number().digits(5));
                a.setCountry("France");
                a.setUser(u);
                a = addressRepository.save(a);

                Order o = new Order();
                o.setOrderNumber(faker.number().digits(9));
                o.setTotal(p.getPrice());
                o.setStatus(0);
                o.setUser(u);
                o.setAddress(a);
                o.setCreatedAt(LocalDateTime.now());
                o = orderRepository.save(o);

                OrderLine ol = new OrderLine();
                ol.setOrder(o);
                ol.setProduct(p);
                ol.setQuantity(1);
                ol.setPrice(p.getPrice());
                orderLineRepository.save(ol);
            }

            User admin = new User();
            admin.setRole(UserRole.ADMIN);
            admin.setEmail("admin@admin.fr");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFirstName("Admin");
            admin.setLastName("Admin");
            admin.setBirthDate(faker.date().birthday(25, 50).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            admin.setCreatedAt(LocalDateTime.now());
            admin.setIsActivated(true);
            userRepository.save(admin);

            System.out.println(">> Fake Data générée avec succès !");

        } catch (Exception e) {
            System.err.println(">> ERREUR INITIALISATION : " + e.getMessage());
            if (e.getCause() != null) System.err.println("CAUSE : " + e.getCause().getMessage());
        }
    }
}