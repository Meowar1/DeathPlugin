package de.neko.deathplugin.commands;

import de.neko.deathplugin.utils.SQLBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DeathMoneyCommand implements CommandExecutor, TabCompleter {

    private final SQLBridge sqlBridge;
    private final String message;
    private final String giveMoney, takeMoney;

    public DeathMoneyCommand(SQLBridge sqlBridge, String message, String giveMoney, String takeMoney) {
        this.sqlBridge = sqlBridge;
        this.message = message;
        this.giveMoney = giveMoney;
        this.takeMoney = takeMoney;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("deathplugin.admin")) {
            if (args.length >= 3) {
                UUID receiver = sqlBridge.getUUID(args[1]);
                if (!(sqlBridge.playerExist(args[1])) /*Not really necessary.*/ || receiver == null) {
                    sender.sendMessage(Component.text("The player " + args[0] + " could not be found!").color(NamedTextColor.RED));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("You must use a positive integer number!").color(NamedTextColor.RED));
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "give":
                        sqlBridge.setMoney(receiver, sqlBridge.getMoney(receiver) + amount);
                        sender.sendMessage(Component.text("You gave " + args[1] + " " + amount + " vigor.").color(NamedTextColor.WHITE));
                        Player player = Bukkit.getPlayer(receiver);
                        if (player != null && player.isOnline())
                            player.sendMessage(Component.text(giveMoney.replaceAll("%money%", "" + amount)).color(NamedTextColor.WHITE));
                        return true;
                    case "remove":
                        int money = sqlBridge.getMoney(receiver);
                        sqlBridge.setMoney(receiver, Math.max(sqlBridge.getMoney(receiver) - amount, 0));
                        sender.sendMessage(Component.text("You took " + amount + " vigor from " + args[1] + ".").color(NamedTextColor.WHITE));
                        player = Bukkit.getPlayer(receiver);
                        if (player != null && player.isOnline())
                            player.sendMessage(Component.text(takeMoney.replaceAll("%money%", "" + amount)).color(NamedTextColor.WHITE));
                        return true;
                    default:
                        sender.sendMessage(Component.text("Command \"" + args[0] + "\" was not found!").color(NamedTextColor.RED));
                        return true;
                }
            }
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }
        Player player = (Player) sender;

        String newMessage = message.replaceAll("%money%", "" + sqlBridge.getMoney(player.getUniqueId()));
        player.sendMessage(newMessage);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("deathplugin.admin")) {
            List<String> list = new ArrayList<>();
            if (args.length == 1) {
                list.add("give");
                list.add("remove");
            } else if (args.length == 2) {
                return null;
            } else if (args.length == 3) {
                list.add("[Amount]");
            } else {
                return Collections.emptyList();
            }
            return list.stream().filter(s -> s.startsWith(args[args.length-1])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
