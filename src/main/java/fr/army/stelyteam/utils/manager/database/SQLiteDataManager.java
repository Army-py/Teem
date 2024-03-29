package fr.army.stelyteam.utils.manager.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import fr.army.stelyteam.StelyTeamPlugin;

public class SQLiteDataManager {

    private String database;
    private Connection connection;
    private StelyTeamPlugin plugin;

    public SQLiteDataManager(StelyTeamPlugin plugin) {
        this.database = "data.db";
        
        this.plugin = plugin;
    }


    public boolean isConnected() {
        return connection == null ? false : true;
    }


    public void connect() throws ClassNotFoundException, SQLException{
        if(!isConnected()){
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+ plugin.getDataFolder().getAbsolutePath()+"/"+this.database);
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


    public void disconnect(Connection connection) {
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void createTables(){
        if (isConnected()){
            try {
                PreparedStatement queryHomes = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'homes' ('id' INTEGER, 'team_id' TEXT, world TEXT, 'x' REAL, 'y' REAL, 'z' REAL, 'yaw' REAL, PRIMARY KEY('id' AUTOINCREMENT));");
                queryHomes.executeUpdate();
                queryHomes.close();

                // PreparedStatement queryPlayers = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'players' ('uuid' TEXT, 'playername' TEXT, PRIMARY KEY('uuid'));");
                // queryPlayers.executeUpdate();
                // queryPlayers.close();

                PreparedStatement dropInventoryDebug = connection.prepareStatement("DROP TABLE IF EXISTS 'inventoryDebug';");
                dropInventoryDebug.executeUpdate();
                dropInventoryDebug.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public boolean isSet(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT team_id FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
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


    public void addHome(UUID teamUuid, String world, double x, double y, double z, double yaw){
        if(isConnected()){
            try {
                PreparedStatement queryPlayers = connection.prepareStatement("INSERT INTO homes VALUES (null, ?, ?, ?, ?, ?, ?)");
                queryPlayers.setString(1, teamUuid.toString());
                queryPlayers.setString(2, world);
                queryPlayers.setDouble(3, x);
                queryPlayers.setDouble(4, y);
                queryPlayers.setDouble(5, z);
                queryPlayers.setDouble(6, yaw);
                queryPlayers.executeUpdate();
                queryPlayers.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateHome(UUID teamUuid, String world, double x, double y, double z, double yaw){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("UPDATE homes SET world = ?, x = ?, y = ?, z = ?, yaw = ? WHERE team_id = ?");
                query.setString(1, world);
                query.setDouble(2, x);
                query.setDouble(3, y);
                query.setDouble(4, z);
                query.setDouble(5, yaw);
                query.setString(6, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void removeHome(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("DELETE FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
                query.executeUpdate();
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public String getWorld(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT world FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                if(isParticipant){
                    String world = result.getString("world");
                    query.close();
                    return world;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public double getX(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT x FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                if(isParticipant){
                    double x = result.getDouble("x");
                    query.close();
                    return x;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    public double getY(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT y FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                if(isParticipant){
                    double y = result.getDouble("y");
                    query.close();
                    return y;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    public double getZ(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT z FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                if(isParticipant){
                    double z = result.getDouble("z");
                    query.close();
                    return z;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    public double getYaw(UUID teamUuid){
        if(isConnected()){
            try {
                PreparedStatement query = connection.prepareStatement("SELECT yaw FROM homes WHERE team_id = ?");
                query.setString(1, teamUuid.toString());
                ResultSet result = query.executeQuery();
                boolean isParticipant = result.next();
                if(isParticipant){
                    double yaw = result.getDouble("yaw");
                    query.close();
                    return yaw;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    // public void registerPlayer(Player player){
    //     if(isConnected()){
    //         try {
    //             PreparedStatement query = connection.prepareStatement("INSERT INTO players VALUES (?, ?)");
    //             query.setString(1, player.getUniqueId().toString());
    //             query.setString(2, player.getName());
    //             query.executeUpdate();
    //             query.close();
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     }
    // }


    // public void registerPlayer(OfflinePlayer player){
    //     if(isConnected()){
    //         try {
    //             PreparedStatement query = connection.prepareStatement("INSERT INTO players VALUES (?, ?)");
    //             query.setString(1, player.getUniqueId().toString());
    //             query.setString(2, player.getName());
    //             query.executeUpdate();
    //             query.close();
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     }
    // }


    // public boolean isRegistered(String playername){
    //     if(isConnected()){
    //         try {
    //             PreparedStatement query = connection.prepareStatement("SELECT playername FROM players WHERE playername = ?");
    //             query.setString(1, playername);
    //             ResultSet result = query.executeQuery();
    //             boolean isParticipant = result.next();
    //             query.close();
    //             return isParticipant;
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     return false;
    // }


    // // Should be not null
    // @NotNull
    // public UUID getUUID(String playername) {
    //     if (!isConnected()) {
    //         throw new IllegalStateException("Can not use the this database while the connection is not established");
    //     }
    //     try {
    //         PreparedStatement query = connection.prepareStatement("SELECT uuid FROM players WHERE playername = ?");
    //         query.setString(1, playername);
    //         ResultSet result = query.executeQuery();
    //         boolean isParticipant = result.next();
    //         if (!isParticipant) {
    //             throw new RuntimeException("The uuid of '" + playername + "' is not stored in the database");
    //         }
    //         final UUID uuid = UUID.fromString(result.getString("uuid"));
    //         query.close();

    //         // if (uuid != null)
    //         //     System.out.println(uuid.toString());
    //         // else
    //         //     System.out.println("null");

    //         return uuid;
    //     } catch (SQLException e) {
    //         throw new RuntimeException(e);
    //     }
    // }

    // public String getPlayerName(UUID uuid){
    //     if(isConnected()){
    //         try {
    //             PreparedStatement query = connection.prepareStatement("SELECT playername FROM players WHERE uuid = ?");
    //             query.setString(1, uuid.toString());
    //             ResultSet result = query.executeQuery();
    //             boolean isParticipant = result.next();
    //             String playername = null;
    //             if(isParticipant){
    //                 playername = result.getString("playername");
    //             }
    //             query.close();
    //             return playername;
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     return null;
    // }
}