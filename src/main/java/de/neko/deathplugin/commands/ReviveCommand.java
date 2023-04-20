package de.neko.deathplugin.commands;

import de.neko.deathplugin.utils.SQLBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ReviveCommand implements CommandExecutor {

    private final SQLBridge sqlBridge;

    public ReviveCommand(SQLBridge sqlBridge) {
        this.sqlBridge = sqlBridge;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("You must specify a player!").color(NamedTextColor.RED));
            return true;
        }
        UUID uuid = sqlBridge.getUUID(args[0]);
        if (!(sqlBridge.playerExist(args[0])) || uuid == null) {
            sender.sendMessage(Component.text("The player " + args[0] + " could not be found!").color(NamedTextColor.RED));
            return true;
        }
        if (!sqlBridge.isBanned(uuid)) {
            sender.sendMessage(Component.text("The player " + args[0] + " was not banned for missing vigor! You can't unban him with /revive!").color(NamedTextColor.RED));
            return true;
        }
        sqlBridge.banPlayer(uuid, false);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "unban " + args[0]);
        sender.sendMessage(Component.text("The player " + args[0] + " can now join again!").color(NamedTextColor.WHITE));
        return true;
    }
}
