package fr.army.stelyteam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.army.stelyteam.chat.TeamChatLoader;
import fr.army.stelyteam.chat.TeamChatManager;
import fr.army.stelyteam.command.CommandManager;
import fr.army.stelyteam.external.ExternalManager;
import fr.army.stelyteam.listener.ListenerLoader;
import fr.army.stelyteam.menu.TeamMenu;
import fr.army.stelyteam.menu.impl.AdminMenu;
import fr.army.stelyteam.menu.impl.CreateTeamMenu;
import fr.army.stelyteam.menu.impl.MemberMenu;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.builder.ColorsBuilder;
import fr.army.stelyteam.utils.builder.conversation.ConversationBuilder;
import fr.army.stelyteam.utils.manager.CacheManager;
import fr.army.stelyteam.utils.manager.EconomyManager;
import fr.army.stelyteam.utils.manager.MessageManager;
import fr.army.stelyteam.utils.manager.database.DatabaseManager;
import fr.army.stelyteam.utils.manager.database.SQLiteDataManager;
import fr.army.stelyteam.utils.manager.serializer.ItemStackSerializer;
import org.jetbrains.annotations.NotNull;

public class StelyTeamPlugin extends JavaPlugin {

    private static boolean debug = false;

    private String currentServerName;
    private String[] serverNames;
    private String teamChatFormat;

    private static StelyTeamPlugin plugin;
    private YamlConfiguration config;
    private YamlConfiguration messages;
    private CacheManager cacheManager;
    private SQLiteDataManager sqliteManager;
    private EconomyManager economyManager;
    private TeamChatManager teamChatManager;
    private CommandManager commandManager;
    private MessageManager messageManager;
    private ColorsBuilder colorsBuilder;
    private ConversationBuilder conversationBuilder;
    private ItemStackSerializer serializeManager;
    private DatabaseManager databaseManager;
    private ExternalManager externalManager;


    @Override
    public void onEnable() {
        plugin = this;

        try {
            this.config = initFile("config.yml");
            this.messages = initFile("messages.yml");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.currentServerName = Bukkit.getServer().getMotd();
        this.serverNames = this.config.getStringList("serverNames").toArray(new String[0]);
        this.teamChatFormat = this.config.getString("teamChat.format");

        this.sqliteManager = new SQLiteDataManager(this);

        try {
            this.databaseManager = DatabaseManager.connect(this);
            this.sqliteManager.connect();
            this.sqliteManager.createTables();
            this.databaseManager.createTables();
            this.getLogger().info("SQL connectée au plugin !");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        
        this.serializeManager = new ItemStackSerializer();
        this.cacheManager = new CacheManager(this);
        this.economyManager = new EconomyManager(this);
        this.messageManager = new MessageManager(this);
        final TeamChatLoader teamChatLoader = new TeamChatLoader(this);
        this.teamChatManager = teamChatLoader.load();
        this.commandManager = new CommandManager(this);
        this.colorsBuilder = new ColorsBuilder(this);
        this.conversationBuilder = new ConversationBuilder(this);


        this.externalManager = new ExternalManager();
        this.externalManager.load();

        final ListenerLoader listenerLoader = new ListenerLoader();
        listenerLoader.registerListeners(this);

        
        getLogger().info("StelyTeam ON");
        cacheAllTeams();
    }


    @Override
    public void onDisable() {
        this.externalManager.unload();
        
        closeAllPlayerInventories();

        this.databaseManager.disconnect();
        this.sqliteManager.disconnect();
        getLogger().info("StelyTeam OFF");
    }


    public static StelyTeamPlugin getPlugin() {
        return plugin;
    }


    public YamlConfiguration initFile(@NotNull String fileName) throws FileNotFoundException {
        plugin.saveDefaultConfig();
        final File file = new File(plugin.getDataFolder(), fileName);
        Path parentDir = Paths.get(file.getParent());

        if (!Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new FileNotFoundException("Unable to create directories for path: " + parentDir);
            }
        }

        if (!file.exists()) {
            try {
                Files.copy(plugin.getResource(fileName), file.toPath());
            } catch (IOException ignored) {
                throw new FileNotFoundException("Unable to copy file from resources");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }


    private void closeAllPlayerInventories(){
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof TeamMenu){
                player.closeInventory();
            }
        }
    }


    private void cacheAllTeams(){
        for (Player player : Bukkit.getOnlinePlayers()){
            final Team team = Team.init(player);
            if (team == null) return;
            cacheManager.addTeam(team);
        }
    }


    public void openMainInventory(Player player, Team team){
        String playerName = player.getName();

        if (team == null){
            new CreateTeamMenu(player, null).openMenu();
        }else if(team.isTeamOwner(playerName)){
            new AdminMenu(player, null).openMenu();
        }else if (playerHasPermissionInSection(playerName, team, "manage")
            || playerHasPermissionInSection(playerName, team, "editMembers")
            || playerHasPermissionInSection(playerName, team, "editAlliances")
            || playerHasPermission(playerName, team, "teamList")){
            new AdminMenu(player, null).openMenu();
        }else{
            new MemberMenu(player, null).openMenu();
        }
    }


    public String getRankFromId(Integer value) {
        for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
            if (config.getInt("ranks." + key + ".id") == value) {
                return key;
            }
        }
        return null;
    }


