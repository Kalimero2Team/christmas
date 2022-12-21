package de.beyondblocks.plugins.christmas.command;

import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.context.CommandContext;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import de.beyondblocks.plugins.christmas.storage.Gift;
import de.beyondblocks.plugins.christmas.util.PlayerHeads;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
        commandManager.command(
                commandManager.commandBuilder("gifts")
                        .literal("isGift")
                        .permission("christmas.isGift")
                        .senderType(Player.class)
                        .argument(LocationArgument.of("location"))
                        .handler(this::isGift)

        );
    }

    private void isGift(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Gift location = plugin.getStorage().getGiftFromLocation(context.get("location"));
        if(location == null){
            player.sendMessage("This is not a gift");
        }else{
            player.sendMessage("This is a gift");
        }
    }

    private void list(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        player.sendMessage(plugin.getStorage().getGifts().size()+ " gifts exist");
    }

    private void reset(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        plugin.getStorage().removePlayerGifts(player.getUniqueId().toString());
        player.sendMessage("Gifts reset");
    }

    private void giveRandom(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            player.getInventory().addItem(PlayerHeads.getRandomPlayerHead(plugin));
        }
    }

    private BukkitRunnable addGiftsTask;

    private void addGifts(CommandContext<CommandSender> context) {
        if (context.getSender() instanceof Player player) {
            if(addGiftsTask != null){
                player.sendMessage(Component.text("Task already running ..."));
                return;
            }
            WorldEdit worldEdit = WorldEdit.getInstance();
            BukkitPlayer wePlayer = BukkitAdapter.adapt(player);
            SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
            LocalSession localSession = sessionManager.get(wePlayer);

            World selectionWorld = localSession.getSelectionWorld();
            if (selectionWorld != null) {
                Region selection = localSession.getSelection();
                addGiftsTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        AtomicInteger counter = new AtomicInteger();
                        selection.forEach(blockVector -> {
                            Block blockAt = BukkitAdapter.adapt(selectionWorld).getBlockAt(blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ());
                            if (blockAt.getType().equals(context.get("material"))) {
                                counter.getAndIncrement();
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        blockAt.setType(Material.PLAYER_HEAD, false);
                                        Skull skull = (Skull) blockAt.getState();
                                        skull.setPlayerProfile(PlayerHeads.getRandomPlayerProfile(plugin));
                                        skull.update();
                                        blockAt.getRelative(BlockFace.UP).setType(Material.AIR);
                                        Rotatable blockData = (Rotatable) blockAt.getBlockData();
                                        blockData.setRotation(PlayerHeads.randomBlockFace());
                                        plugin.getStorage().addGift(String.valueOf(UUID.randomUUID()), blockAt.getX(), blockAt.getY(), blockAt.getZ(), blockAt.getWorld().getName());
                                    }
                                }.runTask(plugin);
                            }
                        });
                        player.sendMessage(Component.text(counter.get()+" Gifts added"));
                        addGiftsTask = null;
                    }
                };
                addGiftsTask.runTaskAsynchronously(plugin);

            }

        }
    }
}
