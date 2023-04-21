package de.neko.deathplugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ExchangeListCommand implements CommandExecutor, TabCompleter {

    private final HashMap<ItemStack, Integer> valueMap;
    private final HashMap<String, ItemStack> nameMap;

    public ExchangeListCommand(HashMap<ItemStack, Integer> valueMap, HashMap<String, ItemStack> nameMap) {
        this.valueMap = valueMap;
        this.nameMap = nameMap;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!valueMap.isEmpty()) {
            final Component[] message = {Component.text("== Items and their value ==").color(NamedTextColor.YELLOW)};
            nameMap.forEach((name, itemStack) -> {
                message[0] = message[0].append(Component.text("\n" + name + " : " + valueMap.get(itemStack)).color(NamedTextColor.YELLOW));
            });
            message[0] = message[0].append(Component.text("\n== Items and their value ==").color(NamedTextColor.YELLOW));
            sender.sendMessage(message[0]);
        } else {
            sender.sendMessage(Component.text("There are no items to exchange!").color(NamedTextColor.RED));
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
