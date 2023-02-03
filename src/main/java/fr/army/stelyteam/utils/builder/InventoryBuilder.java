package fr.army.stelyteam.utils.builder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.utils.Page;
import fr.army.stelyteam.utils.Storage;
import fr.army.stelyteam.utils.Team;
import fr.army.stelyteam.utils.manager.CacheManager;
import fr.army.stelyteam.utils.manager.SQLManager;
import fr.army.stelyteam.utils.manager.SQLiteManager;
import fr.army.stelyteam.utils.manager.serialize.ItemStackSerializer;

public class InventoryBuilder {

    private StelyTeamPlugin plugin;
    private CacheManager cacheManager;
    private YamlConfiguration config;
    private SQLManager sqlManager;
    private SQLiteManager sqliteManager;
    private ItemStackSerializer serializeManager;
    private ColorsBuilder colorsBuilder;


    public InventoryBuilder(StelyTeamPlugin plugin) {
        this.plugin = plugin;
        this.cacheManager = plugin.getCacheManager();
        this.config = plugin.getConfig();
        this.sqlManager = plugin.getSQLManager();
        this.sqliteManager = plugin.getSQLiteManager();
        this.serializeManager = new ItemStackSerializer(plugin);
        this.colorsBuilder = new ColorsBuilder(plugin);
    }


	public Inventory createTeamInventory() {
		Integer slots = config.getInt("inventoriesSlots.createTeam");
		Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.createTeam"));
		
        emptyCases(inventory, slots, 0);
		
        Integer slot = config.getInt("inventories.createTeam.slot");
        Material material = Material.getMaterial(config.getString("inventories.createTeam.itemType"));
        String name = config.getString("inventories.createTeam.itemName");
        List<String> lore = config.getStringList("inventories.createTeam.lore");
        String headTexture = config.getString("inventories.createTeam.headTexture");

        inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
		
		return inventory;
	}


    public Inventory createAdminInventory() {
        Integer slots = config.getInt("inventoriesSlots.admin");
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.admin"));

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.admin").getKeys(false)){
            Integer slot = config.getInt("inventories.admin."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.admin."+str+".itemType"));
            String name = config.getString("inventories.admin."+str+".itemName");
            List<String> lore = config.getStringList("inventories.admin."+str+".lore");
            String headTexture = config.getString("inventories.admin."+str+".headTexture");
            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }
        return inventory;
    }


    public Inventory createManageInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.manage");
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.manage"));
        String teamId = sqlManager.getTeamNameFromPlayerName(playername);

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.manage").getKeys(false)){
            Integer slot = config.getInt("inventories.manage."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.manage."+str+".itemType"));
            String name = config.getString("inventories.manage."+str+".itemName");
            List<String> lore = config.getStringList("inventories.manage."+str+".lore");
            String headTexture = config.getString("inventories.manage."+str+".headTexture");
            
            ItemStack item;
            
            if (plugin.playerHasPermission(playername, teamId, str)){ 
                if (str.equals("buyTeamBank")){
                    item = ItemBuilder.getItem(material, name, lore, headTexture, sqlManager.hasUnlockedTeamBank(teamId));
                }else {
                    item = ItemBuilder.getItem(material, name, lore, headTexture, false);
                }
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


    public Inventory createMemberInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.member");
        String teamID = sqlManager.getTeamNameFromPlayerName(playername);
        String teamPrefix = sqlManager.getTeamPrefix(teamID);
        String teamOwner = sqlManager.getTeamOwnerName(teamID);
        Integer teamMembersLelvel = sqlManager.getImprovLvlMembers(teamID);
        Integer teamMembers = sqlManager.getTeamMembers(teamID).size();
        Integer maxMembers = config.getInt("teamMaxMembers");
        String memberJoinDate = sqlManager.getJoinDate(playername);
        String memberRank = plugin.getRankFromId(sqlManager.getMemberRank(playername));
        String memberRankName = config.getString("ranks." + memberRank + ".name");
        String rankColor = config.getString("ranks." + memberRank + ".color");
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.member"));
        Double teamMoney = sqlManager.getTeamMoney(teamID);
        String teamDescription = sqlManager.getTeamDescription(teamID);

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.member").getKeys(false)){
            Integer slot = config.getInt("inventories.member."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.member."+str+".itemType"));
            String name = config.getString("inventories.member."+str+".itemName");
            List<String> lore = config.getStringList("inventories.member."+str+".lore");
            String headTexture = config.getString("inventories.member."+str+".headTexture");
            ItemStack item;

            if (name.equals(config.getString("inventories.member.seeTeamBank.itemName"))){
                lore = replaceInLore(lore, "%TEAM_MONEY%", DoubleToString(teamMoney));
                lore = replaceInLore(lore, "%MAX_MONEY%", DoubleToString(config.getDouble("teamMaxMoney")));
            }else if (name.equals(config.getString("inventories.member.teamInfos.itemName"))){
                lore = replaceInLore(lore, "%NAME%", teamID);
                lore = replaceInLore(lore, "%PREFIX%", colorsBuilder.replaceColor(teamPrefix));
                lore = replaceInLore(lore, "%OWNER%", teamOwner);
                lore = replaceInLore(lore, "%RANK%", rankColor + memberRankName);
                lore = replaceInLore(lore, "%DATE%", memberJoinDate);
                lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(teamMembers));
                lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+teamMembersLelvel));
                lore = replaceInLore(lore, "%DESCRIPTION%", colorsBuilder.replaceColor(teamDescription));
            }

