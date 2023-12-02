package fr.army.stelyteam.team;

import fr.army.stelyteam.cache.*;
import fr.army.stelyteam.entity.impl.MemberEntity;
import fr.army.stelyteam.entity.impl.TeamEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Team implements PropertiesHolder {

    /*
    private static StelyTeamPlugin plugin = StelyTeamPlugin.getPlugin();
    private YamlConfiguration config = plugin.getConfig();
    private static CacheManager cacheManager = StelyTeamPlugin.getPlugin().getCacheManager();*/

    private final UUID uuid;
    private final Property<String> name;
    private final Property<String> prefix;
    private final Property<String> description;
    private final Property<Date> creationDate;

    private final BankAccount bankAccount;

    private final Upgrades upgrades;

    private final Property<Member> owner;

    private final SetProperty<UUID, Member> members;
    private final SetProperty<String, Permission> permissions;
    private final SetProperty<UUID, Alliance> alliances;
    private final SetProperty<Integer, Storage> storages;

    public Team(@NotNull UUID uuid) {
        this.uuid = uuid;
        name = new Property<>(SaveField.NAME);
        prefix = new Property<>(SaveField.PREFIX); // null
        description = new Property<>(SaveField.DESCRIPTION); // config.getString("team.defaultDescription")
        creationDate = new Property<>(SaveField.CREATION_DATE); // getCurrentDate()
        owner = new Property<>(SaveField.OWNER);

        bankAccount = new BankAccount();
        upgrades = new Upgrades();

        members = new SetProperty<>(SaveField.MEMBERS, Member::getId);
        permissions = new SetProperty<>(SaveField.PERMISSION_NAME, Permission::getPermissionName);
        alliances = new SetProperty<>(SaveField.ALLIANCE_UUID, Alliance::getTeamUuid);
        storages = new SetProperty<>(SaveField.STORAGE_ID, Storage::getStorageId);
        // this.teamMembers = plugin.getDatabaseManager().getTeamMembers(uuid);
        // this.teamPermissions = plugin.getDatabaseManager().getTeamAssignement(uuid);
        // this.teamAlliances = plugin.getDatabaseManager().getTeamAlliances(uuid);
        // this.teamStorages = plugin.getDatabaseManager().getTeamStorages(uuid);
    }

    /*

    // New team
    public Team(String teamName, String teamOwnerName){
        this.uuid = UUID.randomUUID();
        this.name = teamName;
        this.teamOwnerName = teamOwnerName;
    }


    public void createTeam(){
        plugin.getDatabaseManager().insertTeam(this);
        cacheManager.addTeam(Team.init(uuid));
    }

    public static Team init(String teamName){
        return cacheManager.getTeamByName(teamName) == null 
            ? plugin.getDatabaseManager().getTeamFromTeamName(teamName) 
            : cacheManager.getTeamByName(teamName);
    }

    public static Team init(Player player){
        return cacheManager.getTeamByPlayerName(player.getName()) == null 
            ? plugin.getDatabaseManager().getTeamFromPlayerName(player.getName()) 
            : cacheManager.getTeamByPlayerName(player.getName());
    }

    public static Team initFromPlayerName(String playerName){
        return cacheManager.getTeamByPlayerName(playerName) == null 
            ? plugin.getDatabaseManager().getTeamFromPlayerName(playerName) 
            : cacheManager.getTeamByPlayerName(playerName);
    }

    public static Team initFromPlayerUuid(UUID playerUuid){
        return cacheManager.getTeamByPlayerUuid(playerUuid) == null 
            ? plugin.getDatabaseManager().getTeamFromPlayerName(plugin.getSQLiteManager().getPlayerName(playerUuid)) 
            : cacheManager.getTeamByPlayerUuid(playerUuid);
    }

    public static Team init(UUID teamUuid){
        return cacheManager.getTeamByUuid(teamUuid) == null 
            ? plugin.getDatabaseManager().getTeamFromTeamUuid(teamUuid) 
            : cacheManager.getTeamByUuid(teamUuid);
    }

    public static Team getFromCache(Player player){
        return cacheManager.getTeamByPlayerName(player.getName());
    }


    public boolean isTeamMember(String playerName){
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(playerName)) return true;
        }
        return false;
    }


    public boolean isTeamMember(UUID playerUuid){
        for (Member member : this.teamMembers) {
            if (member.getUuid().equals(playerUuid)) return true;
        }
        return false;
    }


    public boolean isTeamAlliance(UUID allianceUuid){
        for (Alliance alliance : this.teamAlliances) {
            if (alliance.getTeamUuid().equals(allianceUuid)) return true;
        }
        return false;
    }


    public boolean isTeamOwner(String playerName){
        if (this.teamOwnerName.equals(playerName)) return true;
        return false;
    }


    public void updateTeamName(String newTeamName){
        plugin.getDatabaseManager().updateTeamName(this.uuid, newTeamName);

        this.name = newTeamName;
    }


    public void updateTeamPrefix(String newPrefix){
        this.teamPrefix = newPrefix;
        plugin.getDatabaseManager().updateTeamPrefix(uuid, newPrefix);
    }


    public void updateTeamDescription(String newDescription){
        this.teamDescription = newDescription;
        plugin.getDatabaseManager().updateTeamDescription(uuid, newDescription);
    }


    public void updateTeamOwner(String newOwnerName){
        plugin.getDatabaseManager().updateTeamOwner(uuid, teamOwnerName, newOwnerName);
        this.teamOwnerName = newOwnerName;
    }


    public void unlockedTeamBank(){
        this.unlockedTeamBank = true;
        plugin.getDatabaseManager().updateUnlockedTeamBank(uuid);
    }


    public void insertMember(String playerName){
        this.teamMembers.add(
            new Member(
                playerName,
                plugin.getLastRank(),
                getCurrentDate(),
                StelyTeamPlugin.getPlugin().getSQLiteManager().getUUID(playerName)
            )
        );
        plugin.getDatabaseManager().insertMember(playerName, uuid);
    }


    public void insertAlliance(UUID allianceUuid){
        this.teamAlliances.add(new Alliance(allianceUuid, getCurrentDate()));
        plugin.getDatabaseManager().insertAlliance(uuid, allianceUuid);
    }


    public void removeMember(String playerName){
        this.teamMembers.removeIf(member -> member.getMemberName().equals(playerName));
        plugin.getDatabaseManager().removeMember(playerName, uuid);
    }


    public void removeAlliance(UUID allianceUuid){
        this.teamAlliances.removeIf(alliance -> alliance.getTeamUuid().equals(allianceUuid));
        plugin.getDatabaseManager().removeAlliance(uuid, allianceUuid);
    }


    public void removeTeam(){
        plugin.getDatabaseManager().removeTeam(uuid);
        plugin.getSQLiteManager().removeHome(uuid);
        cacheManager.removeTeam(this);
    }


    public void incrementTeamStorageLvl(){
        this.teamStorageLvl++;
        plugin.getDatabaseManager().incrementTeamStorageLvl(uuid);
    }


    public void incrementImprovLvlMembers(){
        this.improvLvlMembers++;
        plugin.getDatabaseManager().incrementImprovLvlMembers(uuid);
    }


    public void incrementAssignement(String permissionName){
        for (Permission permission : this.teamPermissions) {
            if (permission.getPermissionName().equals(permissionName)){
                permission.incrementTeamRank();
                plugin.getDatabaseManager().incrementAssignement(uuid, permissionName);
                return;
            }
        }
    }


    public void decrementImprovLvlMembers(){
        this.improvLvlMembers--;
        plugin.getDatabaseManager().decrementImprovLvlMembers(uuid);
    }


    public void decrementAssignement(String permissionName){
        for (Permission permission : this.teamPermissions) {
            if (permission.getPermissionName().equals(permissionName)){
                permission.decrementTeamRank();
                plugin.getDatabaseManager().decrementAssignement(uuid, permissionName);
                return;
            }
        }
    }


    public void insertAssignement(String permissionName, Integer teamRank){
        this.teamPermissions.add(new Permission(permissionName, teamRank));
        plugin.getDatabaseManager().insertAssignement(uuid, permissionName, teamRank);
    }


    public void incrementTeamMoney(Double amount){
        this.teamMoney += amount;
        plugin.getDatabaseManager().incrementTeamMoney(uuid, amount);
    }


    public void decrementTeamMoney(Double amount){
        this.teamMoney -= amount;
        plugin.getDatabaseManager().decrementTeamMoney(uuid, amount);
    }


    public void refreshTeamMembersInventory(String authorName) {
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(authorName)) continue;

            Player player = Bukkit.getPlayer(member.getMemberName());
            if (player != null) {
                if (!config.getConfigurationSection("inventoriesName").getValues(true).containsValue(player.getOpenInventory().getTitle())){
                    continue;
                }
                
                String openInventoryTitle = player.getOpenInventory().getTitle();
                if (openInventoryTitle.equals(config.getString("inventoriesName.admin"))){
                    new AdminMenu(player).openMenu();
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.manage"))){
                    new ManageMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.member"))){
                    new MemberMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.upgradeTotalMembers"))){
                    new UpgradeMembersMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.editMembers"))){
                    new EditMembersMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.teamMembers"))){
                    new MembersMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.permissions"))){
                    new PermissionsMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.storageDirectory"))){
                    new StorageDirectoryMenu(player).openMenu(this);
                }else if (openInventoryTitle.equals(config.getString("inventoriesName.editAlliances"))){
                    new EditAlliancesMenu(player).openMenu(this);
                }
            }
        }
    }


    public void closeTeamMembersInventory(String authorName) {
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(authorName)) continue;

            HumanEntity player = Bukkit.getPlayer(member.getMemberName());
            if (player != null) {
                if (!config.getConfigurationSection("inventoriesName").getValues(true).containsValue(player.getOpenInventory().getTitle())){
                    continue;
                }

                player.closeInventory();
            }
        }
    }


    public void teamBroadcast(String authorName, String message) {
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(authorName)) continue;

            HumanEntity player = Bukkit.getPlayer(member.getMemberName());
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }


    public ArrayList<String> getMembersName(){
        ArrayList<String> membersName = new ArrayList<String>();
        for (Member member : this.teamMembers) {
            membersName.add(member.getMemberName());
        }
        return membersName;
    }


    public Member getMember(String playerName){
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(playerName)) return member;
        }
        return null;
    }


    public String getMembershipDate(String playerName){
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(playerName)) return member.getJoinDate();
        }
        return null;
    }


    public Integer getPermissionRank(String permissionName){
        for (Permission permission : this.teamPermissions) {
            if (permission.getPermissionName().equals(permissionName)) return permission.getTeamRank();
        }
        return null;
    }


    public Integer getMemberRank(String playerName){
        for (Member member : this.teamMembers) {
            if (member.getMemberName().equals(playerName)) return member.getTeamRank();
        }
        return null;
    }


    public boolean hasStorage(int storageId){
        return this.teamStorages.containsKey(storageId);
    }


    public Storage getStorage(int storageId){
        return this.teamStorages.get(storageId);
    }

    public UUID getId() {
        return uuid;
    }

    public Property<String> getName() {
        return name;
    }

    public Property<String> getPrefix() {
        return prefix;
    }

    public Property<String> getDescription() {
        return description;
    }

    public Double getTeamMoney() {
        return teamMoney;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public Integer getImprovLvlMembers() {
        return improvLvlMembers;
    }

    public Integer getTeamStorageLvl() {
        return teamStorageLvl;
    }

    public boolean isUnlockedTeamBank() {
        return unlockedTeamBank;
    }

    public String getTeamOwnerName() {
        return teamOwnerName;
    }

    public Collection<Member> getTeamMembers() {
        return teamMembers;
    }

    public Set<Permission> getTeamPermissions() {
        return teamPermissions;
    }

    public Set<Alliance> getTeamAlliances() {
        return teamAlliances;
    }


    public void setTeamPrefix(String teamPrefix) {
        this.teamPrefix = teamPrefix;
    }

    private String getCurrentDate(){
        return new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
    }*/

    @NotNull
    public UUID getId() {
        return uuid;
    }

    @NotNull
    public Property<String> getName() {
        return name;
    }

    @NotNull
    public Property<String> getPrefix() {
        return prefix;
    }

    @NotNull
    public Property<String> getDescription() {
        return description;
    }

    @NotNull
    public Property<Date> getCreationDate() {
        return creationDate;
    }

    @NotNull
    public BankAccount getBankAccount() {
        return bankAccount;
    }

    @NotNull
    public Upgrades getUpgrades() {
        return upgrades;
    }

    @NotNull
    public SetProperty<UUID, Member> getMembers() {
        return members;
    }

    @NotNull
    public SetProperty<String, Permission> getPermissions() {
        return permissions;
    }

    @NotNull
    public SetProperty<UUID, Alliance> getAlliances() {
        return alliances;
    }

    public void loadUnsafe(@NotNull TeamSnapshot snapshot) {
        snapshot.name().ifPresent(name::loadUnsafe);
        snapshot.prefix().ifPresent(prefix::loadUnsafe);
        snapshot.description().ifPresent(description::loadUnsafe);
        snapshot.creationDate().ifPresent(creationDate::loadUnsafe);
        snapshot.bankAccount().ifPresent(bankAccount::loadUnsafe);
        snapshot.members().ifPresent(v -> members.loadUnsafe(v, Member::new));
    }

    public Team loadUnsafe(@NotNull TeamEntity teamEntity) {
        teamEntity.getName().ifPresent(name::loadUnsafe);
        teamEntity.getDisplayName().ifPresent(prefix::loadUnsafe);
        teamEntity.getDescription().ifPresent(description::loadUnsafe);
        teamEntity.getCreationDate().ifPresent(creationDate::loadUnsafe);
        teamEntity.getBankAccountEntity().ifPresent(bankAccount::loadUnsafe);

        Function<Collection<MemberEntity>, Collection<UUID>> extractMembersUuid = collection -> {
            return collection.stream()
                    .map(MemberEntity::getPlayerUuid)
                    .collect(Collectors.toList());
        };
        teamEntity.getMembersEntities().ifPresent(v -> members.loadUnsafe(extractMembersUuid.apply(v), Member::new));
        return this;
    }

    @Override
    public void save(@NotNull List<SaveProperty<?>> values) {
        name.save(this, values);
        prefix.save(this, values);
        description.save(this, values);
        creationDate.save(this, values);
        bankAccount.save(this, values);
        members.save(this, values, new Member[0]);
    }

    public void getProperties(final IProperty[] properties) {
        for (IProperty property : new IProperty[]{name, prefix, description, creationDate, owner}) {
            properties[property.getField().ordinal()] = property;
        }
        bankAccount.getProperties(properties);
    }

    @NotNull
    public List<SaveField> getNeedLoad(@NotNull SaveField... fields) {
        final List<SaveField> needLoad = new LinkedList<>();
        final IProperty[] properties = new IProperty[SaveField.values().length];
        getProperties(properties);
        for (SaveField field : fields) {
            if (!properties[field.ordinal()].isLoaded()) {
                needLoad.add(field);
            }
        }
        return needLoad;
    }


}