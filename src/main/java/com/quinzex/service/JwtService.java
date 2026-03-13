package com.quinzex.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JwtService {
private final PublicKey publicKey;

public JwtService(@Value("${jwt.rsa.public-key}") String publicKeyStr) throws Exception {
this.publicKey= loadPublicKey(publicKeyStr);
}

private PublicKey loadPublicKey(String key) throws Exception {
byte[] decodedKey = Base64.getDecoder().decode(key);
return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
}

public Claims parseToken(String token) {
    return  Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
}
}
