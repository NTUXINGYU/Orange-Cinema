package com.orange.moviebackend.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // Token expiration time in milliseconds (default: 1 hour)
    private static final long EXPIRE_TIME = 60 * 60 * 1000;

    /**
     * Check if token is not expired.
     */
    public static boolean isTokenValid(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().after(new Date());
        } catch (TokenExpiredException e) {
            log.warn("Token expired: {}", token);
            return false;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify token signature and username claim.
     */
    public static boolean verify(String token, String username, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("username", username)
                    .build();
            verifier.verify(token);
            log.debug("Token verified successfully: username={}", username);
            return true;
        } catch (TokenExpiredException e) {
            log.warn("Token expired: {}", token);
            return false;
        } catch (Exception e) {
            log.error("Token verification failed: token={}, username={}, error={}", token, username, e.getMessage());
            return false;
        }
    }

    /**
     * Get username from token without needing the secret.
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            log.error("Failed to decode token: {}", token);
            return null;
        }
    }

    /**
     * Generate a signed JWT token that expires after EXPIRE_TIME.
     */
    public static String sign(String username, String secret) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    /**
     * Get token expiration date.
     */
    public static Date getExpiration(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt();
        } catch (JWTDecodeException e) {
            log.error("Failed to get expiration time: {}", token);
            return null;
        }
    }

    /**
     * Get token issued at date.
     */
    public static Date getIssuedAt(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getIssuedAt();
        } catch (JWTDecodeException e) {
            log.error("Failed to get issued time: {}", token);
            return null;
        }
    }

    /**
     * Check if token will expire within the given time.
     */
    public static boolean isTokenAboutToExpire(String token, long milliseconds) {
        try {
            Date expiresAt = getExpiration(token);
            if (expiresAt == null) return false;

            long diff = expiresAt.getTime() - System.currentTimeMillis();
            return diff > 0 && diff <= milliseconds;
        } catch (Exception e) {
            log.error("Failed to check token expiration status: {}", e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        String username = "testuser";
        String secret = "f93643c0eacc54a5ee1783744466ab9e";

        // Generate token
        String token = sign(username, secret);
        System.out.println("Generated Token: " + token);

        // Validate token
        boolean isValid = isTokenValid(token);
        boolean isVerified = verify(token, username, secret);

        System.out.println("Is Token Valid: " + isValid);
        System.out.println("Token Verification Result: " + isVerified);
        System.out.println("Username from Token: " + getUsername(token));
        System.out.println("Token Expiration Time: " + getExpiration(token));
    }
}