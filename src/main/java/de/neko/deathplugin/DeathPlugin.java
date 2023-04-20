package de.neko.deathplugin;

import de.neko.deathplugin.commands.*;
import de.neko.deathplugin.listeners.DeathListener;
import de.neko.deathplugin.listeners.JoinListener;
import de.neko.deathplugin.utils.SQLBridge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

public final class DeathPlugin extends JavaPlugin {

    private SQLBridge sqlBridge;
    private HashMap<Material, Integer> valueMap = new HashMap<>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        sqlBridge = new SQLBridge(getConfig().getString("SQL.url"), getConfig().getString("SQL.user"), getConfig().getString("SQL.password"));

        File itemsFile = new File(getDataFolder(), "items.yml");
        if (!itemsFile.exists())
            saveResource("items.yml", false);
        FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        Set<String> keys = itemsConfig.getKeys(false);

        for (String currKey : keys) {
            try {
                Material material = Material.getMaterial(itemsConfig.getString(currKey + ".materialName"));
                if (material == null)
                    throw new Exception();
                int value = Integer.parseInt(itemsConfig.getString(currKey + ".value"));
                if (!valueMap.containsKey(material))
                    valueMap.put(material, value);
                else
                    Bukkit.getServer().getLogger().log(Level.WARNING, "Item: " + material.name() + " is already on the list!");
            } catch (Exception e){
                Bukkit.getServer().getLogger().log(Level.SEVERE, currKey + " is set incorrectly, materialName or value is incorrect!");
            }
        }

        registerListener();
        registerCommands();
    }

    public void registerListener() {
        this.getServer().getPluginManager().registerEvents(new JoinListener(sqlBridge, getConfig().getString("Messages.dailyJoin"), getConfig().getInt("Money.dailyJoin")), this);
        this.getServer().getPluginManager().registerEvents(new DeathListener(sqlBridge, getConfig().getString("Messages.death"), getConfig().getInt("Money.death"), getConfig().getBoolean("Money.loseAll"), getConfig().getString("Messages.ban")), this);
    }

    public void registerCommands() {
        this.getCommand("deathmoney").setExecutor(new DeathMoneyCommand(sqlBridge, getConfig().getString("Messages.money")));
        this.getCommand("deathmoneysend").setExecutor(new DeathMoneySendCommand(sqlBridge, getConfig().getString("Messages.moneySend1"), getConfig().getString("Messages.moneySend2")));
        this.getCommand("revive").setExecutor(new ReviveCommand(sqlBridge));
        this.getCommand("exchange").setExecutor(new ExchangeCommand(sqlBridge, valueMap));
        this.getCommand("exchangelist").setExecutor(new ExchangeListCommand(valueMap));
    }

    @Override
    public void onDisable() {

    }
}
