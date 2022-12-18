package de.beyondblocks.plugins.christmas.command;


import de.beyondblocks.plugins.christmas.ChristmasPlugin;

public abstract class CommandHandler {
    protected final ChristmasPlugin plugin;
    protected final CommandManager commandManager;

    protected CommandHandler(ChristmasPlugin plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    public abstract void register();
}
