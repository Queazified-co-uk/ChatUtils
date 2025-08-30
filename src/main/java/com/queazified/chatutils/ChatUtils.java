package com.queazified.chatutils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ChatUtils extends JavaPlugin implements TabExecutor {

    @Override
    public void onEnable() {
        getCommand("hand").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You are not holding anything.");
            return true;
        }

        // --- Build Hover Text ---
        List<Component> hoverLines = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();

        Component displayName = meta.hasDisplayName()
                ? meta.displayName()
                : Component.text(formatItemName(item.getType()), NamedTextColor.AQUA);

        hoverLines.add(displayName);

        Component hoverText = Component.join(Component.newline(), hoverLines);

        Component itemComponent = displayName.color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(hoverText));

        // --- Hook into PlaceholderAPI chat format ---
        // Example: assume your chat format is something like: "%luckperms_prefix% %player_name%: %message%"
        String format = getConfig().getString("chat-format", "%luckperms_prefix% %player_name%: %message%");
        String parsedFormat = PlaceholderAPI.setPlaceholders(player, format);

        // Split at %message% to inject our Component
        String before = parsedFormat.substring(0, parsedFormat.indexOf("%message%"));
        String after = parsedFormat.substring(parsedFormat.indexOf("%message%") + 9);

        Component chatMessage = Component.text(before)
                .append(Component.text("is holding ", NamedTextColor.YELLOW))
                .append(itemComponent)
                .append(Component.text(after));

        Bukkit.broadcast(chatMessage);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
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
