package fr.army.stelyteam.menu.impl.temp_new;

import fr.army.stelyteam.menu.TeamMenu;
import fr.army.stelyteam.menu.view.impl.TeamMenuView;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.builder.menu.MenuBuilder;
import fr.army.stelyteam.utils.builder.menu.MenuBuilderResult;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConfirmAddMoneyMenu extends TeamMenu<TeamMenuView> {

    public ConfirmAddMoneyMenu(@NotNull MenuBuilderResult<TeamMenuView> menuBuilderResult) {
        super(menuBuilderResult);
    }

    @Override
    public TeamMenuView createView(Player player, Optional<Team> team) {
        return new TeamMenuView(player, this, team.orElse(null));
    }

    @Override
    public void onClick(InventoryClickEvent clickEvent) {
    }

    public static ConfirmAddMoneyMenu createInstance(String configName){
        final MenuBuilderResult<TeamMenuView> builderResult = MenuBuilder.getInstance().loadMenu(configName + ".yml");
        return new ConfirmAddMoneyMenu(builderResult);
    }
}