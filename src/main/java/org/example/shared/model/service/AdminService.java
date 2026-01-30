package org.example.shared.model.service;

import org.example.shared.model.DTO.*;
import org.example.shared.model.entity.*;
import org.example.shared.model.enumeration.OrderStatus;
import org.example.shared.model.enumeration.UserRole;
import org.example.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderLineRepository orderLineRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private ProductPromotionRepository productPromotionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private ProductPictureRepository productPictureRepository;



}