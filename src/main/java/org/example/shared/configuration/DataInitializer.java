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
    @Transactional // Important pour les liaisons complexes
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) return;

        Faker faker = new Faker();

        // 1. Création d'une Catégorie
        Category tech = new Category();
        tech.setName("Électronique");
        categoryRepository.save(tech);

        // 2. Création d'une Promotion (-30%)
        Promotion winterSale = new Promotion();
        winterSale.setName("Soldes d'Hiver");
        winterSale.setDiscountRate(30.0);
        promotionRepository.save(winterSale);

        // 3. Création d'un pool d'images génériques pour la galerie
        List<Picture> poolPictures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Picture pic = new Picture();
            pic.setName("Gallery Pic " + i);
            pic.setPictureUrl("https://picsum.photos/seed/" + i + "/800/600");
            pic.setIsActive(true);
            poolPictures.add(pictureRepository.save(pic));
        }

        // 4. Génération de Produits
        for (int i = 0; i < 20; i++) {
            Product p = new Product();
            // Ajout d'un suffixe aléatoire pour garantir l'unicité du nom (contrainte UK)
            p.setProductName(faker.commerce().productName() + " " + faker.random().hex(4));
            p.setBrand(faker.company().name());
            p.setColor(faker.color().name());
            p.setDescription(faker.lorem().paragraph());
            p.setPrice(faker.number().randomDouble(2, 50, 1500));
            p.setQuantity(faker.number().numberBetween(0, 100));
            p.setReference("REF-" + faker.random().hex(8).toUpperCase());
            p.setIsEnabled(true);
            p.setCategory(tech);
            p.setDefaultPicture(poolPictures.get(0)); // Image principale

            productRepository.save(p);

            // A. Ajouter des images à la galerie (Table ProductPicture)
            for (int j = 1; j < 3; j++) {
                ProductPicture ppic = new ProductPicture();
                ppic.setProduct(p);
                ppic.setPicture(poolPictures.get(faker.number().numberBetween(0, 5)));
                productPictureRepository.save(ppic);
            }

            // B. Appliquer la promo à 1 produit sur 2 (Table ProductPromotion)
            if (i % 2 == 0) {
                ProductPromotion pp = new ProductPromotion();
                pp.setProduct(p);
                pp.setPromotion(winterSale);
                pp.setStartDate(LocalDateTime.now().minusDays(1)); // Déjà commencée
                pp.setEndDate(LocalDateTime.now().plusMonths(1));   // Finit dans un mois
                productPromotionRepository.save(pp);
            }
        }

        System.out.println(">> Fake Data (Produits, Galeries, Promos) générée !");
    }
}