package org.nezxenka.auth.config.settings;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;

public record RestrictionSettings(
    boolean blockCommands,
    boolean blockMovement,
    boolean blockChat,
    boolean blockInteract,
    boolean blockDamage,
    boolean blockInventory,
    boolean blockDrop,
    boolean blockPickup,
    boolean blockBreak,
    boolean blockPlace,
    Set<String> allowedCommands
) {

    private static final List<String> DEFAULT_ALLOWED_COMMANDS = List.of("login", "l", "log", "register", "reg");

    public static RestrictionSettings from(ConfigurationSection section) {
        List<String> configured = section.getStringList("allowed-commands");
        Set<String> allowed = (configured.isEmpty() ? DEFAULT_ALLOWED_COMMANDS : configured)
            .stream()
            .map(command -> command.trim().toLowerCase(Locale.ROOT))
            .map(command -> command.startsWith("/") ? command.substring(1) : command)
            .collect(Collectors.toUnmodifiableSet());
        return new RestrictionSettings(
            section.getBoolean("block-commands", true),
            section.getBoolean("block-movement", true),
            section.getBoolean("block-chat", true),
            section.getBoolean("block-interact", true),
            section.getBoolean("block-damage", true),
            section.getBoolean("block-inventory", true),
            section.getBoolean("block-drop", true),
            section.getBoolean("block-pickup", true),
            section.getBoolean("block-break", true),
            section.getBoolean("block-place", true),
            allowed
        );
    }

    public boolean isCommandAllowed(String label) {
        return allowedCommands.contains(label);
    }
}
