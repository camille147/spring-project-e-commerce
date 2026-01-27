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
import java.util.ArrayList;
import java.util.List;
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
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private ProductPictureRepository productPictureRepository;
    @Autowired private ProductPromotionRepository productPromotionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) return;

        Faker faker = new Faker(new Locale("fr"));

        // 1. Liste de couleurs cohérentes pour ton menu de filtre
        List<String> colors = List.of("Noir", "Blanc", "Argent", "Bleu", "Rouge", "Vert");

        // 2. Création des Catégories
        Category smartphone = new Category(); smartphone.setName("Smartphones");
        Category laptop = new Category(); laptop.setName("Ordinateurs");
        categoryRepository.saveAll(List.of(smartphone, laptop));

        // 3. Création d'une Promotion
        Promotion flashSale = new Promotion();
        flashSale.setName("Vente Flash");
        flashSale.setDiscountRate(15.0);
        promotionRepository.save(flashSale);

        // 4. Pool d'images
        List<Picture> pool = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Picture pic = new Picture();
            pic.setName("Tech Item " + i);
            pic.setPictureUrl("https://picsum.photos/seed/tech" + i + "/800/600");
            pic.setIsActive(true);
            pool.add(pictureRepository.save(pic));
        }

        // 5. Génération des produits
        for (int i = 0; i < 30; i++) {
            Product p = new Product();
            p.setProductName(faker.commerce().productName() + " " + faker.random().hex(3));
            p.setBrand(faker.company().name());

            // On pioche une couleur dans notre liste fixe
            p.setColor(colors.get(faker.number().numberBetween(0, colors.size())));

            p.setDescription(faker.lorem().paragraph());
            p.setPrice(faker.number().randomDouble(2, 100, 1200));
            p.setQuantity(faker.number().numberBetween(1, 20));
            p.setReference("PROD-" + faker.random().hex(6).toUpperCase());
            p.setIsEnabled(true);
            p.setCategory(i % 2 == 0 ? smartphone : laptop);
            p.setDefaultPicture(pool.get(faker.number().numberBetween(0, 5)));

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

            // Galerie : On ajoute 2 photos par produit
            for (int j = 0; j < 2; j++) {
                ProductPicture ppic = new ProductPicture();
                ppic.setProduct(p);
                ppic.setPicture(pool.get(faker.number().numberBetween(5, 10)));
                productPictureRepository.save(ppic);
            }
                Address a = new Address();
                a.setStreet(faker.address().streetAddress());
                a.setCity(faker.address().city());
                a.setZipCode(faker.number().digits(5));
                a.setCountry("France");
                a.setUser(u);
                a = addressRepository.save(a);

            // Promos : 1 produit sur 3 est en promo
            if (i % 3 == 0) {
                ProductPromotion pp = new ProductPromotion();
                pp.setProduct(p);
                pp.setPromotion(flashSale);
                pp.setStartDate(LocalDateTime.now().minusDays(2));
                pp.setEndDate(LocalDateTime.now().plusDays(5));
                productPromotionRepository.save(pp);
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

        System.out.println(">> Données de test (Couleurs fixes, Promos, Galerie) prêtes !");
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

    }
    }
}