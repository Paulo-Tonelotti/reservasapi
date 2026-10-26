package com.paulotonelotti.reservasapi.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.paulotonelotti.reservasapi.security.TratadorErrosSeguranca;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, TratadorErrosSeguranca tratadorErros) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // Usa o bean corsConfigurationSource abaixo (libera o frontend a chamar a API pelo navegador)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/esqueci-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.GET, "/espacos/**").authenticated()
                        .requestMatchers("/espacos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/reservas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/reservas/*/confirmar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clientes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/clientes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/clientes/*").hasRole("ADMIN")
                        // GET e PUT de /clientes/{id}: qualquer usuário logado passa daqui,
                        // e o ClienteController só deixa o ADMIN ou o próprio cliente seguir
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        // Usa o converter para transformar o claim "roles" em permissões ROLE_*
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                        // Token ausente, inválido ou expirado → 401 no formato do ErroResponse
                        .authenticationEntryPoint(tratadorErros)
                        .accessDeniedHandler(tratadorErros))
                // Mesmos tratadores para os erros que acontecem fora da validação do token
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(tratadorErros)
                        .accessDeniedHandler(tratadorErros))
                .build(); // faz a ‘build’ do objeto http security
    }

    // Diz ao navegador quais origens (o frontend) podem chamar a API, com quais métodos e cabeçalhos
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origens}") List<String> origens) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Sem isso, o JavaScript do frontend não consegue ler o cabeçalho Location das respostas 201
        config.setExposedHeaders(List.of("Location"));
        // O navegador guarda a resposta da verificação (preflight) por 1 hora
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Cria o encoder, o carimbo que assina os 'tokens' que são gerados
    @Bean
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(chaveJwt()).build();
    }

    // Cria o decoder, o conferidor do carimbo que foi assinado no token
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(chaveJwt()).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Pega o secret e transforma numa chave de fato
    private SecretKey chaveJwt() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
