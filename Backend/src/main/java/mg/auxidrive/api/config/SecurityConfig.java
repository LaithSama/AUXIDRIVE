package mg.auxidrive.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                // /error doit rester public, sinon les erreurs (400, 409...) des routes publiques deviennent des 401
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                // le client soumet sa demande d'inscription sans être connecté
                .requestMatchers(HttpMethod.POST, "/api/inscriptions").permitAll()
                // catalogue d'offres : lecture publique, modification réservée à l'administrateur
                .requestMatchers(HttpMethod.GET, "/api/offres/**").permitAll()
                .requestMatchers("/api/offres/**").hasRole("ADMIN")
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")
                .requestMatchers("/api/inscriptions/**", "/api/assures/**", "/api/documents/**", "/api/comptes-clients/**")
                    .hasAnyRole("AGENT", "ADMIN")
                // profil du client connecté (consultation, modification, suppression programmée)
                .requestMatchers("/api/profil/**").hasRole("CLIENT")
                // véhicules : l'agent et l'administrateur ont tous les droits, le client seulement les siens.
                // Les routes staff sont déclarées avant les routes client pour que le premier motif qui
                // correspond gagne ; sans cela un client pourrait lister les véhicules d'un autre assuré.
                .requestMatchers(HttpMethod.GET, "/api/vehicules").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/vehicules/assure/**").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/vehicules/**").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/vehicules/**").hasAnyRole("CLIENT", "AGENT", "ADMIN")
                // contrats : le client souscrit et suit les siens, l'agent valide ou refuse.
                // La validation et le refus engagent la compagnie, ils sont donc réservés au staff.
                // L'ordre compte : les routes staff sont déclarées avant les routes client.
                .requestMatchers("/api/contrats/assure/**", "/api/contrats/*/valider", "/api/contrats/*/refuser")
                    .hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/contrats/**").hasAnyRole("CLIENT", "AGENT", "ADMIN")
                // factures : mêmes règles que les contrats, l'agent y accède globalement
                .requestMatchers("/api/factures/contrat/**").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/factures/**").hasAnyRole("CLIENT", "AGENT", "ADMIN")
                // sinistres : l'assuré déclare et suit, l'agent instruit et clôt
                .requestMatchers("/api/sinistres/contrat/**", "/api/sinistres/*/rapport",
                                 "/api/sinistres/*/estimation", "/api/sinistres/*/indemniser",
                                 "/api/sinistres/*/refuser").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/sinistres/**").hasAnyRole("CLIENT", "AGENT", "ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
