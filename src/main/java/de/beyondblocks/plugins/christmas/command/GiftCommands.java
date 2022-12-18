package de.beyondblocks.plugins.christmas.command;

import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.context.CommandContext;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import de.beyondblocks.plugins.christmas.util.PlayerHeads;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.SQLException;
import java.util.UUID;

public class GiftCommands extends CommandHandler {
    protected GiftCommands(ChristmasPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("gifts")
                        .literal("addfromselection")
                        .permission("christmas.addgifts")
                        .argument(MaterialArgument.of("material"))
                        .senderType(Player.class)
                        .handler(this::addGifts)
        );
        /*
        commandManager.command(
                commandManager.commandBuilder("gifts")
                        .literal("giveRandom")
                        .permission("christmas.giveRandom")
                        .senderType(Player.class)
                        .handler(this::giveRandom)
        );*/

        commandManager.command(
                commandManager.commandBuilder("gifts")
                        .literal("reset")
                        .permission("christmas.reset")
                        .senderType(Player.class)
                        .handler(this::reset)
        );
        commandManager.command(
                commandManager.commandBuilder("gifts")
                        .literal("list")
                        .permission("christmas.list")
                        .senderType(Player.class)
                        .handler(this::list)
        );
    }

    private void list(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        try {
            player.sendMessage(plugin.getStorage().getGifts().size()+ " gifts exist");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reset(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        try {
            plugin.getStorage().removePlayerGifts(player.getUniqueId().toString());
            player.sendMessage("Gifts reset");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("Error while resetting gifts");
        }
    }

    private void giveRandom(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            player.getInventory().addItem(PlayerHeads.getRandomPlayerHead(plugin));
        }
    }

    private void addGifts(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            WorldEdit worldEdit = WorldEdit.getInstance();
            BukkitPlayer wePlayer = BukkitAdapter.adapt(player);
            SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = sessionManager.get(wePlayer);

            World selectionWorld = localSession.getSelectionWorld();
            if (selectionWorld != null) {
                Region selection = localSession.getSelection();
                selection.forEach(blockVector -> {
                    Block blockAt = BukkitAdapter.adapt(selectionWorld).getBlockAt(blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ());
                    if (blockAt.getType().equals(context.get("material"))) {
                        blockAt.setType(Material.PLAYER_HEAD);
                        Skull skull = (Skull) blockAt.getState();
                        skull.setPlayerProfile(PlayerHeads.getRandomPlayerProfile(plugin));
                        skull.update();
                        blockAt.getRelative(BlockFace.UP).setType(Material.AIR);
                        Rotatable blockData = (Rotatable) blockAt.getBlockData();
                        blockData.setRotation(PlayerHeads.randomBlockFace());
                        try {
                            plugin.getStorage().addGift(String.valueOf(UUID.randomUUID()), blockAt.getX(), blockAt.getY(), blockAt.getZ(), blockAt.getWorld().getName());
                        } catch (SQLException e) {
                            player.sendMessage("§cError while adding gift");
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }
}
