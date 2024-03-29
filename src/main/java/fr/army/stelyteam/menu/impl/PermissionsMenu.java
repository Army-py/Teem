package fr.army.stelyteam.menu.impl;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import fr.army.stelyteam.menu.Buttons;
import fr.army.stelyteam.menu.FixedMenu;
import fr.army.stelyteam.menu.Menus;
import fr.army.stelyteam.menu.TeamMenu;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.builder.ItemBuilder;


public class PermissionsMenu extends FixedMenu {

    public PermissionsMenu(Player viewer, TeamMenu previousMenu) {
        super(
            viewer,
            Menus.PERMISSIONS_MENU.getName(),
            Menus.PERMISSIONS_MENU.getSlots(),
            previousMenu
        );
    }


    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(this, this.menuSlots, this.menuName);

        emptyCases(inventory, this.menuSlots, 0);

        for(String buttonName : config.getConfigurationSection("inventories.permissions").getKeys(false)){
            Integer slot = config.getInt("inventories.permissions."+buttonName+".slot");
            if (slot == -1) continue;

            String displayName = config.getString("inventories.permissions."+buttonName+".itemName");
            List<String> lore = config.getStringList("inventories.permissions."+buttonName+".lore");
            String headTexture;
            
            Integer defaultRankId = config.getInt("inventories.permissions."+buttonName+".rank");
            Integer permissionRank = team.getPermissionRank(buttonName);
            String lorePrefix = config.getString("prefixRankLore");
            Material material;

            if (buttonName.equals("close")){
                material = Material.getMaterial(config.getString("inventories.permissions."+buttonName+".itemType"));
                headTexture = config.getString("inventories.permissions."+buttonName+".headTexture");
            }else{
                material = Material.getMaterial(config.getString("ranks."+plugin.getRankFromId(permissionRank != null ? permissionRank : defaultRankId)+".itemType"));
                headTexture = config.getString("ranks."+plugin.getRankFromId(permissionRank != null ? permissionRank : defaultRankId)+".headTexture");
                
                if (permissionRank != null){
                    String rankColor = config.getString("ranks." + plugin.getRankFromId(permissionRank) + ".color");
                    lore.add(0, lorePrefix + rankColor + config.getString("ranks." + plugin.getRankFromId(permissionRank) + ".name"));
                }else{
                    String rankColor = config.getString("ranks." + plugin.getRankFromId(defaultRankId) + ".color");
                    lore.add(0, lorePrefix + rankColor + config.getString("ranks." + plugin.getRankFromId(defaultRankId) + ".name"));
                }
            }


            boolean isDefault = false;
            if (!buttonName.equals("close") && (permissionRank == null || defaultRankId == permissionRank)){
                isDefault = true;
            }

            inventory.setItem(slot, ItemBuilder.getItem(material, buttonName, displayName, lore, headTexture, isDefault));
        }
        return inventory;
    }


    @Override
    public void openMenu() {
        this.open(createInventory());
    }


    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        String playerName = player.getName();
        String itemName = clickEvent.getCurrentItem().getItemMeta().getDisplayName();
        Team team = Team.initFromPlayerName(playerName);

        // Fermeture ou retour en arrière de l'inventaire
        if (Buttons.CLOSE_PERMISSIONS_MENU_BUTTON.isClickedButton(clickEvent)){
            // new ManageMenu(player, previousMenu).openMenu();
            previousMenu.openMenu();
            return;
        }

        if (getPermissionFromName(itemName) != null){
            String permission = getPermissionFromName(itemName);
            Integer permissionRank = team.getPermissionRank(permission);
            Integer authorRank = team.getMemberRank(playerName);
            boolean authorIsOwner = team.isTeamOwner(playerName);
            if (clickEvent.getClick().isRightClick()){
                if (permissionRank != null){
                    if (!authorIsOwner && permissionRank <= authorRank){
                        return;
                    }
                    if (permissionRank < plugin.getLastRank()) team.incrementAssignement(permission);
                }else{
                    Integer defaultRankId = config.getInt("inventories.permissions."+permission+".rank");
                    if (!authorIsOwner && defaultRankId <= authorRank){
                        return;
                    }
                    if (defaultRankId != plugin.getLastRank()) defaultRankId = defaultRankId+1;
                    team.insertAssignement(permission, defaultRankId);
                }
            }else if (clickEvent.getClick().isLeftClick()){
                if (permissionRank != null){
                    if (!authorIsOwner && permissionRank-1 <= authorRank){
                        return;
                    }
                    if (permissionRank > 0) team.decrementAssignement(permission);
                }else{
                    Integer defaultRankId = config.getInt("inventories.permissions."+permission+".rank");
                    if (!authorIsOwner && defaultRankId-1 <= authorRank){
                        return;
                    }
                    if (defaultRankId != 0) defaultRankId = defaultRankId-1;
                    team.insertAssignement(permission, defaultRankId);
                }
            }else return;
            new PermissionsMenu(player, previousMenu).openMenu();
            team.refreshTeamMembersInventory(playerName);
        }
    }


    @Override
    public void onClose(InventoryCloseEvent closeEvent) {}



    private String getPermissionFromName(String value) {
        for (String key : config.getConfigurationSection("inventories.permissions").getKeys(false)) {
            if (config.getString("inventories.permissions." + key + ".itemName").equals(value)) {
                return key;
            }
        }
        return null;
    }
}
