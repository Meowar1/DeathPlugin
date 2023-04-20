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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DeathMoneySendCommand implements CommandExecutor, TabCompleter {

    private final SQLBridge sqlBridge;
    private final String message1;
    private final String message2;

    public DeathMoneySendCommand(SQLBridge sqlBridge, String message1, String message2) {
        this.sqlBridge = sqlBridge;
        this.message1 = message1;
        this.message2 = message2;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("You must specify a player!").color(NamedTextColor.RED));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("You must specify an Amount to send!").color(NamedTextColor.RED));
            return true;
        }

        int amount = 0;

        try {
            amount = Integer.valueOf(args[1]);
        } catch (Exception e) {
            sender.sendMessage(Component.text("You must use a positive integer number!").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        if (sqlBridge.getMoney(player.getUniqueId()) <= amount) {
            sender.sendMessage(Component.text("You do not have enough vigor! You only have " + sqlBridge.getMoney(player.getUniqueId()) + " vigor!").color(NamedTextColor.RED));
            return true;
        }

        UUID receiver = sqlBridge.getUUID(args[0]);

        if (!(sqlBridge.playerExist(args[0])) || receiver == null) {
            sender.sendMessage(Component.text("The player " + args[0] + " could not be found!").color(NamedTextColor.RED));
            return true;
        }

        String newMessage1 = message1.replaceAll("%money%", "" + amount);
        newMessage1 = newMessage1.replaceAll("%player%", player.getName());
        String newMessage2 = message2.replaceAll("%money%", "" + amount);
        newMessage2 = newMessage2.replaceAll("%player%", args[0]);
        player.sendMessage(newMessage1);

        Player player2 = Bukkit.getPlayer(receiver);
        if (player2 != null)
            player2.sendMessage(newMessage2);

        sqlBridge.setMoney(player.getUniqueId(), sqlBridge.getMoney(player.getUniqueId()) - amount);
        sqlBridge.setMoney(receiver, sqlBridge.getMoney(receiver) + amount);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return null;
        } else if (args.length == 2) {
            return Collections.singletonList("Amount");
        } else {
            return Collections.emptyList();
        }
    }
}

