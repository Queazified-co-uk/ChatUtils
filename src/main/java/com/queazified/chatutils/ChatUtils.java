package com.queazified.chatutils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ChatUtils extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§7[§bChatUtils§7] §cOnly players can use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§7[§bChatUtils§7] §cYou must be holding an item!");
            return true;
        }

        ItemMeta meta = item.getItemMeta();

        // Spigot-safe: getDisplayName() returns String
        String itemName;
        if (meta != null && meta.hasDisplayName()) {
            itemName = meta.getDisplayName();
        } else {
            itemName = formatItemName(item.getType());
        }

        Component displayName = Component.text(itemName, NamedTextColor.AQUA);

        // Hover lines (lore, enchantments, etc.)
        List<Component> hoverLines = new ArrayList<>();
        hoverLines.add(displayName);

        Component hoverText = Component.join(Component.newline(), hoverLines);

        Component itemComponent = displayName.color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(hoverText));

        // Build "is holding [item]" message
        Component handMessage = Component.text("is holding ", NamedTextColor.YELLOW)
                .append(itemComponent);

        // Apply chat format (using PAPI for prefix, name, etc.)
        String format = getConfig().getString("chat-format", "%luckperms_prefix% %player_name%: %message%");
        String parsedFormat = PlaceholderAPI.setPlaceholders(player, format);

        String before = parsedFormat.substring(0, parsedFormat.indexOf("%message%"));
        String after = parsedFormat.substring(parsedFormat.indexOf("%message%") + 9);

        Component chatMessage = Component.text(before)
                .append(handMessage)
                .append(Component.text(after));

        // Convert Component → legacy (§ codes) for Spigot broadcast
        String legacy = LegacyComponentSerializer.legacySection().serialize(chatMessage);
        Bukkit.broadcastMessage(legacy);

        return true;
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
