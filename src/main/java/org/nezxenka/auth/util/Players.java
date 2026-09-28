package org.nezxenka.auth.util;

import java.net.InetSocketAddress;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class Players {

    private final String UNKNOWN_ADDRESS = "unknown";

    public String address(Player player) {
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return UNKNOWN_ADDRESS;
        }
        return address.getAddress().getHostAddress();
    }
}
