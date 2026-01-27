package org.example.shared.configuration;

import com.github.javafaker.Faker;
import org.example.shared.model.entity.*;
import org.example.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
class DataInitializer implements CommandLineRunner {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PictureRepository pictureRepository;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private ProductPromotionRepository productPromotionRepository;
    @Autowired private ProductPictureRepository productPictureRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) return;

        Faker faker = new Faker();

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

            productRepository.save(p);

            // Galerie : On ajoute 2 photos par produit
            for (int j = 0; j < 2; j++) {
                ProductPicture ppic = new ProductPicture();
                ppic.setProduct(p);
                ppic.setPicture(pool.get(faker.number().numberBetween(5, 10)));
                productPictureRepository.save(ppic);
            }

            // Promos : 1 produit sur 3 est en promo
            if (i % 3 == 0) {
                ProductPromotion pp = new ProductPromotion();
                pp.setProduct(p);
                pp.setPromotion(flashSale);
                pp.setStartDate(LocalDateTime.now().minusDays(2));
                pp.setEndDate(LocalDateTime.now().plusDays(5));
                productPromotionRepository.save(pp);
            }
        }

        System.out.println(">> Données de test (Couleurs fixes, Promos, Galerie) prêtes !");
    }
}