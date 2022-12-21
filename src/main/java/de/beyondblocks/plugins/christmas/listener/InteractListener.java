package de.beyondblocks.plugins.christmas.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import de.beyondblocks.plugins.christmas.storage.Gift;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class InteractListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (ChristmasPlugin.getPlugin().getStorage().playerExists(event.getPlayer().getUniqueId().toString())) {
            ChristmasPlugin.getPlugin().getStorage().updatePlayer(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), null, 0);
        } else {
            ChristmasPlugin.getPlugin().getStorage().addPlayer(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), null, 0);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType().equals(Material.PLAYER_HEAD)) {
                ChristmasPlugin plugin = ChristmasPlugin.getPlugin();
                Gift giftFromLocation = plugin.getStorage().getGiftFromLocation(event.getClickedBlock().getLocation());
                if (giftFromLocation != null) {
                    if (plugin.getLeaderBoard().playerInteract(event.getPlayer(), giftFromLocation)) {
                        event.getPlayer().sendActionBar(Component.text("Du hast ein Geschenk gefunden!").color(NamedTextColor.GREEN));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event) {
        if (ChristmasPlugin.getPlugin().getStorage().getGiftFromLocation(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (ChristmasPlugin.getPlugin().getStorage().getGiftFromLocation(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

}