            if (plugin.playerHasPermission(playername, teamID, str)){ 
                item = ItemBuilder.getItem(material, name, lore, headTexture, false);
            }else{
                item = ItemBuilder.getItem(
                    Material.getMaterial(config.getString("noPermission.itemType")), 
                    name, 
                    config.getStringList("noPermission.lore"),
                    config.getString("noPermission.headTexture"),
                    false
                );
            }

            if (name.equals(config.getString("inventories.member.seeTeamBank.itemName"))){
                if (!sqlManager.hasUnlockedTeamBank(teamID)){
                    item = ItemBuilder.getItem(
                        Material.getMaterial(config.getString("teamBankNotUnlock.itemType")),
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


    public Inventory createConfirmInventory() {
        Integer slots = config.getInt("inventoriesSlots.confirmInventory");
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.confirmInventory"));

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.confirmInventory").getKeys(false)){
            Material material = Material.getMaterial(config.getString("inventories.confirmInventory."+str+".itemType"));
            String name = config.getString("inventories.confirmInventory."+str+".itemName");
            List<String> lore = config.getStringList("inventories.confirmInventory."+str+".lore");
            String headTexture = config.getString("inventories.confirmInventory."+str+".headTexture");

            for(Integer slot : config.getIntegerList("inventories.confirmInventory."+str+".slots")){
                inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
            }
        }
        return inventory;
    }


    public Inventory createUpgradeTotalMembersInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.upgradeTotalMembers");
        String teamID = sqlManager.getTeamNameFromPlayerName(playername);
        Integer level = sqlManager.getImprovLvlMembers(teamID);
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.upgradeTotalMembers"));
        Material material;
        String headTexture;

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.upgradeTotalMembers").getKeys(false)){
            Integer slot = config.getInt("inventories.upgradeTotalMembers."+str+".slot");
            // Material material = Material.getMaterial(config.getString("inventories.upgradeTotalMembers."+str+".itemType"));
            String name = config.getString("inventories.upgradeTotalMembers."+str+".itemName");
            List<String> lore = config.getStringList("inventories.upgradeTotalMembers."+str+".lore");
            // String headTexture = config.getString("inventories.upgradeTotalMembers."+str+".headTexture");

            // if (level >= config.getInt("inventories.upgradeTotalMembers."+str+".level") && !str.equals("close")){
            //     inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, true));
            // }else{
            //     inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
            // }

            if (str.equals("close")){
                material = Material.getMaterial(config.getString("inventories.upgradeTotalMembers."+str+".itemType"));
                headTexture = config.getString("inventories.upgradeTotalMembers."+str+".headTexture");
            }else if (level >= config.getInt("inventories.upgradeTotalMembers."+str+".level")){
                material = Material.getMaterial(config.getString("inventories.upgradeTotalMembers."+str+".unlock.itemType"));
                headTexture = config.getString("inventories.upgradeTotalMembers."+str+".unlock.headTexture");
            }else{
                material = Material.getMaterial(config.getString("inventories.upgradeTotalMembers."+str+".lock.itemType"));
                headTexture = config.getString("inventories.upgradeTotalMembers."+str+".lock.headTexture");
            }
            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }
        return inventory;
    }


