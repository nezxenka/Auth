package org.nezxenka.auth.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class Colors {

    public String translate(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}
