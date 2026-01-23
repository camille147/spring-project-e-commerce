package org.example.shared.configuration;

import com.github.javafaker.Faker;
import org.example.shared.model.entity.Category;
import org.example.shared.model.entity.Picture;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.CategoryRepository;
import org.example.shared.repository.PictureRepository;
import org.example.shared.repository.ProductRepository;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PictureRepository pictureRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) return;

        Faker faker = new Faker();

        // 1. Création des Catégories
        Category tech = new Category();
        tech.setName("High-Tech");
        categoryRepository.save(tech);

        // 2. Création d'une Image
        Picture img = new Picture();
        img.setName("Macbook Pro");
        img.setPictureUrl("https://images.unsplash.com/photo-1517336712468-0776482cb48f");
        img.setIsActive(true);
        pictureRepository.save(img);

        // 3. Génération de 20 Produits aléatoires
        for (int i = 0; i < 20; i++) {
            Product p = new Product();
            p.setProductName(faker.commerce().productName());
            p.setBrand(faker.company().name());
            p.setColor(faker.color().name());
            p.setDescription(faker.lorem().sentence());
            p.setPrice((double) faker.number().randomDouble(2, 100, 2000));
            p.setQuantity(faker.number().numberBetween(1, 50));
            p.setReference("REF-" + faker.code().asin());
            p.setIsEnabled(true);

            // Liaisons
            p.setCategory(tech);
            p.setDefaultPicture(img);

            productRepository.save(p);
        }

        System.out.println(">> Fake Data générée avec succès !");
    }
}