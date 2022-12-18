package de.beyondblocks.plugins.christmas.util.playtime;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;

public class PlayTimeCheckerTask implements Runnable{

    private final ChristmasPlugin plugin;

    public PlayTimeCheckerTask(ChristmasPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.getServer().getOnlinePlayers().forEach(player -> {
            //this.plugin.getStorage().addPlayTime(player.getUniqueId(),1);
        });
    }
}
