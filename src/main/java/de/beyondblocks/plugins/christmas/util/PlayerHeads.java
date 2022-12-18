package de.beyondblocks.plugins.christmas.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.beyondblocks.plugins.christmas.ChristmasPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PlayerHeads {

    public static ItemStack getPlayerHead(String skinURL) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setPlayerProfile(getPlayerProfile(skinURL));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getRandomPlayerHead(ChristmasPlugin plugin) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setPlayerProfile(getRandomPlayerProfile(plugin));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static PlayerProfile getPlayerProfile(String skinURL) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(skinURL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        profile.setTextures(textures);
        return profile;
    }

    public static PlayerProfile getRandomPlayerProfile(ChristmasPlugin plugin) {
        List<String> heads = plugin.getConfig().getStringList("textures");
        String skinURL = heads.get(new Random().nextInt(heads.size()));
        return getPlayerProfile(skinURL);
    }

    public static BlockFace randomBlockFace() {
        List<BlockFace> blockFaces = new ArrayList<>(Arrays.stream(BlockFace.values()).toList());
        blockFaces.remove(BlockFace.SELF);
        blockFaces.remove(BlockFace.UP);
        blockFaces.remove(BlockFace.DOWN);

        return blockFaces.get(new Random().nextInt(blockFaces.size()));
    }

}
