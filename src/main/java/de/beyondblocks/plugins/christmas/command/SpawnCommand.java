package de.beyondblocks.plugins.christmas.command;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandHandler {
    public SpawnCommand(ChristmasPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        commandManager.command(commandManager.commandBuilder("spawn")
                .senderType(Player.class)
                .handler(commandContext -> {
                    Player player = (Player) commandContext.getSender();
                    player.teleport(player.getWorld().getSpawnLocation());
                })
        );
    }

}
