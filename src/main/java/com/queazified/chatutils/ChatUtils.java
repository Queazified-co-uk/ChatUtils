package com.queazified.chatutils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public class ChatUtils extends JavaPlugin {

    private BukkitAudiences adventure;
    private MiniMessage miniMessage;
    private HashMap<UUID, Long> handCooldowns;

    @Override
    public void onEnable() {
        // Dependency check for PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("PlaceholderAPI not found! Disabling ChatUtils.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        this.adventure = BukkitAudiences.create(this);
        this.miniMessage = MiniMessage.miniMessage();
        this.handCooldowns = new HashMap<>();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§7[§bChatUtils§7] §cOnly players can use this command!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("hand")) {
            // Permission check
            if (!player.hasPermission("chatutils.hand")) {
                player.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            // Cooldown check
            FileConfiguration config = getConfig();
            int cooldown = config.getInt("hand-cooldown", 10);
            long now = System.currentTimeMillis();
            long last = handCooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (now - last < cooldown * 1000L) {
                long seconds = (cooldown - ((now - last) / 1000));
                player.sendMessage("§cYou must wait " + seconds + " seconds before using /hand again.");
                return true;
            }
            handCooldowns.put(player.getUniqueId(), now);

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage("§7[§bChatUtils§7] §cYou must be holding an item!");
                return true;
            }
            ItemMeta meta = item.getItemMeta();
            String itemName;
            if (meta != null && meta.hasDisplayName()) {
                itemName = meta.getDisplayName();
            } else {
                itemName = formatItemName(item.getType());
            }
            Component displayName = Component.text(itemName, NamedTextColor.AQUA).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);

            // Use HoverEvent.showItem for native Minecraft tooltip with NBT
            HoverEvent<ShowItem> hoverEvent = null;
            try {
                NBTItem nbtItem = new NBTItem(item);
                String nbtString = nbtItem.toString();
                net.kyori.adventure.text.event.HoverEvent.ShowItem showItem =
                    net.kyori.adventure.text.event.HoverEvent.ShowItem.of(
                        Key.key(item.getType().getKey().toString()),
                        item.getAmount(),
                        BinaryTagHolder.of(nbtString)
                    );
                hoverEvent = HoverEvent.showItem(showItem);
            } catch (Exception e) {
                // fallback: no hover
            }
            Component itemComponent = displayName;
            if (hoverEvent != null) {
                itemComponent = itemComponent.hoverEvent(hoverEvent);
            }

            // MiniMessage support for chat-format (prefix should be in config now)
            String format = config.getString("chat-format", "<gray>[<aqua>ChatUtils</aqua>] <yellow>%player_name%: %message%");
            String parsedFormat = PlaceholderAPI.setPlaceholders(player, format);

            // Replace %message% with the hand message component
            String[] parts = parsedFormat.split("%message%", 2);
            Component chatMessage = miniMessage.deserialize(parts[0])
                .append(Component.text("is holding ", NamedTextColor.YELLOW))
                .append(itemComponent);
            if (parts.length > 1) {
                chatMessage = chatMessage.append(miniMessage.deserialize(parts[1]));
            }

            // Send with Adventure for hex support
            adventure.all().sendMessage(chatMessage);

            return true;
        }

        if (command.getName().equalsIgnoreCase("iteminfo")) {
            // Permission check
            if (!player.hasPermission("chatutils.iteminfo")) {
                player.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage("§7[§bChatUtils§7] §cYou must be holding an item!");
                return true;
            }
            ItemMeta meta = item.getItemMeta();
            player.sendMessage("§bItem Info:");
            player.sendMessage("§7Type: §f" + item.getType());
            player.sendMessage("§7Amount: §f" + item.getAmount());
            if (meta != null) {
                if (meta.hasDisplayName()) player.sendMessage("§7Name: §f" + meta.getDisplayName());
                if (meta.hasLore()) {
                    player.sendMessage("§7Lore:");
                    for (String lore : meta.getLore()) player.sendMessage("  §f" + lore);
                }
                if (meta.hasEnchants()) {
                    player.sendMessage("§7Enchants:");
                    meta.getEnchants().forEach((enchant, level) ->
                        player.sendMessage("  §f" + enchant.getKey().getKey() + " " + level)
                    );
                }
            }
            return true;
        }

        return false;
    }

    private String formatItemName(Material mat) {
        String[] parts = mat.toString().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1))
              .append(" ");
        }
        return sb.toString().trim();
    }
}
}
