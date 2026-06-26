package com.authsphere.server.account.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;

/**
 * 密码哈希服务。
 */
@Service
@RequiredArgsConstructor
public class PasswordHashService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String ALL = UPPER + LOWER + DIGIT;

    private static final String PASSWORD_ALGORITHM = "PBKDF2_SHA256";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    private final PasswordSecurityProperties properties;


    public PasswordSecurityProperties getProperties() {
        return properties;
    }

    /**
     * 使用随机盐值对密码进行哈希。
     */
    public PasswordHash hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return hash(rawPassword, Base64.getEncoder().encodeToString(salt));
    }

    /**
     * 使用指定盐值对密码进行哈希，便于单元测试验证 pepper 行为。
     */
    PasswordHash hash(String rawPassword, String encodedSalt) {
        Assert.hasText(rawPassword, "rawPassword must not be blank");
        if (!StringUtils.hasText(properties.getPepper())) {
            throw new IllegalStateException("authsphere.password.pepper 未配置");
        }
        byte[] salt = Base64.getDecoder().decode(encodedSalt);
        char[] source = (rawPassword + properties.getPepper()).toCharArray();
        PBEKeySpec spec = new PBEKeySpec(source, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hashed = factory.generateSecret(spec).getEncoded();
            return new PasswordHash(
                    Base64.getEncoder().encodeToString(hashed),
                    encodedSalt,
                    PASSWORD_ALGORITHM
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("密码哈希失败", exception);
        } finally {
            spec.clearPassword();
            specClear(source);
        }
    }

    /**
     * 生成临时密码
     */
    public String generateTemplatePassword(int length) {
        if (length < 12) {
            throw new IllegalArgumentException("临时密码长度不能小于 12 位");
        }

        List<Character> chars = new ArrayList<>();

        chars.add(randomChar(UPPER));
        chars.add(randomChar(LOWER));
        chars.add(randomChar(DIGIT));

        for (int i = chars.size(); i < length; i++) {
            chars.add(randomChar(ALL));
        }

        Collections.shuffle(chars, SECURE_RANDOM);

        StringBuilder password = new StringBuilder();
        for (Character c : chars) {
            password.append(c);
        }

        return password.toString();
    }

    private static char randomChar(String source) {
        return source.charAt(SECURE_RANDOM.nextInt(source.length()));
    }

    private void specClear(char[] source) {
        Arrays.fill(source, (char) 0);
    }
}
