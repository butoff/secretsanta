package io.butoff.secretsanta;

public final class LoginUtils {

    public static String getLoginByEmail(String mail) {
        String[] parts = mail.split("@");
        return parts[0];
    }

    public static String getEmailByLogin(String login) {
        return login + "@evotor.ru";
    }

    public static boolean equalsIgnoreWhitespaces(String name1, String name2) {
        String clean1 = name1.replaceAll(" ", "");
        String clean2 = name2.replaceAll(" ", "");
        System.out.println(clean1 + " " + clean1.length());
        System.out.println(clean2 + " " + clean2.length());
        System.out.println(clean1.equals(clean2));
        return clean1.equalsIgnoreCase(clean2);
    }
}
