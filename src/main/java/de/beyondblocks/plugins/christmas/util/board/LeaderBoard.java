package de.beyondblocks.plugins.christmas.util.board;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class LeaderBoard implements Listener {

    private final ChristmasPlugin plugin;
    private final HashMap<Player, CustomBoard> boards = new HashMap<>();
    private final HashMap<Player, ActionBar> actionBars = new HashMap<>();
    private final PlaceholderProvider datePlaceHolder = () -> Placeholder.unparsed("date", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
    private final PlaceholderProvider firstPlacePlaceHolder = () -> Placeholder.unparsed("first_place", Objects.requireNonNullElse(ChristmasPlugin.getPlugin().getStorage().getFirstPlaceName(),"null"));
    private final PlaceholderProvider secondPlacePlaceHolder = () -> Placeholder.unparsed("second_place", Objects.requireNonNullElse(ChristmasPlugin.getPlugin().getStorage().getSecondPlaceName(),"null"));
    private final PlaceholderProvider thirdPlacePlaceHolder = () -> Placeholder.unparsed("third_place", Objects.requireNonNullElse(ChristmasPlugin.getPlugin().getStorage().getThirdPlaceName(),"null"));
    private final PlaceholderProvider firstPointsPlaceHolder = () -> Placeholder.unparsed("first_points", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getFirstPlaceFound()));
    private final PlaceholderProvider secondPointsPlaceHolder = () -> Placeholder.unparsed("second_points", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getSecondPlaceFound()));
    private final PlaceholderProvider thirdPointsPlaceHolder = () -> Placeholder.unparsed("third_points", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getThirdPlaceFound()));


    public LeaderBoard(ChristmasPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    private CustomBoard getBoard(Player player) {
        PlaceholderProvider yourPlacePlaceHolder = () -> Placeholder.unparsed("your_place", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getPlaceForPlayer(player.getUniqueId().toString())));
        PlaceholderProvider yourPointsPlaceHolder = () -> Placeholder.unparsed("your_points", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getFoundForPlayer(player.getUniqueId().toString())));
        if (boards.containsKey(player)) {
            return boards.get(player);
        } else {

            CustomBoard board = new CustomBoard(plugin, player, datePlaceHolder, firstPlacePlaceHolder, secondPlacePlaceHolder, thirdPlacePlaceHolder, firstPointsPlaceHolder, secondPointsPlaceHolder, thirdPointsPlaceHolder, yourPlacePlaceHolder, yourPointsPlaceHolder);
            board.setTitle(MiniMessage.miniMessage().deserialize("<bold><gradient:#df3630:#d26f1f>Weihnachtsevent"));
            boards.put(player, board);

            ArrayList<String> lines = new ArrayList<>();
            lines.add("");
            lines.add("<#E8C561>1.</#E8C561> Platz:<#79E3EE> <first_place> (<first_points>)");
            lines.add("<#9DA5A6>2.</#9DA5A6> Platz:<#79E3EE> <second_place> (<second_points>)");
            lines.add("<#C07F47>3.</#C07F47> Platz:<#79E3EE> <third_place> (<third_points>)");
            lines.add("");
            lines.add("Dein Platz:<#79E3EE> <your_place>");
            lines.add("Deine Punkte:<#79E3EE> <your_points>");
            lines.add("");
            board.setLines(lines);

            return board;
        }
    }

    private ActionBar getActionBar(Player player){
        PlaceholderProvider yourPlacePlaceHolder = () -> Placeholder.unparsed("your_place", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getPlaceForPlayer(player.getUniqueId().toString())));
        PlaceholderProvider yourPointsPlaceHolder = () -> Placeholder.unparsed("your_points", String.valueOf(ChristmasPlugin.getPlugin().getStorage().getFoundForPlayer(player.getUniqueId().toString())));
        if(actionBars.containsKey(player)) {
            return actionBars.get(player);
        }else {
            String template = "Platz: <#79E3EE><your_place></#79E3EE> | Punkte: <#79E3EE><your_points></#79E3EE>";
            ActionBar actionBar = new ActionBar(plugin, player, template, datePlaceHolder, firstPlacePlaceHolder, secondPlacePlaceHolder, thirdPlacePlaceHolder, firstPointsPlaceHolder, secondPointsPlaceHolder, thirdPointsPlaceHolder, yourPlacePlaceHolder, yourPointsPlaceHolder);
            actionBars.put(player, actionBar);
            return actionBar;
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //getBoard(event.getPlayer()).showScoreboard();
        //getActionBar(event.getPlayer()).showActionbar();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        boards.remove(event.getPlayer());
    }


}
