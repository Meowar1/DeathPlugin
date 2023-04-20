package de.neko.deathplugin.commands;

import de.neko.deathplugin.utils.SQLBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DeathMoneyCommand implements CommandExecutor, TabCompleter {

    private final SQLBridge sqlBridge;
    private final String message;

    public DeathMoneyCommand(SQLBridge sqlBridge, String message) {
        this.sqlBridge = sqlBridge;
        this.message = message;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
        return Collections.emptyList();
    }
}
