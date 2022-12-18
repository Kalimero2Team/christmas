package de.beyondblocks.plugins.christmas.listener;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import de.beyondblocks.plugins.christmas.storage.Gift;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class InteractListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            if(ChristmasPlugin.getPlugin().getStorage().playerExists(event.getPlayer().getUniqueId().toString())) {
                ChristmasPlugin.getPlugin().getStorage().updatePlayer(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(),null,0);
            }else{
                ChristmasPlugin.getPlugin().getStorage().addPlayer(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(),null,0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType().equals(Material.PLAYER_HEAD)) {
                ChristmasPlugin plugin = ChristmasPlugin.getPlugin();
                try {
                    Gift giftFromLocation = plugin.getStorage().getGiftFromLocation(event.getClickedBlock().getLocation());
                    if (giftFromLocation != null) {
                        if (!plugin.getStorage().hasPlayerFoundGift(event.getPlayer().getUniqueId().toString(), giftFromLocation.uuid())) {
                            plugin.getStorage().addPlayerFoundGift(event.getPlayer().getUniqueId().toString(), giftFromLocation.uuid());
                            event.getPlayer().sendActionBar(Component.text("Du hast ein Geschenk gefunden!").color(NamedTextColor.GREEN));
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
