package com.queazified.chatutils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
            player.sendMessage("You are not holding any item.");
            return true;
        }

        // Build item hover text
        List<Component> hoverLines = new ArrayList<>();

        // Item name
        Component displayName = item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().displayName()
                : Component.text(item.getType().toString().replace("_", " "), NamedTextColor.AQUA);

        hoverLines.add(displayName);

        // Enchantments
        if (item.getEnchantments().size() > 0) {
            item.getEnchantments().forEach((ench, lvl) -> {
                hoverLines.add(Component.text(ench.getKey().getKey().replace("_", " ") + " " + lvl, NamedTextColor.GRAY));
            });
        }

        // Base attack attributes (for swords/axes)
        if (item.getType().toString().endsWith("_SWORD") || item.getType().toString().endsWith("_AXE")) {
            hoverLines.add(Component.empty());
            hoverLines.add(Component.text("When in Main Hand:", NamedTextColor.GRAY));
            hoverLines.add(Component.text(" " + getAttackDamage(item) + " Attack Damage", NamedTextColor.GREEN));
            hoverLines.add(Component.text(" 1.6 Attack Speed", NamedTextColor.GREEN)); // Simplified (for swords)
        }

        Component hoverText = Component.join(Component.newline(), hoverLines);

        // Final message to chat
        Component message = Component.text(player.getName() + " is holding: ", NamedTextColor.YELLOW)
                .append(displayName.hoverEvent(HoverEvent.showText(hoverText)));

        Bukkit.broadcast(message);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }

    private double getAttackDamage(ItemStack item) {
        return switch (item.getType()) {
            case NETHERITE_SWORD -> 8;
            case DIAMOND_SWORD -> 7;
            case IRON_SWORD -> 6;
            case STONE_SWORD -> 5;
            case GOLDEN_SWORD, WOODEN_SWORD -> 4;
            default -> 0;
        };
    }
}
