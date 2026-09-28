package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;

public record PasswordSettings(int minLength, int maxLength, int bcryptCost, String legacyAlgorithm) {

    private static final int MIN_BCRYPT_COST = 4;
    private static final int MAX_BCRYPT_COST = 31;

    public static PasswordSettings from(ConfigurationSection section) {
        int minLength = Math.max(1, section.getInt("min-length", 6));
        int maxLength = Math.max(minLength, section.getInt("max-length", 30));
        int cost = Math.min(MAX_BCRYPT_COST, Math.max(MIN_BCRYPT_COST, section.getInt("bcrypt-cost", 10)));
        String legacyAlgorithm = section.getString(
            "legacy-hash-algorithm",
            section.getString("hash-algorithm", "SHA-256")
        );
        return new PasswordSettings(minLength, maxLength, cost, legacyAlgorithm);
    }
}
