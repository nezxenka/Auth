package org.nezxenka.auth.message;

public record Placeholder(String token, String value) {

    public static Placeholder of(String name, Object value) {
        return new Placeholder("%" + name + "%", String.valueOf(value));
    }

    public static String apply(String text, Placeholder... placeholders) {
        String result = text;
        for (Placeholder placeholder : placeholders) {
            result = result.replace(placeholder.token(), placeholder.value());
        }
        return result;
    }
}
