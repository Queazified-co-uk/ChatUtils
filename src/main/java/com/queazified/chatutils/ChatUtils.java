package com.queazified.chatutils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.util.List;

public class ChatUtils {

    /**
     * Sends an item in chat with hover showing full item info.
     * Displays: [Rank] Player: is holding [Item]
     */
    public static void sendItemWithHover(Player player, String rank) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(Component.text("You are not holding any item!", NamedTextColor.RED));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        String itemName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : item.getType().name();

        // Build hover text
        Component hover = Component.text(itemName + "\n", TextColor.fromHexString("#FFAA00"));

        if (meta != null) {
            // Enchantments
            meta.getEnchants().forEach((enchant, level) ->
                    hover = hover.append(Component.text(enchant.getKey().getKey() + " " + level + "\n", TextColor.fromHexString("#00FFAA"))));

            // Lore
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                for (String line : lore) {
                    hover = hover.append(Component.text(line + "\n", TextColor.fromHexString("#AAAAAA")));
                }
            }
        }

        // Full chat message
        Component message = Component.text("[" + rank + "] ", TextColor.fromHexString("#FFA500"))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" is holding ", NamedTextColor.WHITE))
                .append(Component.text("[" + itemName + "]", TextColor.fromHexString("#FFAA00"))
                        .hoverEvent(HoverEvent.showText(hover)));

        player.sendMessage(message);
    }
}
