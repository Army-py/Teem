package fr.army.stelyteam.external.stelyclaim.handler;

import fr.flowsqy.stelyclaim.api.ClaimOwner;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;

public class TeamOwner implements ClaimOwner {

    private final OfflinePlayer player;

    public TeamOwner(OfflinePlayer player) {
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public Set<OfflinePlayer> getMailable() {
        return Collections.singleton(player);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean own(Player player) {
        return this.player.equals(player);
    }

}
