package de.neko.deathplugin.commands;

import de.neko.deathplugin.DeathPlugin;
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

import java.util.Collections;
import java.util.List;

public class AddExchangeItemCommand implements CommandExecutor, TabCompleter {

    private final DeathPlugin deathPlugin;

    public AddExchangeItemCommand(DeathPlugin deathPlugin) {
        this.deathPlugin = deathPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("You must specify a name!").color(NamedTextColor.RED));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("You must specify a value!").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        itemStack.setAmount(1);
        if (itemStack.getType().equals(Material.AIR)) {
            sender.sendMessage(Component.text("You must have an item in your main hand!").color(NamedTextColor.RED));
            return true;
        }

        int value = 0;

        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("You must use a positive integer number!").color(NamedTextColor.RED));
            return true;
        }

        if (deathPlugin.newItem(args[0], itemStack, value)) {
            sender.sendMessage(Component.text("New item for the exchange was created.").color(NamedTextColor.WHITE));
        } else {
            sender.sendMessage(Component.text("Name is already taken, please use another one.").color(NamedTextColor.WHITE));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("[Name]");
        } else if (args.length == 2) {
            return Collections.singletonList("[Value]");
        }
        return null;
    }
}
