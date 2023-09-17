package fr.army.stelyteam.utils.builder.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.menu.button.Button;
import fr.army.stelyteam.menu.button.ButtonItem;
import fr.army.stelyteam.menu.button.impl.BlankButton;
import fr.army.stelyteam.menu.button.template.ButtonTemplate;
import fr.army.stelyteam.menu.template.MenuTemplate;
import fr.army.stelyteam.menu.view.AbstractMenuView;

public class MenuBuilder {
    
    private static final MenuBuilder INSTANCE = new MenuBuilder();

    private final StelyTeamPlugin plugin = StelyTeamPlugin.getPlugin();

    public static MenuBuilder getInstance() {
        return INSTANCE;
    }


    public <T extends AbstractMenuView<T>> MenuBuilderResult<T> loadMenu(String configName){
        return buildMenu(plugin.getConfigLoader().initFile("menus/" + configName));
    }


    private <T extends AbstractMenuView<T>> MenuBuilderResult<T> buildMenu(YamlConfiguration config) {
        final String title = config.getString("title");
        final String[] pattern = config.getStringList("pattern").toArray(String[]::new);
        final List<Button<T>> buttons = new ArrayList<>();

        int size = 0;
        for (int row = 0; row < pattern.length && row < 6; row++) {
            final String line = pattern[row].replace(" ", "");

            for (int column = 0; column < line.length() && column < 9; column++) {
                final char character = line.charAt(column);

                final String path = "items." + character + ".";

                final String material = config.getString(path + "material");
                final String name = config.getString(path + "name");
                final int amount = config.getInt(path + "amount");
                final List<String> lore = config.getStringList(path + "lore");
                final boolean glow = config.getBoolean(path + "is-glowing");
                final String skullTexture = config.getString(path + "skull-texture");

                final ButtonItem buttonItem = new ButtonItem(Material.valueOf(material), name, amount, lore, glow, skullTexture);
                final ButtonTemplate buttonTemplate = new ButtonTemplate(character, buttonItem);
                final BlankButton<T> button = new BlankButton<>(buttonTemplate);
                buttons.add(button);
                size++;
            }
        }

        final MenuTemplate<T> menuTemplate = new MenuTemplate<>(title, size);
        menuTemplate.addButtons(buttons.toArray(Button[]::new));

        return new MenuBuilderResult<>(menuTemplate, config);
    }
}
