package org.nezxenka.auth.message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.auth.config.YamlFiles;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.util.Colors;

@RequiredArgsConstructor
public final class LocaleLoader {

    private static final String DIRECTORY = "messages";
    private static final String EXTENSION = ".yml";
    private static final String FALLBACK_LANGUAGE = "en";
    private static final String LEGACY_FILE = "messages.yml";
    private static final String LEGACY_LANGUAGE = "ru";
    private static final List<String> BUNDLED_LANGUAGES = List.of("en", "ru");
    private static final String TELEGRAM_SECTION = "telegram.";
    private static final String MISSING_MESSAGE = "&cMessage not found: ";

    private final JavaPlugin plugin;

    public LocaleBundle load(String requestedLanguage) {
        migrateLegacyFile();
        BUNDLED_LANGUAGES.forEach(language -> YamlFiles.saveDefault(plugin, path(language)));
        String language = resolve(requestedLanguage);
        YamlConfiguration yaml = YamlFiles.read(file(language));
        YamlFiles
            .resource(plugin, path(language))
            .or(() -> YamlFiles.resource(plugin, path(FALLBACK_LANGUAGE)))
            .ifPresent(yaml::setDefaults);
        return new LocaleBundle(
            language,
            Colors.translate(yaml.getString("prefix", "")),
            loadMessages(yaml),
            loadTelegramTexts(yaml)
        );
    }

    private String resolve(String requestedLanguage) {
        String language = requestedLanguage == null
            ? FALLBACK_LANGUAGE
            : requestedLanguage.trim().toLowerCase(Locale.ROOT);
        if (file(language).exists()) {
            return language;
        }
        plugin.getLogger().warning("Language file " + path(language) + " not found, using " + FALLBACK_LANGUAGE);
        return FALLBACK_LANGUAGE;
    }

    private void migrateLegacyFile() {
        File legacy = new File(plugin.getDataFolder(), LEGACY_FILE);
        File target = file(LEGACY_LANGUAGE);
        if (!legacy.exists() || target.exists()) {
            return;
        }
        try {
            Files.createDirectories(target.toPath().getParent());
            Files.move(legacy.toPath(), target.toPath());
            plugin.getLogger().info("Migrated " + LEGACY_FILE + " to " + path(LEGACY_LANGUAGE));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to migrate " + LEGACY_FILE, exception);
        }
    }

    private static Map<MessageKey, String> loadMessages(YamlConfiguration yaml) {
        Map<MessageKey, String> messages = new EnumMap<>(MessageKey.class);
        for (MessageKey key : MessageKey.values()) {
            String value = yaml.getString(key.getPath());
            messages.put(key, Colors.translate(value != null ? value : MISSING_MESSAGE + key.getPath()));
        }
        return Collections.unmodifiableMap(messages);
    }

    private static Map<TelegramText, String> loadTelegramTexts(YamlConfiguration yaml) {
        Map<TelegramText, String> texts = new EnumMap<>(TelegramText.class);
        for (TelegramText text : TelegramText.values()) {
            String value = yaml.getString(TELEGRAM_SECTION + text.getKey());
            texts.put(text, value != null ? value : text.getKey());
        }
        return Collections.unmodifiableMap(texts);
    }

    private File file(String language) {
        return new File(plugin.getDataFolder(), path(language));
    }

    private static String path(String language) {
        return DIRECTORY + "/" + language + EXTENSION;
    }
}
