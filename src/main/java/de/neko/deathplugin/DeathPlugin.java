package de.neko.deathplugin;

import de.neko.deathplugin.commands.*;
import de.neko.deathplugin.listeners.DeathListener;
import de.neko.deathplugin.listeners.JoinListener;
import de.neko.deathplugin.utils.SQLBridge;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

public final class DeathPlugin extends JavaPlugin {

    private SQLBridge sqlBridge;
    private HashMap<ItemStack, Integer> valueMap = new HashMap<>();
    private HashMap<String, ItemStack> nameMap = new HashMap<>();
    private File itemsFile;
    private FileConfiguration itemsConfig;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        sqlBridge = new SQLBridge(getConfig().getString("SQL.url"), getConfig().getString("SQL.user"), getConfig().getString("SQL.password"));

        itemsFile = new File(getDataFolder(), "items.yml");
        if (!itemsFile.exists())
            saveResource("items.yml", false);
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        Set<String> keys = itemsConfig.getKeys(false);

        for (String currKey : keys) {
            try {
                ItemStack itemStack = itemsConfig.getItemStack(currKey);
                if (itemStack == null)
                    throw new Exception();
                int value = Integer.parseInt(itemsConfig.getString(currKey + ".value"));
                if (!valueMap.containsKey(itemStack)) {
                    valueMap.put(itemStack, value);
                    nameMap.put(currKey, itemStack);
                }
                else
                    Bukkit.getServer().getLogger().log(Level.WARNING, "Item: " + itemStack + " is already on the list!");
            } catch (Exception e){
                Bukkit.getServer().getLogger().log(Level.SEVERE, currKey + " is set incorrectly!");
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
        this.getCommand("deathmoney").setExecutor(new DeathMoneyCommand(sqlBridge, getConfig().getString("Messages.money"), getConfig().getString("Messages.giveMoney"), getConfig().getString("Messages.takeMoney")));
        this.getCommand("deathmoneysend").setExecutor(new DeathMoneySendCommand(sqlBridge, getConfig().getString("Messages.moneySend1"), getConfig().getString("Messages.moneySend2")));
        this.getCommand("revive").setExecutor(new ReviveCommand(sqlBridge, getConfig().getInt("Money.reviveCost")));
        this.getCommand("exchange").setExecutor(new ExchangeCommand(sqlBridge, valueMap, nameMap));
        this.getCommand("exchangelist").setExecutor(new ExchangeListCommand(valueMap, nameMap));
        this.getCommand("addexchangeitem").setExecutor(new AddExchangeItemCommand(this));
    }

    public boolean newItem(String name, ItemStack itemStack, int value) {
        if (nameMap.containsKey(name.toLowerCase()))
            return false;
        itemsConfig.set(name, itemStack);
        itemsConfig.set(name + ".value", value);
        nameMap.put(name, itemStack);
        valueMap.put(itemStack, value);
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
