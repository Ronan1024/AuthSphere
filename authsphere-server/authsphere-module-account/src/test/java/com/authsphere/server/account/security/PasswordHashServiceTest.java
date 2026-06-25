package com.authsphere.server.account.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordHashServiceTest {

    @Test
    void hashWithSaltShouldIncludePepper() {
        PasswordSecurityProperties first = new PasswordSecurityProperties();
        first.setPepper("pepper-A");
        PasswordSecurityProperties second = new PasswordSecurityProperties();
        second.setPepper("pepper-B");

        PasswordHashService firstService = new PasswordHashService(first);
        PasswordHashService secondService = new PasswordHashService(second);

        PasswordHash firstHash = firstService.hash("AuthSphere#123", "c2FsdC12YWx1ZQ");
        PasswordHash secondHash = secondService.hash("AuthSphere#123", "c2FsdC12YWx1ZQ");

        assertEquals("c2FsdC12YWx1ZQ", firstHash.salt());
        assertEquals("c2FsdC12YWx1ZQ", secondHash.salt());
        assertNotEquals(firstHash.hash(), secondHash.hash());
    }

    @Test
    void hashShouldRejectBlankPepper() {
        PasswordSecurityProperties properties = new PasswordSecurityProperties();
        PasswordHashService service = new PasswordHashService(properties);

        assertThrows(IllegalStateException.class, () -> service.hash("AuthSphere#123", "c2FsdC12YWx1ZQ"));
    }
}
