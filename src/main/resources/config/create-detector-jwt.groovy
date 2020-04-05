package config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import rocks.metaldetector.butler.config.security.UserRole

import java.time.LocalDateTime
import java.time.ZoneId

final String TOKEN_SECRET = System.getenv("TOKEN_SECRET")
final String AUTHORITIES_KEY = "auth"

final String token = Jwts.builder()
    .setSubject("metal-detector.rocks")
    .setId(UUID.randomUUID().toString())
    .setIssuedAt(new Date(System.currentTimeMillis()))
    .claim(AUTHORITIES_KEY, UserRole.ROLE_ADMINISTRATOR.name + "," + UserRole.ROLE_USER.name)
    .setIssuer("mrb")
    .setExpiration(Date.from(LocalDateTime.now().atZone(ZoneId.of("Europe/Berlin")).plusYears(1).toInstant()))
    .signWith(SignatureAlgorithm.HS512, TOKEN_SECRET.bytes.encodeBase64().toString())
    .compact()

System.out.println(token)