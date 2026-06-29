package com.authsphere.account.mapper;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountMapperXmlTest {

    @Test
    void listSubjectsShouldReturnDefaultSubjectFlag() throws Exception {
        try (var input = getClass().getResourceAsStream("/mapper/AccountMapper.xml")) {
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(xml.contains("sm.is_default as isDefault"));
        }
    }

    @Test
    void listSubjectsShouldHideRemovedMembers() throws Exception {
        try (var input = getClass().getResourceAsStream("/mapper/AccountMapper.xml")) {
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(xml.contains("and sm.status != 3"));
        }
    }
}
