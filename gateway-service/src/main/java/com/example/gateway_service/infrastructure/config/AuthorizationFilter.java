package com.example.gateway_service.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.gateway_service.domain.user.vo.RoleType;

import reactor.core.publisher.Mono;

@Component
public class AuthorizationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public static final Map<String, RoleType> routeRole = Map.of(
        "/chat/messages", RoleType.USER,
        "/chat/user", RoleType.USER,
        "/chat/admin", RoleType.ADMIN,
        "/users/profile", RoleType.USER
    );

    private boolean isSwaggerPath(String path) {
        return path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/webjars")
            || path.startsWith("/docs")
            || path.startsWith("/swagger-ui.html")
            ;
    }

    private boolean isAuthorized(String path, RoleType role) {
        for (Map.Entry<String, RoleType> entry: routeRole.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return role.covers(entry.getValue());
            }
        }

        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Verifica se a rota é segura (aquelas definidas em routeRole)
        boolean isSecured = routeRole.entrySet().stream().anyMatch(entry -> path.startsWith(entry.getKey()));
        if (!isSecured) {
            return chain.filter(exchange);
        }
        
        if (isSwaggerPath(path)) {
            return chain.filter(exchange);
        }

        // ... Lógica de verificação e decodificação do JWT (mantida)
        // Se a rota for segura, verifica o cabeçalho Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        DecodedJWT jwt;
        try {
            // ... Lógica de decodificação e verificação do JWT
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            jwt = verifier.verify(token);
        } catch(Exception e) {
            return unauthorized(exchange);
        }

        // verifica se o jwt é access
        String tokenType = jwt.getClaim("type").asString();
        if (!tokenType.equals("access")) {
            return unauthorized(exchange);
        }

        // NOVO: Extrai o ID do usuário e adiciona ao cabeçalho da requisição para serviços downstream
        String userId = jwt.getSubject();
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
            .header("X-User-ID", userId) // Injeta o ID do usuário no cabeçalho
            .build();
        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

        // verifica se está com uma role valida
        String userRole = jwt.getClaim("role").asString();
        RoleType roleType = null;
        try {
            roleType = RoleType.valueOf(userRole);
        } catch (Exception e) {
            return unauthorized(modifiedExchange);
        }

        // verifica a permissão com base na role 
        if (!isAuthorized(path, roleType)) {
            modifiedExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return modifiedExchange.getResponse().setComplete();
        }


        return chain.filter(modifiedExchange); // Passa a requisição modificada adiante
    }
}
