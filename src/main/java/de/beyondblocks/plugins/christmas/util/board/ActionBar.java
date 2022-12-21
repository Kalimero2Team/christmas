package de.beyondblocks.plugins.christmas.util.board;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionBar {
    private final Player player;
    private final ChristmasPlugin plugin;
    private final String message;
    private int taskId;
    private boolean shown;
    private int taskTicks;
    private final PlaceholderProvider[] placeholderProviders;


    public ActionBar(ChristmasPlugin plugin, Player player, String message, PlaceholderProvider... placeholderProviders) {
        this.plugin = plugin;
        this.player = player;
        this.message = message;
        this.placeholderProviders = placeholderProviders;
    }


    private void startSchedule(int ticks) {
        taskTicks = ticks;
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                player.sendActionBar(getComponent());
            }
        }.runTaskTimer(plugin, 0, ticks).getTaskId();
    }

    public Component getComponent() {
        List<TagResolver> placeholders = new ArrayList<>();
        Arrays.stream(placeholderProviders).forEach(placeholderProvider -> {
            placeholders.add(placeholderProvider.getPlaceholder());
        });

        return MiniMessage.miniMessage().deserialize(message, placeholders.toArray(new TagResolver[0]));
    }

    private void stopSchedule() {
        if (shown) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        shown = false;
    }

    public void hideActionbar() {
        stopSchedule();
    }

    public void showActionbar() {
        if (!shown) startSchedule(20);
    }

}
