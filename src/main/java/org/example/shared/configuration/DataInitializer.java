package org.example.shared.configuration;

import com.github.javafaker.Faker;
import org.example.shared.model.entity.*;
import org.example.shared.model.enumeration.UserRole;
import org.example.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private ProductPictureRepository productPictureRepository;
    @Autowired private ProductPromotionRepository productPromotionRepository;

    @Override
    // On retire @Transactional ici pour éviter de bloquer la table sur 1500+ insertions
    public void run(String... args) throws Exception {

        if (productRepository.count() > 0 || userRepository.count() > 0) {
            System.out.println(">> Données déjà présentes, saut de l'initialisation.");
            return;
        }

        Faker faker = new Faker(new Locale("fr"));
        Random random = new Random();
        java.util.Set<String> usedEmails = new java.util.HashSet<>();

        // Bornes de dates pour 2025 - 2026
        java.util.Date startDate = java.sql.Timestamp.valueOf("2025-01-01 00:00:00");
        java.util.Date endDate = java.sql.Timestamp.valueOf("2026-12-31 23:59:59");

        // 1. Catégories
        Category smartphone = new Category(); smartphone.setName("Smartphones");
        Category laptop = new Category(); laptop.setName("Ordinateurs");
        categoryRepository.saveAll(List.of(smartphone, laptop));

        // 2. Promotion
        Promotion flashSale = new Promotion();
        flashSale.setName("Vente Flash");
        flashSale.setDiscountRate(15.0);
        promotionRepository.save(flashSale);

        // 3. Pool d'images
        List<Picture> pool = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Picture pic = new Picture();
            pic.setName("Tech Item " + i);
            pic.setPictureUrl("https://picsum.photos/seed/tech" + i + "/800/600");
            pic.setIsActive(true);
            pool.add(pictureRepository.save(pic));
        }

        // 4. Admin & Test User
        createDefaultUsers(usedEmails);

        // 5. Pool d'utilisateurs
        List<User> userPool = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            String email;
            do { email = faker.internet().emailAddress(); } while (usedEmails.contains(email));

            User u = new User();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode("password"));
            u.setFirstName(faker.name().firstName());
            u.setLastName(faker.name().lastName());
            u.setRole(UserRole.USER);
            u.setIsActivated(true);
            u.setCreatedAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)));
            u.setBirthDate(faker.date().birthday(18, 60).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            userRepository.save(u);
            userPool.add(u);
            usedEmails.add(email);

            Address a = new Address();
            a.setStreet(faker.address().streetAddress());
            a.setCity(faker.address().city());
            a.setZipCode(faker.address().zipCode());
            a.setCountry("France");
            a.setUser(u);
            a.setIsActive(true);
            addressRepository.save(a);
        }

        // 6. Génération des produits et Commandes
        List<String> colors = List.of("Noir", "Blanc", "Argent", "Bleu", "Rouge", "Vert");
        for (int i = 0; i < 150; i++) {
            Product p = new Product();
            p.setProductName(faker.commerce().productName() + " " + faker.random().hex(4));
            p.setBrand(faker.company().name());
            p.setColor(colors.get(random.nextInt(colors.size())));
            p.setDescription(faker.lorem().fixedString(250));
            p.setPrice(faker.number().randomDouble(2, 50, 1500));
            p.setQuantity(faker.number().numberBetween(5, 50));
            p.setReference("TZ-" + faker.random().hex(8).toUpperCase());
            p.setIsEnabled(true);
            p.setCategory(random.nextBoolean() ? smartphone : laptop);
            p.setDefaultPicture(pool.get(random.nextInt(5)));
            productRepository.save(p);

            // Images secondaires
            for (int j = 0; j < 2; j++) {
                ProductPicture ppic = new ProductPicture();
                ppic.setProduct(p);
                ppic.setPicture(pool.get(faker.number().numberBetween(5, 10)));
                productPictureRepository.save(ppic);
            }

            // 7. Commandes massives (tous les 3 produits)
            if (i % 3 == 0) {
                ProductPromotion pp = new ProductPromotion();
                pp.setProduct(p);
                pp.setPromotion(flashSale);
                pp.setStartDate(LocalDateTime.now().minusDays(1));
                pp.setEndDate(LocalDateTime.now().plusDays(7));
                productPromotionRepository.save(pp);

                User randomUser = userPool.get(random.nextInt(userPool.size()));
                Address userAddr = addressRepository.findByUserAndIsActiveTrue(randomUser).get(0);

                // Optimisation : On prépare des listes pour insérer en lot (batch)
                List<Order> ordersToSave = new ArrayList<>();
                List<OrderLine> linesToSave = new ArrayList<>();

                for (int j = 0; j < 15; j++) {
                    Order o = new Order();
                    o.setOrderNumber(faker.number().digits(9));
                    o.setTotal(p.getPrice());
                    o.setStatus(1);
                    o.setUser(randomUser);
                    o.setAddress(userAddr);

                    // Date aléatoire entre 2025 et 2026
                    java.util.Date randomDate = faker.date().between(startDate, endDate);
                    o.setCreatedAt(randomDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

                    ordersToSave.add(o);
                }

                // Sauvegarde des commandes par lot
                orderRepository.saveAll(ordersToSave);

                // Création des lignes de commande liées
                for (Order o : ordersToSave) {
                    OrderLine ol = new OrderLine();
                    ol.setOrder(o);
                    ol.setProduct(p);
                    ol.setQuantity(faker.number().numberBetween(1, 5));
                    ol.setPrice(p.getPrice());
                    linesToSave.add(ol);
                }
                orderLineRepository.saveAll(linesToSave);
                System.out.println("Batch inséré pour produit : " + p.getProductName());
            }
        }

        System.out.println(">> Fake Data générée avec succès !");
    }

    private void createDefaultUsers(java.util.Set<String> usedEmails) {
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User admin = new User();
            admin.setRole(UserRole.ADMIN);
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("adminadmin"));
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setIsActivated(true);
            userRepository.save(admin);
            usedEmails.add(admin.getEmail());
        }
    }
}