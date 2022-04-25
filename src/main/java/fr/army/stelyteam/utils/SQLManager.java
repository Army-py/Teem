package fr.army.stelyteam.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import fr.army.stelyteam.StelyTeamPlugin;

public class SQLManager {
    
    private String host;
    private String database;
    private String user;
    private String password;
    private int port;

    private Connection connection;

    public SQLManager() {
        // this.host = App.config.getString("sql.host");
        // this.database = App.config.getString("sql.database");
        // this.user = App.config.getString("sql.user");
        // this.password = App.config.getString("sql.password");
        // this.port = App.config.getInt("sql.port");

        this.database = "bungeecord_StelyTeam.db";
    }


    public boolean isConnected() {
        return connection == null ? false : true;
    }


    public void connect() throws ClassNotFoundException, SQLException{
        if(!isConnected()){
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+ StelyTeamPlugin.instance.getDataFolder().getAbsolutePath()+"/"+this.database);
        }
    }


    public void disconnect() {
        if(isConnected()){
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public Connection getConnection() {
        return connection;
    }


    public void disconnect(Connection connection) {
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isOwner(String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT owner FROM teams WHERE owner = ?");
                query.setString(1, playername);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean isMember(String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playername FROM players WHERE playername = ? AND rank = ?");
                query.setString(1, playername);
                query.setInt(2, 0);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean isMemberInTeam(String playername, String team_id){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playername FROM players WHERE playername = ? AND team_id = ?");
                query.setString(1, playername);
                query.setString(2, team_id);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean isAdmin(String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playername FROM players WHERE playername = ? AND rank = ?");
                query.setString(1, playername);
                query.setInt(2, 1);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean hasUnlockedTeamBank(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT team_id FROM teams WHERE team_id = ? AND team_bank = 1");
                query.setString(1, teamID);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean teamIdExist(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT team_id FROM teams WHERE team_id = ?");
                query.setString(1, teamID);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean teamPrefixExist(String teamPrefix){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT team_prefix FROM teams WHERE team_prefix = ?");
                query.setString(1, teamPrefix);
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                query.close();
                return isParticipant;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public void insertTeam(String teamID, String teamPrefix, String owner){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("INSERT INTO teams (team_id, team_prefix, owner, money, creation_date, members_level, team_bank) VALUES (?, ?, ?, ?, ?, ?, ?)");
                queryTeam.setString(1, teamID);
                queryTeam.setString(2, teamPrefix);
                queryTeam.setString(3, owner);
                queryTeam.setInt(4, 0);
                queryTeam.setString(5, new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime()));
                queryTeam.setInt(6, 0);
                queryTeam.setInt(7, 0);
                queryTeam.executeUpdate();
                queryTeam.close();

                PreparedStatement queryMember = connection.prepareStatement("INSERT INTO players (playername, rank, team_id) VALUES (?, ?, ?)");
                queryMember.setString(1, owner);
                queryMember.setInt(2, 2);
                queryMember.setString(3, teamID);
                queryMember.executeUpdate();
                queryMember.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void insertMember(String playername, String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("INSERT INTO players (playername, rank, team_id) VALUES (?, ?, ?)");
                query.setString(1, playername);
                query.setInt(2, 0);
                query.setString(3, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void removeTeam(String teamID, String owner){
        if(isConnected()){
            try {
                PreparedStatement queryTeams = connection.prepareStatement("DELETE FROM teams WHERE team_id = ? AND owner = ?");
                queryTeams.setString(1, teamID);
                queryTeams.setString(2, owner);
                queryTeams.executeUpdate();
                queryTeams.close();

                PreparedStatement queryMembers = connection.prepareStatement("DELETE FROM players WHERE team_id = ?");
                queryMembers.setString(1, teamID);
                queryMembers.executeUpdate();
                queryMembers.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void removeMember(String playername, String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("DELETE FROM players WHERE playername = ? AND team_id = ?");
                query.setString(1, playername);
                query.setString(2, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateTeamID(String teamID, String newTeamID, String owner){
        if(isConnected()){
            try {
                PreparedStatement queryTeam = connection.prepareStatement("UPDATE teams SET team_id = ? WHERE team_id = ? AND owner = ?");
                queryTeam.setString(1, newTeamID);
                queryTeam.setString(2, teamID);
                queryTeam.setString(3, owner);
                queryTeam.executeUpdate();
                queryTeam.close();

                PreparedStatement queryMember = connection.prepareStatement("UPDATE players SET team_id = ? WHERE team_id = ?");
                queryMember.setString(1, newTeamID);
                queryMember.setString(2, teamID);
                queryMember.executeUpdate();
                queryMember.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateTeamPrefix(String teamID, String newTeamPrefix, String owner){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE teams SET team_prefix = ? WHERE team_id = ? AND owner = ?");
                query.setString(1, newTeamPrefix);
                query.setString(2, teamID);
                query.setString(3, owner);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateTeamOwner(String teamID, String newOwner, String owner){
        if(isConnected()){
            try {
                PreparedStatement queryOwner = connection.prepareStatement("UPDATE players SET rank = ? WHERE team_id = ? AND playername = ?");
                queryOwner.setInt(1, 1);
                queryOwner.setString(2, teamID);
                queryOwner.setString(3, owner);
                queryOwner.executeUpdate();
                queryOwner.close();

                PreparedStatement queryNewOwner = connection.prepareStatement("UPDATE players SET rank = ? WHERE team_id = ? AND playername = ?");
                queryNewOwner.setInt(1, 2);
                queryNewOwner.setString(2, teamID);
                queryNewOwner.setString(3, newOwner);
                queryNewOwner.executeUpdate();
                queryNewOwner.close();

                PreparedStatement queryTeam = connection.prepareStatement("UPDATE teams SET owner = ? WHERE team_id = ? AND owner = ?");
                queryTeam.setString(1, newOwner);
                queryTeam.setString(2, teamID);
                queryTeam.setString(3, owner);
                queryTeam.executeUpdate();
                queryTeam.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateUnlockTeamBank(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE teams SET team_bank = ? WHERE team_id = ?");
                query.setInt(1, 1);
                query.setString(2, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void incrementTeamLevel(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE teams SET members_level = members_level + 1 WHERE team_id = ?");
                query.setString(1, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void incrementTeamMoney(String teamID, int money){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE teams SET money = money + ? WHERE team_id = ?");
                query.setInt(1, money);
                query.setString(2, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void decrementTeamMoney(String teamID, int money){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE teams SET money = money - ? WHERE team_id = ?");
                query.setInt(1, money);
                query.setString(2, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void promoteToAdmin(String teamID, String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE players SET rank = ? WHERE team_id = ? AND playername = ?");
                query.setInt(1, 1);
                query.setString(2, teamID);
                query.setString(3, playername);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void demoteToMember(String teamID, String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE players SET rank = ? WHERE team_id = ? AND playername = ?");
                query.setInt(1, 0);
                query.setString(2, teamID);
                query.setString(3, playername);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void removeTeamAdmin(String teamID, String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE players SET rank = ? WHERE team_id = ?");
                query.setString(1, teamID);
                query.setInt(2, 0);
                query.setString(3, teamID);
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public String getTeamIDFromOwner(String owner){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT team_id FROM teams WHERE owner = ?");
                query.setString(1, owner);
                ResultSet result = query.executeQuery();
                String teamID = null;
                if(result.next()){
                    teamID = result.getString("team_id");
                }
                query.close();
                return teamID;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public String getTeamIDFromPlayer(String playername){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT team_id FROM players WHERE playername = ?");
                query.setString(1, playername);
                ResultSet result = query.executeQuery();
                String teamID = null;
                if(result.next()){
                    teamID = result.getString("team_id");
                }
                query.close();
                return teamID;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Integer getTeamLevel(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT members_level FROM teams WHERE team_id = ?");
                query.setString(1, teamID);
                ResultSet result = query.executeQuery();
                Integer level = null;
                if(result.next()){
                    level = result.getInt("members_level");
                }
                query.close();
                return level;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Integer getTeamMoney(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT money FROM teams WHERE team_id = ?");
                query.setString(1, teamID);
                ResultSet result = query.executeQuery();
                Integer level = null;
                if(result.next()){
                    level = result.getInt("money");
                }
                query.close();
                return level;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public ArrayList<String> getMembers(String teamID){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT playername FROM players WHERE team_id = ? ORDER BY rank DESC");
                query.setString(1, teamID);

                ResultSet result = query.executeQuery();
                ArrayList<String> data = new ArrayList<String>();
                while(result.next()){
                    data.add(result.getString("playername"));
                }
                query.close();
                return data;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}