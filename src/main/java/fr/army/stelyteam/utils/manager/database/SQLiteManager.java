package fr.army.stelyteam.utils.manager.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.team.Alliance;
import fr.army.stelyteam.team.Member;
import fr.army.stelyteam.team.Permission;
import fr.army.stelyteam.team.Storage;
import fr.army.stelyteam.team.Team;

public class SQLiteManager extends DatabaseManager {
    private Connection connection;
    private StelyTeamPlugin plugin;
    private YamlConfiguration config;
    private String filename;

    public SQLiteManager(StelyTeamPlugin plugin) {
        super(plugin);
        this.config = plugin.getConfig();
        this.plugin = plugin;
        
        this.filename = this.config.getString("sqlite.file");
    }

    @Override
    public boolean isConnected() {
        return this.connection == null ? false : true;
    }

    @Override
    public void init() throws ClassNotFoundException, SQLException{
        if(!isConnected()){
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+"/"+this.filename);
        }
    }

    @Override
    public void disconnect() {
        if(isConnected()){
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }


    @Override
    public void createTables(){
        if (isConnected()){
            try {
                PreparedStatement queryPlayers = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'player' ('playerId' INTEGER, 'playerName' TEXT, 'teamRank' INTEGER, 'joinDate' TEXT, 'teamId' INTEGER, FOREIGN KEY('teamId') REFERENCES 'team'('teamId'), PRIMARY KEY('playerId' AUTOINCREMENT));");
                PreparedStatement queryTeams = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'team' ('teamId' INTEGER, 'teamUuid' TEXT UNIQUE, 'teamName' TEXT UNIQUE, 'teamPrefix' TEXT, 'teamDescription' TEXT, 'teamMoney' INTEGER, 'creationDate' TEXT, 'improvLvlMembers' INTEGER, 'teamStorageLvl' INTEGER, 'unlockedTeamBank' INTEGER, 'unlockedTeamClaim' INTEGER, 'teamOwnerPlayerId' INTEGER UNIQUE, PRIMARY KEY('teamId' AUTOINCREMENT), FOREIGN KEY('teamOwnerPlayerId') REFERENCES 'player'('playerId'));");
                PreparedStatement queryTeamStorages = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'teamStorage' ('storageId' INTEGER, 'teamId' INTEGER, 'storageContent' BLOB, 'openedServer' TEXT, PRIMARY KEY('storageId','teamId'), FOREIGN KEY('teamId') REFERENCES 'team'('teamId'), FOREIGN KEY('storageId') REFERENCES 'storage'('storageId'));");
                PreparedStatement queryAlliances = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'alliance' ('teamId' INTEGER, 'teamAllianceId' INTEGER, 'allianceDate' TEXT, FOREIGN KEY('teamId') REFERENCES 'team'('teamId'), FOREIGN KEY('teamAllianceId') REFERENCES 'team'('teamId'), PRIMARY KEY('teamId','teamAllianceId'));");
                PreparedStatement queryAssignements = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'assignement' ('teamId' INTEGER, 'permLabel' TEXT, 'teamRank' INTEGER, FOREIGN KEY('teamId') REFERENCES 'team'('teamId'), PRIMARY KEY('permLabel','teamId'));");
                PreparedStatement queryStorages = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'storage' ('storageId' INTEGER, PRIMARY KEY('storageId'));");

                PreparedStatement queryCreatePlayersUuid = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'players' ('playerId' INTEGER, 'uuid' TEXT, 'playerName' TEXT, PRIMARY KEY('playerId' AUTOINCREMENT));");

                queryPlayers.executeUpdate();
                queryTeams.executeUpdate();
                queryTeamStorages.executeUpdate();
                queryAlliances.executeUpdate();
                queryAssignements.executeUpdate();
                queryStorages.executeUpdate();

                queryCreatePlayersUuid.executeUpdate();

                queryPlayers.close();
                queryTeams.close();
                queryTeamStorages.close();
                queryAlliances.close();
                queryAssignements.close();
                queryStorages.close();

                queryCreatePlayersUuid.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isOwner(String playerName){
        if (isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT * FROM player WHERE playerName = ? AND teamRank = 0");
                query.setString(1, playerName);
                ResultSet result = query.executeQuery();
                boolean isOwner = result.next();
                query.close();
                return isOwner;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isMember(String playerName){
        if (isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT * FROM player WHERE playerName = ?");
                query.setString(1, playerName);
                ResultSet result = query.executeQuery();
                boolean isMember = result.next();
                query.close();
                return isMember;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean teamNameExists(String teamName){
        if (isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT * FROM team WHERE teamName = ?");
                query.setString(1, teamName);
                ResultSet result = query.executeQuery();
                boolean teamNameExists = result.next();
                query.close();
                return teamNameExists;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void insertTeam(Team team){
        if(isConnected()){
            try {
                PreparedStatement queryMember = connection.prepareStatement("INSERT INTO player (playerName, teamRank, joinDate) VALUES (?, ?, ?)");
                queryMember.setString(1, team.getTeamOwnerName());
                queryMember.setInt(2, 0);
                queryMember.setString(3, getCurrentDate());
                queryMember.executeUpdate();
                queryMember.close();
                
                PreparedStatement queryTeam = connection.prepareStatement("INSERT INTO team (teamUuid, teamName, teamPrefix, teamDescription, teamMoney, creationDate, improvLvlMembers, teamStorageLvl, unlockedTeamBank, teamOwnerPlayerId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                queryTeam.setString(1, team.getTeamUuid().toString());
                queryTeam.setString(2, team.getTeamName());
                queryTeam.setString(3, team.getTeamPrefix());
                queryTeam.setString(4, config.getString("teamDefaultDescription"));
                queryTeam.setInt(5, 0);
                queryTeam.setString(6, getCurrentDate());
                queryTeam.setInt(7, 0);
                queryTeam.setInt(8, 0);
                queryTeam.setInt(9, 0);
                queryTeam.setInt(10, getPlayerId(team.getTeamOwnerName()));
                queryTeam.executeUpdate();
                queryTeam.close();

                PreparedStatement queryUpdateMember = connection.prepareStatement("UPDATE player SET teamId = (SELECT teamId FROM team WHERE teamName = ?) WHERE playerName = ?");
                queryUpdateMember.setString(1, team.getTeamName());
                queryUpdateMember.setString(2, team.getTeamOwnerName());
                queryUpdateMember.executeUpdate();
                queryUpdateMember.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void insertMember(String playerName, UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO player (playerName, teamRank, joinDate, teamId) VALUES (?, ?, ?, ?)");
                query.setString(1, playerName);
                query.setInt(2, plugin.getLastRank());
                query.setString(3, getCurrentDate());
                query.setInt(4, getTeamId(teamUuid));
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeTeam(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement queryMembers = connection.prepareStatement("DELETE FROM player WHERE teamId = ?");
                queryMembers.setInt(1, getTeamId(teamUuid));
                queryMembers.executeUpdate();
                queryMembers.close();

                PreparedStatement queryPermissions = connection.prepareStatement("DELETE FROM assignement WHERE teamId = ?");
                queryPermissions.setInt(1, getTeamId(teamUuid));
                queryPermissions.executeUpdate();
                queryPermissions.close();

                PreparedStatement queryAlliances = connection.prepareStatement("DELETE FROM alliance WHERE teamId = ? OR teamAllianceId = ?");
                queryAlliances.setInt(1, getTeamId(teamUuid));
                queryAlliances.setInt(2, getTeamId(teamUuid));
                queryAlliances.executeUpdate();
                queryAlliances.close();

                PreparedStatement queryTeamStorage = connection.prepareStatement("DELETE FROM teamStorage WHERE teamId = ?");
                queryTeamStorage.setInt(1, getTeamId(teamUuid));
                queryTeamStorage.executeUpdate();
                queryTeamStorage.close();

                PreparedStatement queryTeams = connection.prepareStatement("DELETE FROM team WHERE teamId = ?");
                queryTeams.setInt(1, getTeamId(teamUuid));
                queryTeams.executeUpdate();
                queryTeams.close();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeMember(String playerName, UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("DELETE FROM player WHERE playerName = ? AND teamId = ?");
                query.setString(1, playerName);
                query.setInt(2, getTeamId(teamUuid));
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTeamName(UUID teamUuid, String newTeamName){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("UPDATE team SET teamName = ? WHERE teamUuid = ?");
                queryTeam.setString(1, newTeamName);
                queryTeam.setString(2, teamUuid.toString());
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTeamPrefix(UUID teamUuid, String newTeamPrefix){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("UPDATE team SET teamPrefix = ? WHERE teamUuid = ?");
                queryTeam.setString(1, newTeamPrefix);
                queryTeam.setString(2, teamUuid.toString());
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTeamDescription(UUID teamUuid, String newTeamDescription){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("UPDATE team SET teamDescription = ? WHERE teamUuid = ?");
                queryTeam.setString(1, newTeamDescription);
                queryTeam.setString(2, teamUuid.toString());
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTeamOwner(UUID teamUuid, String teamOwner, String newTeamOwner){
        if(isConnected()){
            try {
                PreparedStatement queryOwner = connection.prepareStatement("UPDATE player SET teamRank = ? WHERE teamId = (SELECT teamId FROM team WHERE teamUuid = ?) AND playerName = ?");
                queryOwner.setInt(1, 1);
                queryOwner.setString(2, teamUuid.toString());
                queryOwner.setString(3, teamOwner);
                queryOwner.executeUpdate();
                queryOwner.close();

                PreparedStatement queryNewOwner = connection.prepareStatement("UPDATE player SET teamRank = ? WHERE teamId = (SELECT teamId FROM team WHERE teamUuid = ?) AND playerName = ?");
                queryNewOwner.setInt(1, 0);
                queryNewOwner.setString(2, teamUuid.toString());
                queryNewOwner.setString(3, newTeamOwner);
                queryNewOwner.executeUpdate();
                queryNewOwner.close();

                PreparedStatement queryTeam = connection.prepareStatement("UPDATE team SET teamOwnerPlayerId = (SELECT playerId FROM player WHERE playerName = ?) WHERE teamUuid = ?");
                queryTeam.setString(1, newTeamOwner);
                queryTeam.setString(2, teamUuid.toString());
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void updateUnlockedTeamBank(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("UPDATE team SET unlockedTeamBank = ? WHERE teamUuid = ?");
                queryTeam.setInt(1, 1);
                queryTeam.setString(2, teamUuid.toString());
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateUnlockedTeamClaim(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("UPDATE team SET unlockedTeamClaim = ? WHERE teamUuid = ?");
                queryTeam.setInt(1, 1);
                queryTeam.setString(2, teamUuid.toString());
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void incrementImprovLvlMembers(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE team SET improvLvlMembers = improvLvlMembers + 1 WHERE teamUuid = ?");
                query.setString(1, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void incrementTeamStorageLvl(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE team SET teamStorageLvl = teamStorageLvl + 1 WHERE teamUuid = ?");
                query.setString(1, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void incrementTeamMoney(UUID teamUuid, double money){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE team SET teamMoney = teamMoney + ? WHERE teamUuid = ?");
                query.setDouble(1, money);
                query.setString(2, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void decrementImprovLvlMembers(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE team SET improvLvlMembers = improvLvlMembers - 1 WHERE teamUuid = ?");
                query.setString(1, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void decrementTeamMoney(UUID teamUuid, double money){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE team SET teamMoney = teamMoney - ? WHERE teamUuid = ?");
                query.setDouble(1, money);
                query.setString(2, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void promoteMember(String playerName){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE player SET teamRank = teamRank - 1 WHERE playerName = ?");
                query.setString(1, playerName);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void demoteMember(String playerName){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE player SET teamRank = teamRank + 1 WHERE playerName = ?");
                query.setString(1, playerName);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getTeamNameFromPlayerName(String playerName){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT t.teamName FROM team AS t INNER JOIN player AS p ON t.teamId = p.teamId WHERE p.playerName = ?");
                query.setString(1, playerName);
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return result.getString("teamName");
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public double getTeamMoney(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT teamMoney FROM team WHERE teamUuid = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return result.getDouble("teamMoney");
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public Set<String> getTeamMembersWithRank(UUID teamUuid, int rank){
        Set<String> teamMembers = new HashSet<>();
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT p.playerName FROM player AS p INNER JOIN team AS t ON p.teamId = t.teamId WHERE t.teamUuid = ? AND p.teamRank <= ? ORDER BY p.teamRank ASC");
                query.setString(1, teamUuid.toString());
                query.setInt(2, rank);
                ResultSet result = query.executeQuery();
                while(result.next()){
                    teamMembers.add(result.getString("playerName"));
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return teamMembers;
    }

    @Override
    public Set<String> getTeamsName(){
        Set<String> teamsName = new HashSet<>();
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT teamName FROM team");
                ResultSet result = query.executeQuery();
                while(result.next()){
                    teamsName.add(result.getString("teamName"));
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return teamsName;
    }

    @Override
    public Set<Team> getTeams(){
        Set<Team> teams = new HashSet<>();
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT * FROM team INNER JOIN player ON team.teamOwnerPlayerId = player.playerId");
                ResultSet result = query.executeQuery();
                while(result.next()){
                    teams.add(new Team(
                        UUID.fromString(result.getString("teamUuid")),
                        result.getString("teamName"),
                        result.getString("teamPrefix"),
                        result.getString("teamDescription"),
                        result.getInt("teamMoney"),
                        result.getString("creationDate"),
                        result.getInt("improvLvlMembers"),
                        result.getInt("teamStorageLvl"),
                        (1 == result.getInt("unlockedTeamBank")),
                        (1 == result.getInt("unlockedTeamClaim")),
                        result.getString("playerName")
                    ));
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return teams;
    }

    @Override
    public void insertAssignement(UUID teamUuid, String permLabel, Integer teamRank){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO assignement (teamId, permLabel, teamRank) VALUES (?, ?, ?)");
                query.setInt(1, getTeamId(teamUuid));
                query.setString(2, permLabel);
                query.setInt(3, teamRank);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void incrementAssignement(UUID teamUuid, String permLabel){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE assignement SET teamRank = teamRank + 1 WHERE teamId = ? AND permLabel = ?");
                query.setInt(1, getTeamId(teamUuid));
                query.setString(2, permLabel);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void decrementAssignement(UUID teamUuid, String permLabel){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE assignement SET teamRank = teamRank - 1 WHERE teamId = ? AND permLabel = ?");
                query.setInt(1, getTeamId(teamUuid));
                query.setString(2, permLabel);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void insertStorageId(int storageId){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO storage (storageId) VALUES (?)");
                query.setInt(1, storageId);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean storageIdExist(int storageId){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT storageId FROM storage WHERE storageId = ?");
                query.setInt(1, storageId);
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return true;
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean teamHasStorage(UUID teamUuid, Integer storageId){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT * FROM teamStorage AS ts INNER JOIN team AS t ON ts.teamId = t.teamId WHERE t.teamUuid = ? AND ts.storageId = ?");
                query.setString(1, teamUuid.toString());
                query.setInt(2, storageId);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public byte[] getStorageContent(UUID teamUuid, Integer storageId){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT storageContent FROM teamStorage AS ts INNER JOIN team AS t ON ts.teamId = t.teamId WHERE t.teamUuid = ? AND ts.storageId = ?");
                query.setString(1, teamUuid.toString());
                query.setInt(2, storageId);
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return result.getBytes("storageContent");
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void insertStorageContent(UUID teamUuid, Integer storageId, byte[] storageContent, String openedServer){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO teamStorage VALUES (?, ?, ?, ?)");
                query.setInt(1, storageId);
                query.setInt(2, getTeamId(teamUuid));
                query.setBytes(3, storageContent);
                // query.setString(4, openedServer);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateStorageContent(UUID teamUuid, Integer storageId, byte[] storageContent, String openedServer){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE teamStorage SET storageContent = ?, openedServer = ? WHERE teamId = ? AND storageId = ?");
                query.setBytes(1, storageContent);
                query.setString(2, openedServer);
                query.setInt(3, getTeamId(teamUuid));
                query.setInt(4, storageId);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveStorage(Storage storage){
        UUID teamUuid = storage.getTeamUuid();
        int storageId = storage.getStorageId();
        byte[] storageContent = storage.getStorageContent();
        String openedServerName = storage.getOpenedServerName();
        if (!teamHasStorage(teamUuid, storageId)){
            if (!storageIdExist(storageId)){
                insertStorageId(storageId);
            }
            insertStorageContent(teamUuid, storageId, storageContent, null);
        }else{
            updateStorageContent(teamUuid, storageId, storageContent, null);
        }
    }

    @Override
    public void insertAlliance(UUID teamUuid, UUID allianceUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO alliance VALUES (?, ?, ?)");
                query.setInt(1, getTeamId(teamUuid));
                query.setInt(2, getTeamId(allianceUuid));
                query.setString(3, getCurrentDate());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeAlliance(UUID teamUuid, UUID allianceUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("DELETE FROM alliance WHERE (teamId = (SELECT t.teamId FROM team t WHERE t.teamUuid = ?) AND teamAllianceId = (SELECT t.teamId FROM team t WHERE t.teamUuid = ?)) OR (teamId = (SELECT t.teamId FROM team t WHERE t.teamUuid = ?) AND teamAllianceId = (SELECT t.teamId FROM team t WHERE t.teamUuid = ?))");
                query.setString(1, teamUuid.toString());
                query.setString(2, allianceUuid.toString());
                query.setString(3, allianceUuid.toString());
                query.setString(4, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isAlliance(UUID teamUuid, UUID allianceUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT * FROM alliance AS a INNER JOIN team AS t ON a.teamId = t.teamId WHERE (t.teamUuid = ? OR t.teamUuid = ?) AND (a.teamAllianceId = ? OR a.teamId = ?)");
                query.setString(1, teamUuid.toString());
                query.setString(2, allianceUuid.toString());
                query.setInt(3, getTeamId(allianceUuid));
                query.setInt(4, getTeamId(allianceUuid));
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return true;
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public Set<String> getMembers(){
        Set<String> members = new HashSet<>();
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playerName FROM player");
                ResultSet result = query.executeQuery();
                while(result.next()){
                    members.add(result.getString("playerName"));
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return members;
    }

    @Override
    public Team getTeamFromPlayerName(String playerName){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT t.teamUuid, t.teamName, t.teamPrefix, t.teamDescription, t.teamMoney, t.creationDate, t.improvLvlMembers, t.teamStorageLvl, t.unlockedTeamBank, t.unlockedTeamClaim, o.playerName AS 'ownerName' FROM team AS t INNER JOIN player p ON t.teamId = p.teamId INNER JOIN player o ON t.teamOwnerPlayerId = o.playerId WHERE p.playerName = ?");
                query.setString(1, playerName);
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return new Team(
                        UUID.fromString(result.getString("teamUuid")),
                        result.getString("teamName"),
                        result.getString("teamPrefix"),
                        result.getString("teamDescription"),
                        result.getInt("teamMoney"),
                        result.getString("creationDate"),
                        result.getInt("improvLvlMembers"),
                        result.getInt("teamStorageLvl"),
                        (1 == result.getInt("unlockedTeamBank")),
                        (1 == result.getInt("unlockedTeamClaim")),
                        result.getString("ownerName")
                    );
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Team getTeamFromTeamName(String teamName){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT t.teamUuid, t.teamName, t.teamPrefix, t.teamDescription, t.teamMoney, t.creationDate, t.improvLvlMembers, t.teamStorageLvl, t.unlockedTeamBank, t.unlockedTeamClaim, o.playerName AS 'ownerName' FROM team AS t INNER JOIN player o ON t.teamOwnerPlayerId = o.playerId WHERE t.teamName = ?");
                query.setString(1, teamName);
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return new Team(
                        UUID.fromString(result.getString("teamUuid")),
                        result.getString("teamName"),
                        result.getString("teamPrefix"),
                        result.getString("teamDescription"),
                        result.getInt("teamMoney"),
                        result.getString("creationDate"),
                        result.getInt("improvLvlMembers"),
                        result.getInt("teamStorageLvl"),
                        (1 == result.getInt("unlockedTeamBank")),
                        (1 == result.getInt("unlockedTeamClaim")),
                        result.getString("ownerName")
                    );
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<Member> getTeamMembers(UUID teamUuid){
        // List<Member> teamMembers = Collections.synchronizedList(new ArrayList<>());
        List<Member> teamMembers = new ArrayList<>();
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT p.playerName, p.teamRank, p.joinDate FROM player AS p INNER JOIN team AS t ON p.teamId = t.teamId WHERE t.teamUuid = ? ORDER BY p.teamRank ASC, p.playerName ASC;");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                while(result.next()){
                    try{
                        teamMembers.add(
                            // new Member(
                            //     result.getString("playerName"),
                            //     result.getInt("teamRank"),
                            //     result.getString("joinDate"),
                            //     StelyTeamPlugin.getPlugin().getSQLiteManager().getUUID(result.getString("playerName"))
                            // )
                            new Member(
                                result.getString("playerName"),
                                result.getInt("teamRank"),
                                result.getString("joinDate"),
                                getUUID(result.getString("playerName"))
                            )
                        );
                    }catch(Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return teamMembers;
    }

    @Override
    public Set<Permission> getTeamAssignement(UUID teamUuid){
        Set<Permission> teamAssignement = Collections.synchronizedSet(new HashSet<>());
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT a.permLabel, a.teamRank FROM assignement AS a INNER JOIN team AS t ON a.teamId = t.teamId WHERE t.teamUuid = ?;");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                while(result.next()){
                    teamAssignement.add(
                        new Permission(
                            result.getString("permLabel"),
                            result.getInt("teamRank")
                        )
                    );
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return teamAssignement;
    }

    @Override
    public List<Alliance> getTeamAlliances(UUID teamUuid){
        List<Alliance> alliances = Collections.synchronizedList(new ArrayList<>());
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("SELECT t.teamUuid, a.allianceDate FROM team AS t INNER JOIN alliance AS a ON t.teamId = a.teamAllianceId WHERE a.teamId = ? UNION SELECT t.teamUuid, a.allianceDate FROM team AS t INNER JOIN alliance AS a ON t.teamId = a.teamId WHERE a.teamAllianceId = ?");
                queryTeam.setInt(1, getTeamId(teamUuid));
                queryTeam.setInt(2, getTeamId(teamUuid));
                ResultSet resultTeam = queryTeam.executeQuery();
                while(resultTeam.next()){
                    alliances.add(
                        new Alliance(
                            UUID.fromString(resultTeam.getString("teamUuid")),
                            resultTeam.getString("allianceDate")
                        )
                    );
                }
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return alliances;
    }

    @Override
    public Map<Integer, Storage> getTeamStorages(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT ts.storageId, ts.storageContent, ts.openedServer FROM teamStorage AS ts INNER JOIN team AS t ON ts.teamId = t.teamId WHERE t.teamUuid = ?;");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                Map<Integer, Storage> teamStorage = new HashMap<>();
                while(result.next()){
                    teamStorage.put(
                        result.getInt("storageId"),
                        new Storage(
                            teamUuid,
                            result.getInt("storageId"),
                            null,
                            result.getBytes("storageContent"),
                            null
                        )
                    );
                }
                query.close();
                return teamStorage;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getPlayerId(String playerName){
        if (isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playerId FROM player WHERE playerName = ?");
                query.setString(1, playerName);
                ResultSet result = query.executeQuery();
                int playerId = 0;
                if(result.next()){
                    playerId = result.getInt("playerId");
                }
                query.close();
                return playerId;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getTeamId(UUID teamUuid){
        if (isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT teamId FROM team WHERE teamUuid = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                int teamId = 0;
                if(result.next()){
                    teamId = result.getInt("teamId");
                }
                query.close();
                return teamId;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public Team getTeamFromTeamUuid(UUID teamUuid) {
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT t.teamUuid, t.teamName, t.teamPrefix, t.teamDescription, t.teamMoney, t.creationDate, t.improvLvlMembers, t.teamStorageLvl, t.unlockedTeamBank, t.unlockedTeamClaim, o.playerName AS 'ownerName' FROM team AS t INNER JOIN player o ON t.teamOwnerPlayerId = o.playerId WHERE t.teamUuid = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                if(result.next()){
                    return new Team(
                        UUID.fromString(result.getString("teamUuid")),
                        result.getString("teamName"),
                        result.getString("teamPrefix"),
                        result.getString("teamDescription"),
                        result.getInt("teamMoney"),
                        result.getString("creationDate"),
                        result.getInt("improvLvlMembers"),
                        result.getInt("teamStorageLvl"),
                        (1 == result.getInt("unlockedTeamBank")),
                        (1 == result.getInt("unlockedTeamClaim")),
                        result.getString("ownerName")
                    );
                }
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    @Override
    public String getCurrentDate(){
        return new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
    }







    @Override
    public void registerPlayer(Player player){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO players (uuid, playerName) VALUES (?, ?)");
                query.setString(1, player.getUniqueId().toString());
                query.setString(2, player.getName());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void registerPlayer(OfflinePlayer player){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO players (uuid, playerName) VALUES (?, ?)");
                query.setString(1, player.getUniqueId().toString());
                query.setString(2, player.getName());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean isRegistered(String playerName){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playerName FROM players WHERE playerName = ?");
                query.setString(1, playerName);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Override
    public UUID getUUID(String playerName){
        try {
            return queryUUID(playerName);
        } catch (Exception e) {
            // System.out.println(e);
            return UUID.randomUUID();
        }
    }


    // Should be not null
    @NotNull
    private UUID queryUUID(String playerName) {
        if (!isConnected()) {
            throw new IllegalStateException("Can not use the this database while the connection is not established");
        }
        try {
            PreparedStatement query = connection.prepareStatement("SELECT uuid FROM players WHERE playerName = ?");
            query.setString(1, playerName);
            ResultSet result = query.executeQuery();
            boolean isParticipant = result.next();
            if (!isParticipant) {
                throw new RuntimeException("The uuid of '" + playerName + "' is not stored in the database");
            }
            final UUID uuid = UUID.fromString(result.getString("uuid"));
            query.close();

            // if (uuid != null)
            //     System.out.println(uuid.toString());
            // else
            //     System.out.println("null");

            return uuid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPlayerName(UUID uuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playerName FROM players WHERE uuid = ?");
                query.setString(1, uuid.toString());
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                String playerName = null;
                if(isParticipant){
                    playerName = result.getString("playerName");
                }
                query.close();
                return playerName;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}