package de.beyondblocks.plugins.christmas.storage;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import org.bukkit.Location;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Storage {
    private final ChristmasPlugin plugin;
    private final HashMap<Location, Gift> giftCache;
    private Connection connection;

    public Storage(ChristmasPlugin plugin, File dataBase) {
        this.plugin = plugin;
        this.giftCache = new HashMap<>();

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


    public void addGift(String uuid, int x, int y, int z, String world) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO GIFTS (GIFT_UUID,X,Y,Z,WORLD) VALUES ('" + uuid + "'," + x + "," + y + "," + z + ",'" + world + "')");
            giftCache.put(new Location(plugin.getServer().getWorld(world), x, y, z), new Gift(uuid, x, y, z, world));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeGift(String uuid) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM GIFTS WHERE GIFT_UUID = '" + uuid + "'");
            giftCache.values().removeIf(gift -> gift.uuid().equals(uuid));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Gift> getGifts() {
        try {
            if (!giftCache.isEmpty()) {
                return new ArrayList<>(giftCache.values());
            }
            List<Gift> gifts = new ArrayList<>();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS");
            while (resultSet.next()) {
                Gift gift = new Gift(resultSet.getString("GIFT_UUID"), resultSet.getInt("X"), resultSet.getInt("Y"), resultSet.getInt("Z"), resultSet.getString("WORLD"));
                giftCache.put(new Location(plugin.getServer().getWorld(gift.world()), gift.x(), gift.y(), gift.z()), gift);
                gifts.add(gift);
            }
            return gifts;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void addPlayer(String uuid, String name, String lastDate, int playTime) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO PLAYERS (UUID,NAME,LAST_DATE,PLAY_TIME) VALUES ('" + uuid + "','" + name + "','" + lastDate + "'," + playTime + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean playerExists(String uuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM PLAYERS WHERE UUID = '" + uuid + "'");
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updatePlayer(String uuid, String name, String lastDate, int playTime) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("UPDATE PLAYERS SET NAME = '" + name + "', LAST_DATE = '" + lastDate + "', PLAY_TIME = " + playTime + " WHERE UUID = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerLastDate(String uuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT LAST_DATE FROM PLAYERS WHERE UUID = '" + uuid + "'");
            while (resultSet.next()) {
                return resultSet.getString("LAST_DATE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPlayerPlayTime(String uuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT PLAY_TIME FROM PLAYERS WHERE UUID = '" + uuid + "'");
            while (resultSet.next()) {
                return resultSet.getInt("PLAY_TIME");
            }
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public void addPlayerFoundGift(String playerUuid, String giftUuid) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO GIFTS_FOUND (PLAYER_UUID,GIFT_UUID) VALUES ('" + playerUuid + "','" + giftUuid + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePlayerGifts(String playerUuid) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM GIFTS_FOUND WHERE PLAYER_UUID = '" + playerUuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Gift getGiftFromLocation(Location location) {
        try {
            if (giftCache.containsKey(location)) {
                return giftCache.get(location);
            }
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS WHERE X = " + location.getBlockX() + " AND Y = " + location.getBlockY() + " AND Z = " + location.getBlockZ() + " AND WORLD = '" + location.getWorld().getName() + "'");
            while (resultSet.next()) {
                Gift gift = new Gift(resultSet.getString("GIFT_UUID"), resultSet.getInt("X"), resultSet.getInt("Y"), resultSet.getInt("Z"), resultSet.getString("WORLD"));
                giftCache.put(location, gift);
                return gift;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Gift> getPlayerFoundGifts(String playerUuid) {
        try {
            List<Gift> gifts = new ArrayList<>();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS_FOUND WHERE PLAYER_UUID = '" + playerUuid + "'");
            while (resultSet.next()) {
                gifts.add(getGift(resultSet.getString("GIFT_UUID")));
            }
            return gifts;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Gift getGift(String gift_uuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM GIFTS WHERE GIFT_UUID = '" + gift_uuid + "'");
            while (resultSet.next()) {
                return new Gift(resultSet.getString("GIFT_UUID"), resultSet.getInt("X"), resultSet.getInt("Y"), resultSet.getInt("Z"), resultSet.getString("WORLD"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getPlaceForPlayer(String playerUuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC");
            int place = 1;
            while (resultSet.next()) {
                if (resultSet.getString("PLAYER_UUID").equals(playerUuid)) {
                    return place;
                }
                place++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getPlayerForPlace(int place) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT PLAYER_UUID, COUNT(*) AS COUNT FROM GIFTS_FOUND GROUP BY PLAYER_UUID ORDER BY COUNT DESC");
            int currentPlace = 1;
            while (resultSet.next()) {
                if (currentPlace == place) {
                    return resultSet.getString("PLAYER_UUID");
                }
                currentPlace++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
