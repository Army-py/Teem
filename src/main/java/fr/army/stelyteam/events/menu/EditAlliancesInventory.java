package fr.army.stelyteam.events.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.army.stelyteam.conversations.ConvAddAlliance;
import fr.army.stelyteam.conversations.ConvRemoveAlliance;
import fr.army.stelyteam.utils.Alliance;
import fr.army.stelyteam.utils.Team;
import fr.army.stelyteam.utils.TeamMenu;
import fr.army.stelyteam.utils.builder.ColorsBuilder;
import fr.army.stelyteam.utils.builder.ItemBuilder;
import fr.army.stelyteam.utils.builder.conversation.ConversationBuilder;
import fr.army.stelyteam.utils.manager.database.DatabaseManager;
import fr.army.stelyteam.utils.manager.database.SQLiteDataManager;


public class EditAlliancesInventory extends TeamMenu {

    final DatabaseManager mySqlManager = plugin.getDatabaseManager();
    final SQLiteDataManager sqliteManager = plugin.getSQLiteManager();
    final ColorsBuilder colorsBuilder = plugin.getColorsBuilder();
    final ConversationBuilder conversationBuilder = plugin.getConversationBuilder();


    public EditAlliancesInventory(Player viewer){
        super(viewer);
    }


    public Inventory createInventory(Team team, String playerName) {
        Integer slots = config.getInt("inventoriesSlots.editAlliances");
        Inventory inventory = Bukkit.createInventory(this, slots, config.getString("inventoriesName.editAlliances"));
        Integer maxMembers = config.getInt("teamMaxMembers");

        emptyCases(inventory, slots, 0);
        Integer headSlot = 0;
        for(Alliance alliance : team.getTeamAlliances()){
            Team teamAlliance = mySqlManager.getTeamFromTeamName(alliance.getTeamName());
            String allianceName = teamAlliance.getTeamName();
            String alliancePrefix = teamAlliance.getTeamPrefix();
            String allianceOwnerName = teamAlliance.getTeamOwnerName();
            String allianceDate = alliance.getAllianceDate();
            String allianceDescription = teamAlliance.getTeamDescription();
            Integer teamMembersLelvel = teamAlliance.getImprovLvlMembers();
            Integer teamMembers = teamAlliance.getTeamMembers().size();
            ArrayList<String> allianceMembers = teamAlliance.getMembersName();
            UUID playerUUID = sqliteManager.getUUID(allianceOwnerName);
            String itemName = colorsBuilder.replaceColor(alliancePrefix);
            List<String> lore = config.getStringList("teamAllianceLore");
            OfflinePlayer allianceOwner;
            ItemStack item;


            if (playerUUID == null) allianceOwner = Bukkit.getOfflinePlayer(allianceOwnerName);
            else allianceOwner = Bukkit.getOfflinePlayer(playerUUID);

            
            lore = replaceInLore(lore, "%OWNER%", allianceOwnerName);
            lore = replaceInLore(lore, "%NAME%", allianceName);
            lore = replaceInLore(lore, "%PREFIX%", colorsBuilder.replaceColor(alliancePrefix));
            lore = replaceInLore(lore, "%DATE%", allianceDate);
            lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(teamMembers));
            lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+teamMembersLelvel));
            lore = replaceInLore(lore, "%MEMBERS%", String.join(", ", allianceMembers));
            lore = replaceInLore(lore, "%DESCRIPTION%", colorsBuilder.replaceColor(allianceDescription));
            
            
            if (plugin.playerHasPermission(playerName, team, "seeAllliances")){ 
                item = ItemBuilder.getPlayerHead(allianceOwner, itemName, lore);
            }else{
                item = ItemBuilder.getItem(
                    Material.getMaterial(config.getString("noPermission.itemType")), 
                    itemName, 
                    config.getStringList("noPermission.lore"), 
                    config.getString("noPermission.headTexture"),
                    false
                );
            }

            inventory.setItem(headSlot, item);
            headSlot ++;
        }

        for(String str : config.getConfigurationSection("inventories.editAlliances").getKeys(false)){
            Integer slot = config.getInt("inventories.editAlliances."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.editAlliances."+str+".itemType"));
            String name = config.getString("inventories.editAlliances."+str+".itemName");
            String headTexture = config.getString("inventories.editAlliances."+str+".headTexture");
            ItemStack item;

            if (plugin.playerHasPermission(playerName, team, str)){ 
                item = ItemBuilder.getItem(
                    material,
                    name,
                    config.getStringList("inventories.editAlliances."+str+".lore"),
                    headTexture,
                    false);
            }else{
                item = ItemBuilder.getItem(
                    Material.getMaterial(config.getString("noPermission.itemType")), 
                    name, 
                    config.getStringList("noPermission.lore"),
                    config.getString("noPermission.headTexture"),
                    false
                );
            }
            inventory.setItem(slot, item);
        }
        return inventory;
    }


    public void openMenu(Team team){
        this.open(createInventory(team, viewer.getName()));
    }


    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        String itemName;
        Player player = (Player) clickEvent.getWhoClicked();
        String playerName = player.getName();
        Team team = mySqlManager.getTeamFromPlayerName(playerName);


        itemName = clickEvent.getCurrentItem().getItemMeta().getDisplayName();
        if (itemName.equals(config.getString("inventories.editAlliances.close.itemName"))){
            new ManageInventory(player).openMenu(team);
            return;
        }else if (itemName.equals(config.getString("inventories.editAlliances.addAlliance.itemName"))){
            player.closeInventory();
            conversationBuilder.getNameInput(player, new ConvAddAlliance(plugin));
            return;
        }else if (itemName.equals(config.getString("inventories.editAlliances.removeAlliance.itemName"))){
            player.closeInventory();
            conversationBuilder.getNameInput(player, new ConvRemoveAlliance(plugin));
            return;
        }
    }


    @Override
    public void onClose(InventoryCloseEvent closeEvent) {}
}