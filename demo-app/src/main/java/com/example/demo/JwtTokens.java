package com.example.demo;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokens {
    static final Duration LIFETIME = Duration.ofHours(1);
    private static final String ISSUER = "agentic-demo";
    private final byte[] secret;
    private final Clock clock;

    @Autowired
    JwtTokens(@Value("${demo.jwt-secret}") String secret) {
        this(secret, Clock.systemUTC());
    }

    JwtTokens(String secret, Clock clock) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) throw new IllegalArgumentException("JWT secret requires 32 bytes");
        this.clock = clock;
    }

    public String issue(String subject) {
        var now = clock.instant();
        var claims = new JWTClaimsSet.Builder().issuer(ISSUER).subject(subject)
            .issueTime(Date.from(now)).expirationTime(Date.from(now.plus(Duration.ofMinutes(1)))).build();
        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
        try {
            token.sign(new MACSigner(secret));
            return token.serialize();
        } catch (JOSEException error) {
            throw new IllegalStateException("Unable to sign token", error);
        }
    }

    public Optional<String> subject(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return Optional.empty();
        try {
            var token = SignedJWT.parse(authorization.substring(7));
            if (!JWSAlgorithm.HS256.equals(token.getHeader().getAlgorithm())
                || !token.verify(new MACVerifier(secret))) return Optional.empty();
            var claims = token.getJWTClaimsSet();
            var expires = claims.getExpirationTime();
            if (!ISSUER.equals(claims.getIssuer()) || expires == null
                || !expires.toInstant().isAfter(clock.instant())) return Optional.empty();
            return Optional.ofNullable(claims.getSubject()).filter(subject -> !subject.isBlank());
        } catch (ParseException | JOSEException error) {
            return Optional.empty();
        }
    }
}