    public Inventory createUpgradeStorageInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.upgradeStorageAmount");
        String teamID = sqlManager.getTeamNameFromPlayerName(playername);
        Integer level = sqlManager.getTeamStorageLvl(teamID);
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.upgradeStorageAmount"));
        Material material;
        String headTexture;

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.upgradeStorageAmount").getKeys(false)){
            Integer slot = config.getInt("inventories.upgradeStorageAmount."+str+".slot");
            // Material material = Material.getMaterial(config.getString("inventories.upgradeStorageAmount."+str+".itemType"));
            String name = config.getString("inventories.upgradeStorageAmount."+str+".itemName");
            List<String> lore = config.getStringList("inventories.upgradeStorageAmount."+str+".lore");
            // String headTexture = config.getString("inventories.upgradeStorageAmount."+str+".headTexture");

            // if (level >= config.getInt("inventories.upgradeStorageAmount."+str+".level") && !str.equals("close")){
            //     inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, true));
            // }else{
            //     inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
            // }
            
            if (str.equals("close")){
                material = Material.getMaterial(config.getString("inventories.upgradeStorageAmount."+str+".itemType"));
                headTexture = config.getString("inventories.upgradeStorageAmount."+str+".headTexture");
            }else if (level >= config.getInt("inventories.upgradeStorageAmount."+str+".level")){
                material = Material.getMaterial(config.getString("inventories.upgradeStorageAmount."+str+".unlock.itemType"));
                headTexture = config.getString("inventories.upgradeStorageAmount."+str+".unlock.headTexture");
            }else{
                material = Material.getMaterial(config.getString("inventories.upgradeStorageAmount."+str+".lock.itemType"));
                headTexture = config.getString("inventories.upgradeStorageAmount."+str+".lock.headTexture");
            }

            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }
        return inventory;
    }


    public Inventory createMembersInventory(String playername, String inventoryName) {
        Integer slots = config.getInt("inventoriesSlots.teamMembers");
        String teamID = sqlManager.getTeamNameFromPlayerName(playername);
        Inventory inventory = Bukkit.createInventory(null, slots, inventoryName);

        emptyCases(inventory, slots, 0);
        Integer headSlot = 0;
        for(String str : sqlManager.getTeamMembers(teamID)){
            UUID playerUUID = sqliteManager.getUUID(str);
            String itemName;
            List<String> lore = new ArrayList<>();
            OfflinePlayer member;

            if (playerUUID == null) member = Bukkit.getOfflinePlayer(str);
            else member = Bukkit.getOfflinePlayer(playerUUID);

            String memberRank = plugin.getRankFromId(sqlManager.getMemberRank(str));
            String rankColor = config.getString("ranks." + memberRank + ".color");
            itemName = rankColor + str;
            
            lore.add(config.getString("prefixRankLore") + rankColor + config.getString("ranks." + memberRank + ".name"));
            inventory.setItem(headSlot, ItemBuilder.getPlayerHead(member, itemName, lore));
            headSlot ++;
        }

        for(String str : config.getConfigurationSection("inventories.teamMembers").getKeys(false)){
            Integer slot = config.getInt("inventories.teamMembers."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.teamMembers."+str+".itemType"));
            String name = config.getString("inventories.teamMembers."+str+".itemName");
            List<String> lore;
            String headTexture = config.getString("inventories.teamMembers."+str+".headTexture");

            if (sqlManager.isOwner(playername)) lore = config.getStringList("inventories.teamMembers."+str+".lore");
            else lore = Collections.emptyList();
            
            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }
        return inventory;
    }


    public Inventory createEditMembersInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.editMembers");
        String teamID = sqlManager.getTeamNameFromPlayerName(playername);
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.editMembers"));

        emptyCases(inventory, slots, 0);
        Integer headSlot = 0;
        for(String str : sqlManager.getTeamMembers(teamID)){
            UUID playerUUID = sqliteManager.getUUID(str);
            String itemName;
            List<String> lore = new ArrayList<>();
            OfflinePlayer member;
            ItemStack item;

            if (playerUUID == null) member = Bukkit.getOfflinePlayer(str);
            else member = Bukkit.getOfflinePlayer(playerUUID);

            Integer memberRank = sqlManager.getMemberRank(str);
            String memberRankName = plugin.getRankFromId(memberRank);
            String rankColor = config.getString("ranks." + memberRankName + ".color");
            itemName = rankColor + str;
            
            if (!sqlManager.isOwner(str)){
                lore = config.getStringList("editMembersLores");
                if (plugin.getLastRank() == memberRank) lore.remove(1);
            }

            lore.add(0, config.getString("prefixRankLore") + rankColor + config.getString("ranks." + memberRankName + ".name"));
            
            if (plugin.playerHasPermission(playername, teamID, "manageMembers")){ 
                item = ItemBuilder.getPlayerHead(member, itemName, lore);
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

        for(String str : config.getConfigurationSection("inventories.editMembers").getKeys(false)){
            Integer slot = config.getInt("inventories.editMembers."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.editMembers."+str+".itemType"));
            String name = config.getString("inventories.editMembers."+str+".itemName");
            String headTexture = config.getString("inventories.editMembers."+str+".headTexture");
            List<String> lore;
            ItemStack item;

            if (sqlManager.isOwner(playername)) lore = config.getStringList("inventories.editMembers."+str+".lore");
            else lore = Collections.emptyList();
            

            if (plugin.playerHasPermission(playername, teamID, str)){ 
                item = ItemBuilder.getItem(material, name, lore, headTexture, false);
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


    public Inventory createPermissionsInventory(String playerName) {
        Integer slots = config.getInt("inventoriesSlots.permissions");
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.permissions"));
        String teamId = sqlManager.getTeamNameFromPlayerName(playerName);

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.permissions").getKeys(false)){
            Integer slot = config.getInt("inventories.permissions."+str+".slot");
            // Material material = Material.getMaterial(config.getString("inventories.permissions."+str+".itemType"));
            String name = config.getString("inventories.permissions."+str+".itemName");
            List<String> lore = config.getStringList("inventories.permissions."+str+".lore");
            String headTexture;
            
            String rankPath = config.getString("inventories.permissions."+str+".rankPath");
            Integer defaultRankId = config.getInt("inventories."+rankPath+".rank");
            Integer permissionRank = sqlManager.getRankAssignement(teamId, str);
            String lorePrefix = config.getString("prefixRankLore");
            Material material;

            if (str.equals("close")){
                material = Material.getMaterial(config.getString("inventories.permissions."+str+".itemType"));
                headTexture = config.getString("inventories.permissions."+str+".headTexture");
            }else{
                material = Material.getMaterial(config.getString("ranks."+plugin.getRankFromId(permissionRank != null ? permissionRank : defaultRankId)+".itemType"));
                headTexture = config.getString("ranks."+plugin.getRankFromId(permissionRank != null ? permissionRank : defaultRankId)+".headTexture");
            }

            if (permissionRank != null){
                String rankColor = config.getString("ranks." + plugin.getRankFromId(permissionRank) + ".color");
                lore.add(0, lorePrefix + rankColor + config.getString("ranks." + plugin.getRankFromId(permissionRank) + ".name"));
            }else if (rankPath != null){
                String rankColor = config.getString("ranks." + plugin.getRankFromId(defaultRankId) + ".color");
                lore.add(0, lorePrefix + rankColor + config.getString("ranks." + plugin.getRankFromId(defaultRankId) + ".name"));
            }

            boolean isDefault = false;
            if (!str.equals("close") && (permissionRank == null || defaultRankId == permissionRank)){
                isDefault = true;
            }

            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, isDefault));
        }
        return inventory;
    }


    public Inventory createStorageDirectoryInventory(String playerName) {
        Integer slots = config.getInt("inventoriesSlots.storageDirectory");
        String teamId = sqlManager.getTeamNameFromPlayerName(playerName);
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.storageDirectory"));
        Integer level = sqlManager.getTeamStorageLvl(teamId);

        emptyCases(inventory, slots, 0);

        for(String str : config.getConfigurationSection("inventories.storageDirectory").getKeys(false)){
            Integer slot = config.getInt("inventories.storageDirectory."+str+".slot");
            String headTexture;
            Material material;
            String name;
            List<String> lore;

            if (level >= config.getInt("inventories.storageDirectory."+str+".level") || str.equals("close")){
                material = Material.getMaterial(config.getString("inventories.storageDirectory."+str+".itemType"));
                headTexture = config.getString("inventories.storageDirectory."+str+".headTexture");
                if (str.equals("close")){
                    name = config.getString("inventories.storageDirectory."+str+".itemName");
                }else{
                    name = config.getString(config.getString("inventories.storageDirectory."+str+".itemName"));
                }
                lore = config.getStringList("inventories.storageDirectory."+str+".lore");

                inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
            }else{
                material = Material.getMaterial(config.getString("storageNotUnlock.itemType"));
                name = config.getString("storageNotUnlock.itemName");
                lore = config.getStringList("storageNotUnlock.lore");
                headTexture = config.getString("storageNotUnlock.headTexture");

                inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
            }
        }
        return inventory;
    }


    public Inventory createStorageInventory(Team team, Integer storageId, String storageName){
        Integer slots = config.getInt("inventoriesSlots.storage");
        Inventory inventory;

        if (cacheManager.containsStorage(team, storageId)){
            inventory = cacheManager.getStorage(team, storageId).getStorageInstance();
        }else{
            inventory = Bukkit.createInventory(null, slots, storageName);
            // cacheManager.addStorage(
            //     new Storage(
            //         team,
            //         storageId,
            //         inventory,
            //         serializeManager.serializeToByte(inventory.getContents()))
            // );

            if (sqlManager.teamHasStorage(team.getTeamName(), storageId)){
                byte[] contentString = sqlManager.getStorageContent(team.getTeamName(), storageId);
                ItemStack[] content = serializeManager.deserializeFromByte(contentString);
                inventory.setContents(content);
            }
        }

        emptyCases(inventory, config.getIntegerList("inventories.storage.emptyCase.slots"));

        for(String str : config.getConfigurationSection("inventories.storage").getKeys(false)){
            Integer slot = config.getInt("inventories.storage."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.storage."+str+".itemType"));
            String name = config.getString("inventories.storage."+str+".itemName");
            List<String> lore = config.getStringList("inventories.storage."+str+".lore");
            String headTexture = config.getString("inventories.storage."+str+".headTexture");

            if (str.equals("previous")){
                if (storageId == plugin.getMinStorageId()) continue;
            }else if (str.equals("next")){
                if (storageId == sqlManager.getTeamStorageLvl(team.getTeamName())) continue;
            }else if (str.equals("emptyCase")){
                continue;
            }

            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }

        return inventory;
    }


    public Inventory createAlliancesInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.teamAlliances");
        String teamId = sqlManager.getTeamNameFromPlayerName(playername);
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.teamAlliances"));

        emptyCases(inventory, slots, 0);
        Integer headSlot = 0;
        for(String str : sqlManager.getAlliances(teamId)){
            String alliancePrefix = sqlManager.getTeamPrefix(str);
            String allianceOwnerName = sqlManager.getTeamOwnerName(str);
            String allianceDate = sqlManager.getAllianceDate(teamId, str);
            Integer teamMembersLelvel = sqlManager.getImprovLvlMembers(str);
            Integer teamMembers = sqlManager.getTeamMembers(str).size();
            Integer maxMembers = config.getInt("teamMaxMembers");
            String teamDescription = sqlManager.getTeamDescription(str);
            ArrayList<String> allianceMembers = sqlManager.getTeamMembers(str);
            UUID playerUUID = sqliteManager.getUUID(allianceOwnerName);
            String itemName = colorsBuilder.replaceColor(alliancePrefix);
            List<String> lore = config.getStringList("teamAllianceLore");
            OfflinePlayer allianceOwner;
            ItemStack item;

            if (playerUUID == null) allianceOwner = Bukkit.getOfflinePlayer(allianceOwnerName);
            else allianceOwner = Bukkit.getOfflinePlayer(playerUUID);
            
            lore = replaceInLore(lore, "%OWNER%", allianceOwnerName);
            lore = replaceInLore(lore, "%NAME%", str);
            lore = replaceInLore(lore, "%PREFIX%", colorsBuilder.replaceColor(alliancePrefix));
            lore = replaceInLore(lore, "%DATE%", allianceDate);
            lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(teamMembers));
            lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+teamMembersLelvel));
            lore = replaceInLore(lore, "%MEMBERS%", String.join(", ", allianceMembers));
            lore = replaceInLore(lore, "%DESCRIPTION%", colorsBuilder.replaceColor(teamDescription));
            
            
            if (plugin.playerHasPermission(playername, teamId, "seeTeamAlliances")){ 
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

        for(String str : config.getConfigurationSection("inventories.teamAlliances").getKeys(false)){
            Integer slot = config.getInt("inventories.teamAlliances."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.teamAlliances."+str+".itemType"));
            String name = config.getString("inventories.teamAlliances."+str+".itemName");
            String headTexture = config.getString("inventories.teamAlliances."+str+".headTexture");
            List<String> lore;

            if (sqlManager.isOwner(playername)) lore = config.getStringList("inventories.teamAlliances."+str+".lore");
            else lore = Collections.emptyList();
            
            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }
        return inventory;
    }


    public Inventory createEditAlliancesInventory(String playername) {
        Integer slots = config.getInt("inventoriesSlots.editAlliances");
        String teamId = sqlManager.getTeamNameFromPlayerName(playername);
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.editAlliances"));
        Integer maxMembers = config.getInt("teamMaxMembers");

        emptyCases(inventory, slots, 0);
        Integer headSlot = 0;
        for(String str : sqlManager.getAlliances(teamId)){
            String alliancePrefix = sqlManager.getTeamPrefix(str);
            String allianceOwnerName = sqlManager.getTeamOwnerName(str);
            String allianceDate = sqlManager.getAllianceDate(teamId, str);
            String teamDescription = sqlManager.getTeamDescription(str);
            Integer teamMembersLelvel = sqlManager.getImprovLvlMembers(str);
            Integer teamMembers = sqlManager.getTeamMembers(str).size();
            ArrayList<String> allianceMembers = sqlManager.getTeamMembers(str);
            UUID playerUUID = sqliteManager.getUUID(allianceOwnerName);
            String itemName = colorsBuilder.replaceColor(alliancePrefix);
            List<String> lore = config.getStringList("teamAllianceLore");
            OfflinePlayer allianceOwner;
            ItemStack item;


            if (playerUUID == null) allianceOwner = Bukkit.getOfflinePlayer(allianceOwnerName);
            else allianceOwner = Bukkit.getOfflinePlayer(playerUUID);

            
            lore = replaceInLore(lore, "%OWNER%", allianceOwnerName);
            lore = replaceInLore(lore, "%NAME%", str);
            lore = replaceInLore(lore, "%PREFIX%", colorsBuilder.replaceColor(alliancePrefix));
            lore = replaceInLore(lore, "%DATE%", allianceDate);
            lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(teamMembers));
            lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+teamMembersLelvel));
            lore = replaceInLore(lore, "%MEMBERS%", String.join(", ", allianceMembers));
            lore = replaceInLore(lore, "%DESCRIPTION%", colorsBuilder.replaceColor(teamDescription));
            
            
            if (plugin.playerHasPermission(playername, teamId, "seeAllliances")){ 
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
            List<String> lore;
            ItemStack item;

            if (sqlManager.isOwner(playername)) lore = config.getStringList("inventories.editAlliances."+str+".lore");
            else lore = Collections.emptyList();
            

            if (plugin.playerHasPermission(playername, teamId, str)){ 
                item = ItemBuilder.getItem(material, name, lore, headTexture, false);
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


    public Inventory createTeamListInventory(String playerName){
        ArrayList<Team> teams = sqlManager.getTeams();
        Integer slots = config.getInt("inventoriesSlots.teamList");
        List<Integer> headSlots = config.getIntegerList("inventories.teamList.teamOwnerHead.slots");
        Inventory inventory = Bukkit.createInventory(null, slots, config.getString("inventoriesName.teamList"));
        Integer maxMembers = config.getInt("teamMaxMembers");
        Page page;

        if (cacheManager.containsPage(playerName)){
            page = cacheManager.getPage(playerName);
        }else{
            page = new Page(playerName, headSlots.size(), teams);
            cacheManager.addPage(page);
        }
        ArrayList<List<Team>> pages = page.getPages();
        
        emptyCases(inventory, slots, 0);
        Integer slotIndex = 0;
        for(Team team : pages.get(page.getCurrentPage())){
            String teamOwnerName = team.getTeamOwnerName();
            String teamPrefix = team.getTeamPrefix();
            UUID playerUUID = sqliteManager.getUUID(teamOwnerName);
            String itemName = colorsBuilder.replaceColor(teamPrefix);
            List<String> lore = config.getStringList("teamListLore");
            OfflinePlayer teamOwner;
            ItemStack item;

            if (!sqliteManager.isRegistered(teamOwnerName)){
                sqliteManager.registerOfflinePlayer(Bukkit.getOfflinePlayer(teamOwnerName));
            }

            if (playerUUID == null) teamOwner = Bukkit.getOfflinePlayer(teamOwnerName);
            else teamOwner = Bukkit.getOfflinePlayer(playerUUID);
            
            lore = replaceInLore(lore, "%OWNER%", teamOwnerName);
            lore = replaceInLore(lore, "%NAME%", team.getTeamName());
            lore = replaceInLore(lore, "%PREFIX%", colorsBuilder.replaceColor(teamPrefix));
            lore = replaceInLore(lore, "%DATE%", team.getCreationDate());
            lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(team.getTeamMembersCount()));
            lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+team.getImprovLvlMembers()));
            lore = replaceInLore(lore, "%DESCRIPTION%", colorsBuilder.replaceColor(team.getTeamDescription()));
            
            item = ItemBuilder.getPlayerHead(teamOwner, itemName, lore);
            // item = ItemBuilder.getItem(Material.DIRT, teamOwnerName, lore, null, false);

            inventory.setItem(headSlots.get(slotIndex), item);
            slotIndex ++;
        }

        for(String str : config.getConfigurationSection("inventories.teamList").getKeys(false)){
            if (str.equals("teamOwnerHead")) continue;

            Integer slot = config.getInt("inventories.teamList."+str+".slot");
            Material material = Material.getMaterial(config.getString("inventories.teamList."+str+".itemType"));
            String name = config.getString("inventories.teamList."+str+".itemName");
            List<String> lore = config.getStringList("inventories.teamList."+str+".lore");
            String headTexture = config.getString("inventories.teamList."+str+".headTexture");

            if (str.equals("previous")){
                if (page.getCurrentPage() == 0) continue;
            }else if (str.equals("next")){
                if (page.getCurrentPage() == pages.size()-1) continue;
            }

            inventory.setItem(slot, ItemBuilder.getItem(material, name, lore, headTexture, false));
        }

        return inventory;
    }


    private List<String> replaceInLore(List<String> lore, String value, String replace){
        List<String> newLore = new ArrayList<>();
        for(String str : lore){
            newLore.add(str.replace(value, replace));
        }
        return newLore;
    }


    private String IntegerToString(int value){
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }


    private String DoubleToString(double value){
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }


	public void emptyCases(Inventory inventory, Integer slots, Integer start) {
		ItemStack item = new ItemStack(Material.getMaterial(config.getString("emptyCase")), 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(" ");
		item.setItemMeta(meta);

        for(int i = start; i < slots; i++) {
			inventory.setItem(i, item);
		}
	}

    public void emptyCases(Inventory inventory, List<Integer> list) {
		ItemStack item = new ItemStack(Material.getMaterial(config.getString("emptyCase")), 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(" ");
		item.setItemMeta(meta);

        for(int i : list) {
            inventory.setItem(i, item);
        }
	}
}