    public String getStorageFromId(Integer storageId){
        for (String key : config.getConfigurationSection("inventories.storageDirectory").getKeys(false)) {
            if (config.getInt("inventories.storageDirectory." + key + ".storageId") == storageId) {
                return key;
            }
        }
        return null;
    }


    public Integer getLastRank(){
        Integer lastRank = 0;
        for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
            if (config.getInt("ranks." + key + ".id") > lastRank) {
                lastRank = config.getInt("ranks." + key + ".id");
            }
        }
        return lastRank;
    }


    public Integer getMinStorageId(){
        Integer minStorageId = Integer.MAX_VALUE;
        for (String key : config.getConfigurationSection("inventories.storageDirectory").getKeys(false)) {
            if (config.getInt("inventories.storageDirectory." + key + ".storageId") < minStorageId) {
                if (key.equals("close")) continue;
                minStorageId = config.getInt("inventories.storageDirectory." + key + ".storageId");
            }
        }
        return minStorageId;
    }


    public boolean playerHasPermission(String playerName, Team team, String permissionName){
        if (permissionName.equals("close")) return true;

        Integer permissionRank = team.getPermissionRank(permissionName);

        if (permissionRank != null){
            return permissionRank >= team.getMemberRank(playerName);
        }

        if (team.isTeamOwner(playerName) || config.getInt("inventories.permissions."+permissionName+".rank") == -1){
            return true;
        }else if (config.getInt("inventories.permissions."+permissionName+".rank") >= team.getMemberRank(playerName)){
            return true;
        }
        return false;
    }


    public boolean playerHasPermissionInSection(String playerName, Team team, String sectionName){
        if (sectionName.equals("close")) return true;
        for (String section : config.getConfigurationSection("inventories." + sectionName).getKeys(false)){
            if (playerHasPermission(playerName, team, section) && !section.equals("close")){
                return true;
            }
        }
        return false;
    }


    public Set<UUID> getAllowedPlayers(String permissionName){
        final Set<UUID> players = new HashSet<UUID>();
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.hasPermission(permissionName)){
                players.add(player.getUniqueId());
            }
        }
        return players;
    }


    public YamlConfiguration getConfig() {
        return config;
    }

    public YamlConfiguration getMessages() {
        return messages;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public SQLiteDataManager getSQLiteManager() {
        return sqliteManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ColorsBuilder getColorsBuilder() {
        return colorsBuilder;
    }

    public ConversationBuilder getConversationBuilder() {
        return conversationBuilder;
    }

    public ItemStackSerializer getSerializeManager() {
        return serializeManager;
    }

    public TeamChatManager getTeamChatManager() {
        return teamChatManager;
    }

    public String[] getServerNames(){
        return serverNames;
    }

    public String getCurrentServerName(){
        return currentServerName;
    }

    public String getTeamChatFormat(){
        return teamChatFormat;
    }


    public void toggleDebug(){
        debug = !debug;
    }

    public boolean isDebug() {
        return debug;
    }

}
