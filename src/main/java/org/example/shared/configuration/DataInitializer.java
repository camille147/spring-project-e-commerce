package org.example.shared.configuration;

import com.github.javafaker.Faker;
import org.example.shared.model.entity.Address;
import org.example.shared.model.entity.*;
import org.example.shared.model.enumeration.UserRole;
import org.example.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    public void run(String... args) throws Exception {

        if (productRepository.count() > 0 || userRepository.count() > 0) {
            System.out.println(">> Données déjà présentes, saut de l'initialisation.");
            return;
        }

        Faker faker = new Faker(new Locale("fr"));
        Random random = new Random();
        java.util.Set<String> usedEmails = new java.util.HashSet<>();

        // Bornes de dates pour les commandes (Passé -> Maintenant uniquement)
        java.util.Date startDate = java.sql.Timestamp.valueOf("2024-01-01 00:00:00");
        java.util.Date now = new java.util.Date(); // Pour brider à "tout de suite"

        // 1. Catégories
        Category smartphone = new Category(); smartphone.setName("Smartphones");
        Category laptop = new Category(); laptop.setName("Ordinateurs");
        Category audio = new Category(); audio.setName("Audio");
        categoryRepository.saveAll(List.of(smartphone, laptop, audio));

        // 2. Pool de Promotions (Plusieurs types)
        Promotion flash = new Promotion(); flash.setName("Vente Flash"); flash.setDiscountRate(15.0);
        Promotion summer = new Promotion(); summer.setName("Soldes d'été"); summer.setDiscountRate(30.0);
        Promotion blackFriday = new Promotion(); blackFriday.setName("Black Friday"); blackFriday.setDiscountRate(50.0);
        List<Promotion> promoPool = promotionRepository.saveAll(List.of(flash, summer, blackFriday));

        // 3. Pool d'images (On en crée beaucoup pour garantir l'unicité de l'image par défaut)
        List<Picture> pool = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Picture pic = new Picture();
            pic.setName("Tech Item " + i);
            pic.setPictureUrl("https://picsum.photos/seed/tech" + i + "/800/600");
            pic.setIsActive(true);
            pool.add(pictureRepository.save(pic));
        }

        // 4. Utilisateurs par défaut & Pool
        createDefaultUsers(usedEmails);
        List<User> userPool = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            // ... (logique création user identique à ton code précédent)
            User u = new User();
            u.setEmail(faker.internet().emailAddress());
            u.setPassword(passwordEncoder.encode("password"));
            u.setFirstName(faker.name().firstName());
            u.setLastName(faker.name().lastName());
            u.setRole(UserRole.USER);
            u.setIsActivated(true);
            u.setPrivacyConsent(true);
            u.setCreatedAt(LocalDateTime.now().minusMonths(6));
            u.setBirthDate(LocalDate.of(1995, 5, 10));
            userRepository.save(u);
            userPool.add(u);

            Address a = new Address();
            a.setStreet(faker.address().streetAddress()); a.setCity(faker.address().city());
            a.setZipCode(faker.address().zipCode()); a.setCountry("France");
            a.setUser(u); a.setIsActive(true);
            addressRepository.save(a);
        }

        // 6. Génération des produits
        List<String> colors = List.of("Noir", "Blanc", "Argent", "Bleu", "Rouge");
        for (int i = 0; i < 150; i++) {
            Product p = new Product();
            p.setProductName(faker.commerce().productName() + " " + faker.random().hex(4));
            p.setBrand(faker.company().name());
            p.setColor(colors.get(random.nextInt(colors.size())));
            p.setDescription(faker.lorem().fixedString(200));
            p.setPrice(faker.number().randomDouble(2, 20, 2000));
            p.setQuantity(faker.number().numberBetween(1, 100));
            p.setReference("TZ-" + faker.random().hex(8).toUpperCase());
            p.setIsEnabled(true);
            p.setCategories(List.of(smartphone, audio));

            // IMAGE PAR DÉFAUT UNIQUE (Une image du pool différente pour chaque i)
            p.setDefaultPicture(pool.get(i));

            productRepository.save(p);

            int imagesPerGallery = random.nextInt(3) + 2; // Entre 2 et 4 images de galerie par produit
            for (int g = 0; g < imagesPerGallery; g++) {
                ProductPicture ppic = new ProductPicture();
                ppic.setProduct(p);

                // On sélectionne une image différente de l'image par défaut
                // Utilisation d'un modulo pour ne pas dépasser la taille du pool (200)
                int imageIndex = (i + 50 + g) % 200;
                ppic.setPicture(pool.get(imageIndex));

                productPictureRepository.save(ppic);
            }

            // 7. PROMOTIONS MULTIPLES OU PAS
            // 60% de chance d'avoir une promo
            if (random.nextDouble() < 0.6) {
                int nbPromos = random.nextInt(2) + 1; // 1 ou 2 promos
                for (int k = 0; k < nbPromos; k++) {
                    ProductPromotion pp = new ProductPromotion();
                    pp.setProduct(p);
                    pp.setPromotion(promoPool.get(random.nextInt(promoPool.size())));
                    pp.setStartDate(LocalDateTime.now().minusDays(10));
                    pp.setEndDate(LocalDateTime.now().plusDays(10));
                    productPromotionRepository.save(pp);
                }
            }

            // 8. COMMANDES (Date bridée à NOW)
            if (i % 5 == 0) {
                User randomUser = userPool.get(random.nextInt(userPool.size()));
                Address userAddr = addressRepository.findByUserAndIsActiveTrue(randomUser).get(0);

                Order o = new Order();
                o.setOrderNumber(faker.number().digits(9));
                o.setTotal(p.getEffectivePrice());
                o.setStatus(1);
                o.setUser(randomUser);
                o.setAddress(userAddr);

                // Brider la date : entre 2024 et NOW (pas de futur)
                java.util.Date randomDate = faker.date().between(startDate, now);
                o.setCreatedAt(randomDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                orderRepository.save(o);

                OrderLine ol = new OrderLine();
                ol.setOrder(o); ol.setProduct(p);
                ol.setQuantity(1); ol.setPrice(p.getEffectivePrice());
                orderLineRepository.save(ol);
            }
        }
        System.out.println(">> Fake Data TechZone générée avec succès !");
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

            // RGPD
            admin.setPrivacyConsent(true);
            admin.setConsentDate(LocalDateTime.now());

            userRepository.save(admin);
            usedEmails.add(admin.getEmail());
        }

        if (userRepository.findByEmail("test@test.com").isEmpty()) {
            User user = new User();
            user.setRole(UserRole.USER);
            user.setEmail("test@test.com");
            user.setPassword(passwordEncoder.encode("password"));
            user.setFirstName("Camille");
            user.setLastName("Pinault");
            user.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActivated(true);

            // RGPD
            user.setPrivacyConsent(true);
            user.setConsentDate(LocalDateTime.now());

            userRepository.save(user);
            usedEmails.add(user.getEmail());
        }
    }
}