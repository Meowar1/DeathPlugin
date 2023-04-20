package de.neko.deathplugin.commands;

import de.neko.deathplugin.utils.SQLBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ExchangeCommand implements CommandExecutor, TabCompleter {

    private final SQLBridge sqlBridge;
    private final HashMap<Material, Integer> valueMap;

    public ExchangeCommand(SQLBridge sqlBridge, HashMap<Material, Integer> valueMap) {
        this.sqlBridge = sqlBridge;
        this.valueMap = valueMap;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }
        if (valueMap.isEmpty()) {
            sender.sendMessage(Component.text("There are no items to exchange!").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (valueMap.containsKey(itemInHand.getType())) {
                int vigor = valueMap.get(itemInHand.getType());
                vigor *= itemInHand.getAmount();
                player.getInventory().setItemInMainHand(new ItemStack(Material.POPPY, 0));
                sqlBridge.setMoney(player.getUniqueId(), vigor + sqlBridge.getMoney(player.getUniqueId()));
                sender.sendMessage(Component.text("You cant exchanged " + itemInHand.getAmount() + " " + itemInHand.getType().name() + " for " + vigor + " vigor!").color(NamedTextColor.WHITE));
                return true;
            } else {
                sender.sendMessage(Component.text("You cant exchange " + itemInHand.getType().name() + " for vigor! Please hold an item in your mainhand which you want to exchange!").color(NamedTextColor.WHITE));
                return true;
            }
        } else if (args.length >= 2) {
            Material material;
            int amount;
            try {
                material = Material.getMaterial(args[0]);
            } catch (Exception e) {
                sender.sendMessage(Component.text("Your item was not found or can't be exchanged!").color(NamedTextColor.RED));
                return true;
            }
            try {
                amount = Integer.parseInt(args[1]);
            } catch (Exception e) {
                sender.sendMessage(Component.text("You must use a positive integer number!").color(NamedTextColor.RED));
                return true;
            }

            if (valueMap.containsKey(material)) {
                int price = amount * valueMap.get(material);
                int credit = sqlBridge.getMoney(player.getUniqueId());
                if (price > credit) {
                    sender.sendMessage(Component.text("You can't buy " + amount + " " + material.name() + " for " + price + " vigor! You only own " + credit + " vigor!").color(NamedTextColor.RED));
                    return true;
                }
                sqlBridge.setMoney(player.getUniqueId(), credit-price);
                HashMap<Integer, ItemStack> restItems = player.getInventory().addItem(new ItemStack(material, amount));
                restItems.forEach((i, itemStack) -> {
                    player.getLocation().getWorld().dropItem(player.getLocation(), itemStack);
                });
                sender.sendMessage(Component.text("You have exchanged " + amount + " " + material.name() + " for " + price + " vigor.").color(NamedTextColor.WHITE));
            } else {
                sender.sendMessage(Component.text("Your item was not found or can't be exchanged!").color(NamedTextColor.RED));
            }
            return true;
        } else {
            sender.sendMessage(Component.text("You need to select an item that you want to exchange and then specify the number of items.").color(NamedTextColor.RED));
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            valueMap.forEach((material, value) -> {
                list.add(material.name());
            });
        } else if (args.length == 2) {
            list.add("[Amount]");
        } else {
            return Collections.emptyList();
        }
        return list.stream().filter(s -> s.startsWith(args[args.length-1])).collect(Collectors.toList());
    }
}
