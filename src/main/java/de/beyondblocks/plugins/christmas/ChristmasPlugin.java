package de.beyondblocks.plugins.christmas;

import de.beyondblocks.plugins.christmas.command.CommandManager;
import de.beyondblocks.plugins.christmas.listener.InteractListener;
import de.beyondblocks.plugins.christmas.storage.Storage;
import de.beyondblocks.plugins.christmas.util.board.LeaderBoard;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChristmasPlugin extends JavaPlugin {

    private static ChristmasPlugin plugin;
    private Storage storage;
    private LeaderBoard leaderBoard;


    @Override
    public void onEnable() {
        plugin = this;
        try {
            new CommandManager(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(getDataFolder().mkdirs()){
            getLogger().info("Created data folder");
        }
        storage = new Storage(this,new File(getDataFolder()+"/christmas.db"));
        getServer().getPluginManager().registerEvents(new InteractListener(),this);
        leaderBoard = new LeaderBoard(this);
    }

    @Override
    public void onDisable() {
        storage.close();
    }

    public Storage getStorage() {
        return storage;
    }

    public static ChristmasPlugin getPlugin() {
        return plugin;
    }

    public LeaderBoard getLeaderBoard() {
        return leaderBoard;
    }
}
