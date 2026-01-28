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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private ProductPictureRepository productPictureRepository;
    @Autowired private ProductPromotionRepository productPromotionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Sécurité pour ne pas doubler les données au redémarrage
        if (productRepository.count() > 0 || userRepository.count() > 0) return;

        Faker faker = new Faker(new Locale("fr"));
        java.util.Set<String> usedEmails = new java.util.HashSet<>();
        java.util.Random random = new java.util.Random();

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

        // 4. Création de l'Admin (Unique)
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        admin.setEmail("admin@admin.fr");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFirstName("Admin");
        admin.setLastName("TechZone");
        admin.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setIsActivated(true);
        userRepository.save(admin);
        usedEmails.add(admin.getEmail());

        User user = new User();
        user.setRole(UserRole.USER);
        user.setEmail("test@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setFirstName("Camille");
        user.setLastName("Pinault");
        user.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActivated(true);
        userRepository.save(user);
        usedEmails.add(user.getEmail());

        // 5. Création d'un pool d'utilisateurs (Clients)
        List<User> userPool = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            String email;
            do {
                email = faker.internet().emailAddress();
            } while (usedEmails.contains(email)); // Garantie d'email unique

            User u = new User();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode("password"));
            u.setFirstName(faker.name().firstName());
            u.setLastName(faker.name().lastName());
            u.setRole(UserRole.USER);
            u.setIsActivated(true);
            u.setCreatedAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)));
            u.setBirthDate(faker.date().birthday(18, 60).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            userPool.add(userRepository.save(u));
            usedEmails.add(email);

            // Adresse pour chaque utilisateur
            Address a = new Address();
            a.setStreet(faker.address().streetAddress());
            a.setCity(faker.address().city());
            a.setZipCode(faker.address().zipCode());
            a.setCountry("France");
            a.setUser(u);
            addressRepository.save(a);
        }

        // 6. Génération des produits
        List<String> colors = List.of("Noir", "Blanc", "Argent", "Bleu", "Rouge", "Vert");
        for (int i = 0; i < 30; i++) {
            Product p = new Product();
            // Ajout d'un suffixe unique pour éviter les doublons de noms de produits si nécessaire
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

            // 7. Simulation de quelques commandes pour les produits en promo
            if (i % 3 == 0) {
                // Promotion
                ProductPromotion pp = new ProductPromotion();
                pp.setProduct(p);
                pp.setPromotion(flashSale);
                pp.setStartDate(LocalDateTime.now().minusDays(1));
                pp.setEndDate(LocalDateTime.now().plusDays(7));
                productPromotionRepository.save(pp);

                // Commande factice liée à un utilisateur aléatoire du pool
                User randomUser = userPool.get(random.nextInt(userPool.size()));
                // On récupère une adresse de l'utilisateur (on sait qu'il en a une)
                Address userAddr = addressRepository.findByUserAndIsActiveTrue(randomUser).get(0);

                Order o = new Order();
                o.setOrderNumber(faker.number().digits(9));
                o.setTotal(p.getPrice());
                o.setStatus(1); // 1 = Validée par exemple
                o.setUser(randomUser);
                o.setAddress(userAddr);
                o.setCreatedAt(LocalDateTime.now().minusHours(faker.number().numberBetween(1, 24)));
                orderRepository.save(o);

                OrderLine ol = new OrderLine();
                ol.setOrder(o);
                ol.setProduct(p);
                ol.setQuantity(1);
                ol.setPrice(p.getPrice());
                orderLineRepository.save(ol);
            }
        }

        System.out.println(">> Fake Data générée avec succès (Admin, Users, Products, Orders) !");
    }
}