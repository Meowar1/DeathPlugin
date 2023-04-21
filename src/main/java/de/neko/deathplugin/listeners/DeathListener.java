package de.neko.deathplugin.listeners;

import de.neko.deathplugin.utils.SQLBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final SQLBridge sqlBridge;
    private final String deathMessage;
    private final int cost;
    private final boolean fullLost;
    private final String banMessage;

    public DeathListener(SQLBridge sqlBridge, String deathMessage, int cost, boolean fullLost, String banMessage) {
        this.sqlBridge = sqlBridge;
        this.deathMessage = deathMessage;
        this.cost = cost;
        this.fullLost = fullLost;
        this.banMessage = banMessage;
    }

    @EventHandler
    public void onDeath (PlayerDeathEvent e) {
        Player player = e.getPlayer();
        if (sqlBridge.getMoney(player.getUniqueId()) >= cost) {
            player.sendMessage(deathMessage);
            if (fullLost)
                sqlBridge.setMoney(player.getUniqueId(), 0);
            else
                sqlBridge.setMoney(player.getUniqueId(), sqlBridge.getMoney(player.getUniqueId()) - cost);
        } else {
            sqlBridge.banPlayer(player.getUniqueId(), true);
            player.getInventory().clear();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tempban " + player.getName() + " 24h " + banMessage);
        }
    }
}
