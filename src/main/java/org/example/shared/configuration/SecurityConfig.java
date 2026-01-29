package org.example.shared.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.example.shared.model.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    //API REST (Stateless avec JWT)
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**") //config QUE pour URLs /api/...
                .csrf(csrf -> csrf.disable()) // Désactivé pour les API REST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // seules les routes d'authentification sont publiques
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // sécurise les routes admin
                        .anyRequest().authenticated() //tout le reste requiert un Token
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //pas de cookies
                );

        org.springframework.context.ApplicationContext ctx = http.getSharedObject(org.springframework.context.ApplicationContext.class);
        if (ctx != null) {
            try {
                if (ctx.containsBeanDefinition("org.example.springecommerceapi.security.AuthEntryPointJwt") ||
                        ctx.getBeanNamesForType(org.example.springecommerceapi.security.AuthEntryPointJwt.class).length > 0) {
                    org.example.springecommerceapi.security.AuthEntryPointJwt entryPoint = ctx.getBean(org.example.springecommerceapi.security.AuthEntryPointJwt.class);
                    http.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));
                }
            } catch (Exception ignored) {
            }

            try {
                if (ctx.containsBeanDefinition("org.example.springecommerceapi.security.AuthTokenFilter") ||
                        ctx.getBeanNamesForType(org.example.springecommerceapi.security.AuthTokenFilter.class).length > 0) {
                    org.example.springecommerceapi.security.AuthTokenFilter jwtFilter = ctx.getBean(org.example.springecommerceapi.security.AuthTokenFilter.class);
                    http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
                }
            } catch (Exception ignored) {

            }
        }

        return http.build();
    }

    //VUE THYMELEAF (Stateful avec Cookies/Sessions)
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/static/**","/css/**", "/files/**", "/js/**", "/login", "/images/**", "/fonts/**", "/register", "/user/navbar", "/error/**", "/logout", "/rgpd/**",
                                "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(roleBasedSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                );

        return http.build();
    }

    // Handler pour rediriger selon le rôle
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            var roles = authentication.getAuthorities();
            for (var role : roles) {
                if (role.getAuthority().equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard");
                    return;
                }
            }
            response.sendRedirect("user/shop");
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(this.customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Permet d'injecter l'AuthenticationManager dans AuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    //définit comment les mots de passe sont hashés
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}