package fr.army.stelyteam.menu.impl;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.army.stelyteam.conversation.ConvAddMoney;
import fr.army.stelyteam.conversation.ConvWithdrawMoney;
import fr.army.stelyteam.menu.Buttons;
import fr.army.stelyteam.menu.FixedMenu;
import fr.army.stelyteam.menu.Menus;
import fr.army.stelyteam.menu.TeamMenu;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.TemporaryAction;
import fr.army.stelyteam.utils.TemporaryActionNames;
import fr.army.stelyteam.utils.builder.ColorsBuilder;
import fr.army.stelyteam.utils.builder.ItemBuilder;
import fr.army.stelyteam.utils.builder.conversation.ConversationBuilder;



public class MemberMenu extends FixedMenu {

    private final ConversationBuilder conversationBuilder = plugin.getConversationBuilder();

    public MemberMenu(Player viewer, TeamMenu previousMenu) {
        super(
            viewer,
            Menus.MEMBER_MENU.getName(),
            Menus.MEMBER_MENU.getSlots(),
            previousMenu
        );
    }


    public Inventory createInventory(String playerName) {
        String teamName = team.getTeamName();
        String teamPrefix = team.getTeamPrefix();
        String teamOwner = team.getTeamOwnerName();
        Integer teamMembersLelvel = team.getImprovLvlMembers();
        Integer teamMembers = team.getTeamMembers().size();
        String membershipDate = team.getMembershipDate(playerName);
        Double teamMoney = team.getTeamMoney();
        String teamDescription = team.getTeamDescription();
        Integer maxMembers = config.getInt("teamMaxMembers");
        String memberRank = plugin.getRankFromId(team.getMemberRank(playerName));
        String memberRankName = config.getString("ranks." + memberRank + ".name");
        String rankColor = config.getString("ranks." + memberRank + ".color");
        Inventory inventory = Bukkit.createInventory(this, this.menuSlots, this.menuName);

        emptyCases(inventory, this.menuSlots, 0);

        for(String buttonName : config.getConfigurationSection("inventories.member").getKeys(false)){
            Integer slot = config.getInt("inventories.member."+buttonName+".slot");
            Material material = Material.getMaterial(config.getString("inventories.member."+buttonName+".itemType"));
            String displayName = config.getString("inventories.member."+buttonName+".itemName");
            List<String> lore = config.getStringList("inventories.member."+buttonName+".lore");
            String headTexture = config.getString("inventories.member."+buttonName+".headTexture");
            ItemStack item;

            if (displayName.equals(config.getString("inventories.member.seeTeamBank.itemName"))){
                lore = replaceInLore(lore, "%TEAM_MONEY%", DoubleToString(teamMoney));
                lore = replaceInLore(lore, "%MAX_MONEY%", DoubleToString(config.getDouble("teamMaxMoney")));
            }else if (displayName.equals(config.getString("inventories.member.teamInfos.itemName"))){
                lore = replaceInLore(lore, "%NAME%", teamName);
                lore = replaceInLore(lore, "%PREFIX%", ColorsBuilder.replaceColor(teamPrefix));
                lore = replaceInLore(lore, "%OWNER%", teamOwner);
                lore = replaceInLore(lore, "%RANK%", rankColor + memberRankName);
                lore = replaceInLore(lore, "%DATE%", membershipDate);
                lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(teamMembers));
                lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+teamMembersLelvel));
                lore = replaceInLore(lore, "%DESCRIPTION%", ColorsBuilder.replaceColor(teamDescription));
            }

            if (plugin.playerHasPermission(playerName, team, buttonName)){ 
                item = ItemBuilder.getItem(material, buttonName, displayName, lore, headTexture, false);
            }else{
                item = ItemBuilder.getItem(
                    Material.getMaterial(config.getString("noPermission.itemType")),
                    buttonName,
                    displayName, 
                    config.getStringList("noPermission.lore"),
                    config.getString("noPermission.headTexture"),
                    false
                );
            }

            if (buttonName.equals("seeTeamBank")){
                if (!team.isUnlockedTeamBank()){
                    item = ItemBuilder.getItem(
                        Material.getMaterial(config.getString("teamBankNotUnlock.itemType")),
                        "teamBankNotUnlock",
                        config.getString("teamBankNotUnlock.itemName"),
                        Collections.emptyList(),
                        config.getString("teamBankNotUnlock.headTexture"),
                        false
                    );
                }
            }

            inventory.setItem(slot,  item);
        }
        return inventory;
    }


    @Override
    public void openMenu(){
        this.open(createInventory(viewer.getName()));
    }


    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        String playerName = player.getName();
        Team team = Team.initFromPlayerName(playerName);
        Material itemType = clickEvent.getCurrentItem().getType();
        List<String> lore = clickEvent.getCurrentItem().getItemMeta().getLore();

        if (itemType.equals(Material.getMaterial(config.getString("noPermission.itemType"))) && lore.equals(config.getStringList("noPermission.lore"))){
            return;
        }

        // Fermeture ou retour en arrière de l'inventaire        
        if (Buttons.CLOSE_MEMBER_MENU_BUTTON.isClickedButton(clickEvent)){
            if (team.isTeamOwner(playerName) 
                    || plugin.playerHasPermissionInSection(playerName, team, "manage")
                    || plugin.playerHasPermissionInSection(playerName, team, "editMembers")
                    || plugin.playerHasPermissionInSection(playerName, team, "editAlliances")
                    || plugin.playerHasPermission(playerName, team, "teamList")){
                new AdminMenu(player, this).openMenu();
            }else{
                player.closeInventory();
            }
        }else if (Buttons.TEAM_MEMBERS_BUTTON.isClickedButton(clickEvent)){
            new MembersMenu(player, this).openMenu();
        
        }else if (Buttons.TEAM_ALLIANCES_BUTTON.isClickedButton(clickEvent)){
            new AlliancesMenu(player, this).openMenu();
        
        }else if (Buttons.ADD_MONEY_TEAM_BANK_BUTTON.isClickedButton(clickEvent)){
            if (!team.isUnlockedTeamBank()) {
                player.sendMessage(messageManager.getMessage("common.team_bank_not_unlock"));
            }else{
                player.closeInventory();
                conversationBuilder.getNameInput(player, new ConvAddMoney(plugin));
            }
        }else if (Buttons.WITHDRAW_MONEY_TEAM_BANK_BUTTON.isClickedButton(clickEvent)){
            if (!team.isUnlockedTeamBank()) {
                player.sendMessage(messageManager.getMessage("common.team_bank_not_unlock"));
            }else{
                player.closeInventory();
                conversationBuilder.getNameInput(player, new ConvWithdrawMoney(plugin));
            }
        }else if (Buttons.LEAVE_TEAM_BUTTON.isClickedButton(clickEvent)){
            player.closeInventory();
            if (!team.isTeamOwner(playerName)){
                cacheManager.addTempAction(new TemporaryAction(playerName, TemporaryActionNames.LEAVE_TEAM, team));
                new ConfirmMenu(player, this).openMenu();
            }else {
                player.sendMessage(messageManager.getMessage("other.owner_cant_leave_team"));
            }
        }else if (Buttons.STORAGE_DIRECTORY_BUTTON.isClickedButton(clickEvent)){
            new StorageDirectoryMenu(player, this).openMenu();
        }
    }


    @Override
    public void onClose(InventoryCloseEvent closeEvent) {}
}
