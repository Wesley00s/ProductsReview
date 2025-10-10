package com.example.productsreview.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.productsreview.listener.dto.AuthenticatedUser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class TokenService {

    @Value("${app.issuer}")
    private String issuer;

    @Value("${jwt.key.public.path}")
    private String publicKeyPath;

    private RSAPublicKey publicKey;

    @PostConstruct
    public void loadKeys() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = loadKeyBytes(publicKeyPath);
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);

            log.info("Chaves RSA carregadas com sucesso.");

        } catch (Exception e) {
            log.error("Erro ao carregar as chaves RSA", e);
            throw new RuntimeException("Erro ao carregar as chaves RSA", e);
        }
    }

    private byte[] loadKeyBytes(String path) throws Exception {
        String content;
        if (path.startsWith("classpath:")) {
            InputStream is = getClass().getResourceAsStream("/" + path.substring(10));
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } else {
            content = new String(Files.readAllBytes(Paths.get(path)));
        }

        String key = content
                .replaceAll("\\n", "")
                .replaceAll("-----(BEGIN|END) (PRIVATE|PUBLIC) KEY-----", "");

        return Base64.getDecoder().decode(key);
    }

    public AuthenticatedUser validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);

            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            String email = decodedJWT.getSubject();
            String userId = decodedJWT.getClaim("userId").asString();
            String userName = decodedJWT.getClaim("userName").asString();

            if (email == null || userId == null) {
                log.warn("O token não possui as claims obrigatórias (sub ou userId).");
                return null;
            }

            return new AuthenticatedUser(userId, userName, email);

        } catch (JWTVerificationException e) {
            log.warn("Token JWT inválido ou expirado: {}", e.getMessage());
            return null;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            return JWT.decode(token).getClaim("userId").asString();
        } catch (JWTDecodeException e) {
            log.warn("Não foi possível decodificar o token ou a claim 'userId' não foi encontrada.", e);
            return null;
        }
    }
}
