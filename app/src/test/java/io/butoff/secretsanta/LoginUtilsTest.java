package io.butoff.secretsanta;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoginUtilsTest {

    @Test
    public void splitOk() {
        String email = "v.poupkine@evotor.ru";
        String login = LoginUtils.getLoginByEmail(email);
        assertEquals("v.poupkine", login);
    }

    @Test
    public void mailOk() {
        String login = "v.poupkine";
        String email = LoginUtils.getEmailByLogin(login);
        assertEquals("v.poupkine@evotor.ru", email);
    }

    @Test
    public void testCompareIgnoreWhitespaces() {
        String name = "Ковалюк Анна";
        String nameWithSpaces = " Ковалюк  Анна  ";
        assertTrue(LoginUtils.equalsIgnoreWhitespaces(name, nameWithSpaces));
    }
}