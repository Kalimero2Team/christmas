package de.beyondblocks.plugins.christmas.storage;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import org.bukkit.Location;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {
    private final ChristmasPlugin plugin;
    private final HashMap<Location, Gift> giftCache = new HashMap<>();
    private Connection connection;

    public Storage(ChristmasPlugin plugin, File dataBase) {
        this.plugin = plugin;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataBase.getPath());
            createTablesIfNotExists();
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getSLF4JLogger().error("Error while creating database connection", e);
        }

    }

    private void createTablesIfNotExists() throws SQLException {
        createGiftsTableIfNotExists();
        createPlayersTableIfNotExists();
        createGiftsFoundTableIfNotExists();
    }

    private void createGiftsTableIfNotExists() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS GIFTS (" +
                "GIFT_UUID VARCHAR(36) NOT NULL PRIMARY KEY UNIQUE ," +
                "X INTEGER NOT NULL ," +
                "Y INTEGER NOT NULL ," +
                "Z INTEGER NOT NULL ," +
                "WORLD VARCHAR(255) NOT NULL" +
                ")");
    }

    private void createPlayersTableIfNotExists() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS PLAYERS (" +
                "UUID VARCHAR(36) NOT NULL PRIMARY KEY UNIQUE ," + //UUID
                "NAME VARCHAR(16) NOT NULL ," + //16 is the max length of a minecraft username
                "LAST_DATE VARCHAR(10) NOT NULL ," + //YYYY-MM-DD
                "PLAY_TIME INTEGER NOT NULL" + // in minutes
                ")");
    }

    private void createGiftsFoundTableIfNotExists() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS GIFTS_FOUND (" +
                "PLAYER_UUID VARCHAR(36) NOT NULL ," +
                "GIFT_UUID VARCHAR(36) NOT NULL ," +
                "FOREIGN KEY (PLAYER_UUID) REFERENCES PLAYERS(UUID) ," +
                "FOREIGN KEY (GIFT_UUID) REFERENCES GIFTS(GIFT_UUID) )"
        );
    }


    public void addGift(String uuid, int x, int y, int z, String world) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO GIFTS (GIFT_UUID,X,Y,Z,WORLD) VALUES ('" + uuid + "'," + x + "," + y + "," + z + ",'" + world + "')");
        giftCache.put(new Location(plugin.getServer().getWorld(world), x, y, z), new Gift(uuid, x, y, z, world));
    }

    public void removeGift(String uuid) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM GIFTS WHERE GIFT_UUID = '" + uuid + "'");
        giftCache.values().removeIf(gift -> gift.uuid().equals(uuid));
    }

    public List<Gift> getGifts() throws SQLException {
        if(!giftCache.isEmpty()){
            return new ArrayList<>(giftCache.values());
        }
        List<Gift> gifts = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS");
        while (resultSet.next()) {
            Gift gift = new Gift(resultSet.getString("UUID"), resultSet.getInt("X"), resultSet.getInt("Y"), resultSet.getInt("Z"), resultSet.getString("WORLD"));
            giftCache.put(new Location(plugin.getServer().getWorld(gift.world()), gift.x(), gift.y(), gift.z()), gift);
            gifts.add(gift);
        }
        return gifts;
    }

    public void addPlayer(String uuid, String name, String lastDate, int playTime) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO PLAYERS (UUID,NAME,LAST_DATE,PLAY_TIME) VALUES ('" + uuid + "','" + name + "','" + lastDate + "'," + playTime + ")");
    }

    public boolean playerExists(String uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM PLAYERS WHERE UUID = '" + uuid + "'");
        return resultSet.next();
    }

    public void updatePlayer(String uuid, String name, String lastDate, int playTime) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("UPDATE PLAYERS SET NAME = '" + name + "', LAST_DATE = '" + lastDate + "', PLAY_TIME = " + playTime + " WHERE UUID = '" + uuid + "'");
    }

    public String getPlayerLastDate(String uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT LAST_DATE FROM PLAYERS WHERE UUID = '" + uuid + "'");
        while (resultSet.next()) {
            return resultSet.getString("LAST_DATE");
        }
        return null;
    }

    public int getPlayerPlayTime(String uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAY_TIME FROM PLAYERS WHERE UUID = '" + uuid + "'");
        while (resultSet.next()) {
            return resultSet.getInt("PLAY_TIME");
        }
        return 0;
    }

    public void addPlayerFoundGift(String playerUuid, String giftUuid) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO GIFTS_FOUND (PLAYER_UUID,GIFT_UUID) VALUES ('" + playerUuid + "','" + giftUuid + "')");
    }

    public void removePlayerGifts(String playerUuid) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM GIFTS_FOUND WHERE PLAYER_UUID = '" + playerUuid + "'");
    }

    public Gift getGiftFromLocation(Location location) throws SQLException {
        if(giftCache.containsKey(location)){
            return giftCache.get(location);
        }
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS WHERE X = " + location.getBlockX() + " AND Y = " + location.getBlockY() + " AND Z = " + location.getBlockZ() + " AND WORLD = '" + location.getWorld().getName() + "'");
        while (resultSet.next()) {
            Gift gift = new Gift(resultSet.getString("GIFT_UUID"), resultSet.getInt("X"), resultSet.getInt("Y"), resultSet.getInt("Z"), resultSet.getString("WORLD"));
            giftCache.put(location, gift);
            return gift;
        }
        return null;
    }


    public List<String> getPlayerFoundGifts(String playerUuid) throws SQLException {
        List<String> gifts = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT GIFT_UUID FROM GIFTS_FOUND WHERE PLAYER_UUID = '" + playerUuid + "'");
        while (resultSet.next()) {
            gifts.add(resultSet.getString("GIFT_UUID"));
        }
        return gifts;
    }

    public boolean hasPlayerFoundGift(String playerUuid, String giftUuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS_FOUND WHERE PLAYER_UUID = '" + playerUuid + "' AND GIFT_UUID = '" + giftUuid + "'");
        while (resultSet.next()) {
            return true;
        }
        return false;
    }

    public String getFirstPlaceName() throws SQLException{
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC LIMIT 1");
        while (resultSet.next()) {
            String playerUuid = resultSet.getString("PLAYER_UUID");
            // Get the name of the player
            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("SELECT NAME FROM PLAYERS WHERE UUID = '" + playerUuid + "'");
            while (resultSet2.next()) {
                return resultSet2.getString("NAME");
            }
        }
        return null;
    }

    public String getSecondPlaceName() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC LIMIT 1,1");
        while (resultSet.next()) {
            String playerUuid = resultSet.getString("PLAYER_UUID");
            // Get the name of the player
            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("SELECT NAME FROM PLAYERS WHERE UUID = '" + playerUuid + "'");
            while (resultSet2.next()) {
                return resultSet2.getString("NAME");
            }
        }
        return null;
    }

    public String getThirdPlaceName() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC LIMIT 2,1");
        while (resultSet.next()) {
            String playerUuid = resultSet.getString("PLAYER_UUID");
            // Get the name of the player
            Statement statement2 = connection.createStatement();
            ResultSet resultSet2 = statement2.executeQuery("SELECT NAME FROM PLAYERS WHERE UUID = '" + playerUuid + "'");
            while (resultSet2.next()) {
                return resultSet2.getString("NAME");
            }
        }
        return null;
    }

    public int getFirstPlaceFound() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC LIMIT 1");
        while (resultSet.next()) {
            return resultSet.getInt("COUNT");
        }
        return 0;
    }

    public int getSecondPlaceFound() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC LIMIT 1,1");
        while (resultSet.next()) {
            return resultSet.getInt("COUNT");
        }
        return 0;
    }

    public int getThirdPlaceFound() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC LIMIT 2,1");
        while (resultSet.next()) {
            return resultSet.getInt("COUNT");
        }
        return 0;
    }

    public int getPlaceForPlayer(String playerUuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC");
        int place = 1;
        while (resultSet.next()) {
            if (resultSet.getString("PLAYER_UUID").equals(playerUuid)) {
                return place;
            }
            place++;
        }
        return 0;
    }


    public int getFoundForPlayer(String toString) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS COUNT FROM GIFTS_FOUND WHERE PLAYER_UUID = '" + toString + "'");
        while (resultSet.next()) {
            return resultSet.getInt("COUNT");
        }
        return 0;
    }


    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
