package de.beyondblocks.plugins.christmas.util.board;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import de.beyondblocks.plugins.christmas.storage.Gift;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class LeaderBoard implements Listener {

    private final ChristmasPlugin plugin;
    private final HashMap<Player, CustomBoard> boards = new HashMap<>();
    private final HashMap<Player, ActionBar> actionBars = new HashMap<>();
    private final HashMap<UUID, List<Gift>> giftMap = new HashMap<>();
    private final HashMap<UUID, Integer> placeMap = new HashMap<>();

    private final PlaceholderProvider firstPlacePoints = getPlaceHolderForPoints(0);
    private final PlaceholderProvider secondPlacePoints = getPlaceHolderForPoints(1);
    private final PlaceholderProvider thirdPlacePoints = getPlaceHolderForPoints(2);
    private final PlaceholderProvider firstPlace = getPlaceHolderForPlace(0);
    private final PlaceholderProvider secondPlace = getPlaceHolderForPlace(1);
    private final PlaceholderProvider thirdPlace = getPlaceHolderForPlace(2);


    public LeaderBoard(ChristmasPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Load the Leaderboard
        loadPlace(1);
        loadPlace(2);
        loadPlace(3);
    }

    private void loadPlace(int firstPlace) {
        String playerForPlace = plugin.getStorage().getPlayerForPlace(firstPlace);
        if (playerForPlace != null) {
            UUID uuid = UUID.fromString(playerForPlace);
            giftMap.put(uuid, plugin.getStorage().getPlayerFoundGifts(uuid.toString()));
        }
    }


    public boolean playerInteract(Player player, Gift gift) {
        if (!giftMap.containsKey(player.getUniqueId())) {
            giftMap.put(player.getUniqueId(), plugin.getStorage().getPlayerFoundGifts(player.getUniqueId().toString()));
            placeMap.put(player.getUniqueId(), plugin.getStorage().getPlaceForPlayer(player.getUniqueId().toString()));
            getBoard(player).update();
        }
        if (giftMap.get(player.getUniqueId()).contains(gift)) {
            return false;
        } else {
            giftMap.get(player.getUniqueId()).add(gift);
            plugin.getStorage().addPlayerFoundGift(player.getUniqueId().toString(), gift.uuid());
            placeMap.put(player.getUniqueId(), plugin.getStorage().getPlaceForPlayer(player.getUniqueId().toString()));
            if (isOnLeaderBoard(player.getUniqueId())) {
                plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> getBoard(onlinePlayer).update());
            } else {
                getBoard(player).update();
            }
            return true;
        }
    }


    public CustomBoard getBoard(Player player) {
        PlaceholderProvider yourPlacePlaceHolder = () -> Placeholder.unparsed("your_place", placeMap.get(player.getUniqueId()).toString());
        PlaceholderProvider yourPointsPlaceHolder = () -> Placeholder.unparsed("your_points", String.valueOf(giftMap.get(player.getUniqueId()).size()));

        if (boards.containsKey(player)) {
            return boards.get(player);
        } else {
            CustomBoard board = new CustomBoard(plugin, player, yourPlacePlaceHolder, yourPointsPlaceHolder, firstPlace, secondPlace, thirdPlace, firstPlacePoints, secondPlacePoints, thirdPlacePoints);
            board.setTitle(MiniMessage.miniMessage().deserialize("<bold><gradient:#df3630:#d26f1f>Weihnachtsevent"));
            boards.put(player, board);

            ArrayList<String> lines = new ArrayList<>();
            lines.add("");
            lines.add("<#E8C561>1.</#E8C561> Platz:<#79E3EE> <place_0> (<points_0>)");
            lines.add("<#9DA5A6>2.</#9DA5A6> Platz:<#79E3EE> <place_1> (<points_1>)");
            lines.add("<#C07F47>3.</#C07F47> Platz:<#79E3EE> <place_2> (<points_2>)");
            lines.add("");
            lines.add("Dein Platz:<#79E3EE> <your_place>");
            lines.add("Deine Punkte:<#79E3EE> <your_points>");
            lines.add("");
            board.setLines(lines);

            return board;
        }
    }

    private ActionBar getActionBar(Player player) {
        PlaceholderProvider yourPlacePlaceHolder = () -> Placeholder.unparsed("your_place", placeMap.get(player.getUniqueId()).toString());
        PlaceholderProvider yourPointsPlaceHolder = () -> Placeholder.unparsed("your_points", String.valueOf(giftMap.get(player.getUniqueId()).size()));
        if (actionBars.containsKey(player)) {
            return actionBars.get(player);
        } else {
            String template = "Platz: <#79E3EE><your_place></#79E3EE> | Gefundene Geschenke: <#79E3EE><your_points></#79E3EE>";
            ActionBar actionBar = new ActionBar(plugin, player, template, yourPointsPlaceHolder);
            actionBars.put(player, actionBar);
            return actionBar;
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        placeMap.put(event.getPlayer().getUniqueId(), plugin.getStorage().getPlaceForPlayer(event.getPlayer().getUniqueId().toString()));
        giftMap.put(event.getPlayer().getUniqueId(), plugin.getStorage().getPlayerFoundGifts(event.getPlayer().getUniqueId().toString()));
        getBoard(event.getPlayer()).showScoreboard();
        //getActionBar(event.getPlayer()).showActionbar());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        boards.remove(event.getPlayer());
        placeMap.remove(event.getPlayer().getUniqueId());
        if (!isOnLeaderBoard(event.getPlayer().getUniqueId())) {
            giftMap.remove(event.getPlayer().getUniqueId());
        }
    }

    private boolean isOnLeaderBoard(UUID uuid) {
        int thirdLargest = giftMap.values().stream()
                .mapToInt(List::size)
                .sorted()
                .limit(3)
                .min()
                .orElse(0);

        return giftMap.get(uuid).size() >= thirdLargest;
    }

    private UUID getUUIDForPlace(int place) {
        Map.Entry<UUID, List<Gift>> uuidListEntry = giftMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .skip(place)
                .limit(1)
                .findFirst()
                .orElse(null);
        if (uuidListEntry != null) {
            return uuidListEntry.getKey();
        } else {
            return null;
        }
    }

    public PlaceholderProvider getPlaceHolderForPlace(int place) {
        return () -> {
            UUID uuid = getUUIDForPlace(place);
            if (uuid != null) {
                return Placeholder.unparsed("place_" + place, Objects.requireNonNullElse(Bukkit.getOfflinePlayer(uuid).getName(), "Unbekannt"));
            } else {
                return Placeholder.unparsed("place_" + place, "Niemand");
            }
        };
    }

    public PlaceholderProvider getPlaceHolderForPoints(int place) {
        return () -> {
            UUID uuid = getUUIDForPlace(place);
            if (uuid != null) {
                return Placeholder.unparsed("points_" + place, String.valueOf(giftMap.get(uuid).size()));
            } else {
                return Placeholder.unparsed("points_" + place, "0");
            }
        };
    }


}
