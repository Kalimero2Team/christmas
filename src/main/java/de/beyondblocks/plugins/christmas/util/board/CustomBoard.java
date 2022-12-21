package de.beyondblocks.plugins.christmas.util.board;

import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CustomBoard {

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Player player;
    private final ChristmasPlugin plugin;
    private final PlaceholderProvider[] placeholderProviders;
    ArrayList<RandomColor> randomColors = new ArrayList<>();
    List<String> rawScoreboard = new ArrayList<>();
    List<String> teamIds = new ArrayList<>();
    private int taskId;
    private boolean shown;

    public CustomBoard(ChristmasPlugin plugin, Player player, PlaceholderProvider... placeholderProviders) {
        this.plugin = plugin;
        this.player = player;
        this.placeholderProviders = placeholderProviders;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("scoreboard", Criteria.DUMMY, Component.empty());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void setLines(List<String> lines) {
        rawScoreboard = lines;
        for (int i = 0; i < lines.size(); i++) {
            setLine(lines.size() - i, lines.get(i));
        }
    }

    private void setLine(int line, String template) {
        int randomTeam = (int) (Math.random() * ((99999 - 1) + 1)) - 1;
        Team team = scoreboard.registerNewTeam("team" + randomTeam);
        teamIds.add(team.getName());
        RandomColor randomColor = getRandomColors();

        team.addEntry(randomColor.getFirstColor() + "" + randomColor.getSecondColor());
        team.prefix(getComponent(template));
        objective.getScore(randomColor.getFirstColor() + "" + randomColor.getSecondColor()).setScore(line - 1);
    }

    public void update() {
        updateLines(rawScoreboard);
    }

    public void updateLines(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            updateLine(i, lines.get(i));
        }
    }

    private void updateLine(int teamLine, String template) {
        Objects.requireNonNull(scoreboard.getTeam(teamIds.get(teamLine)), "Team").prefix(getComponent(template));
    }

    public void setTitle(Component title) {
        objective.displayName(title);
    }

    public void setScoreboard() {
        player.setScoreboard(scoreboard);
    }

    public Component getComponent(String line) {
        List<TagResolver> placeholders = new ArrayList<>();
        Arrays.stream(placeholderProviders).forEach(placeholderProvider -> {
            placeholders.add(placeholderProvider.getPlaceholder());
        });

        return MiniMessage.miniMessage().deserialize(line, placeholders.toArray(new TagResolver[0]));
    }

    private void startSchedule(int ticks) {
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                updateLines(rawScoreboard);
                setScoreboard();
            }
        }.runTaskTimer(plugin, 0, ticks).getTaskId();
    }

    private void stopSchedule() {
        if (shown) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        shown = false;
    }

    private RandomColor getRandomColors() {
        String colors = "1234567890abcdef";
        RandomColor selectedColors;
        do {
            ChatColor color1;
            ChatColor color2;
            int color1num = new Random().nextInt(colors.length() - 1) + 1;
            int color2num = new Random().nextInt(colors.length() - 1) + 1;
            color1 = ChatColor.getByChar(colors.charAt(color1num));
            color2 = ChatColor.getByChar(colors.charAt(color2num));

            selectedColors = new RandomColor(color1, color2);
        } while (randomColors.contains(selectedColors));


        if (!randomColors.contains(selectedColors)) {
            randomColors.add(selectedColors);
            return selectedColors;
        } else {
            return new RandomColor(ChatColor.DARK_PURPLE, ChatColor.YELLOW);
        }
    }

    public void hideScoreboard() {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void showScoreboard() {
        setScoreboard();
    }

}
