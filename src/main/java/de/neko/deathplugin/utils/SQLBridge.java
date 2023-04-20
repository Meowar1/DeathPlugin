package de.neko.deathplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import javax.annotation.CheckForNull;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class SQLBridge {
    private final String url, user, password;
    private Connection con;

    public SQLBridge(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        Statement stm;

        try {
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Connection to the SQL database is established.").color(NamedTextColor.GREEN));
            con = DriverManager.getConnection(url, user, password);
            stm = con.createStatement();
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Connection to the SQL database was successfully established.").color(NamedTextColor.GREEN));
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Connection to SQL database could not be established!").color(NamedTextColor.DARK_RED));
            Bukkit.getConsoleSender().sendMessage(Component.text(e.getMessage()).color(NamedTextColor.DARK_RED));
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
            return;
        }

        try {
            //Check for tables, if not existent, it will create them

            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Create missing tables.").color(NamedTextColor.GREEN));

            stm.executeUpdate("CREATE TABLE IF NOT EXISTS player_data (" +
                    "  UUID VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "  Name VARCHAR(32) NOT NULL," +
                    "  Money INT NOT NULL," +
                    "  LastOnline DATE NOT NULL," +
                    "  Banned BOOLEAN NOT NULL" +
                    ")");

            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]All missing tables were successfully created.").color(NamedTextColor.GREEN));
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Not all tables could be created!").color(NamedTextColor.DARK_RED));
            Bukkit.getConsoleSender().sendMessage(Component.text(e.getMessage()).color(NamedTextColor.DARK_RED));
            Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
        }
    }

    private void checkConnection () { //maybe exchange with a timed check
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM `player_data` WHERE 1");
        } catch (SQLException e) {
            try {
                Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
                Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Connection to the SQL database is re-established.").color(NamedTextColor.GREEN));
                con = DriverManager.getConnection(url, user, password);
                Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Connection to the SQL database has been successfully re-established.").color(NamedTextColor.GREEN));
                Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
            } catch (SQLException ex) {
                Bukkit.getConsoleSender().sendMessage(Component.text("[DP]Connection to SQL database could not be re-established!").color(NamedTextColor.DARK_RED));
                Bukkit.getConsoleSender().sendMessage(Component.text(ex.getMessage()).color(NamedTextColor.DARK_RED));
                Bukkit.getConsoleSender().sendMessage(Component.text("[DP]-------------------------------------------------------------").color(NamedTextColor.DARK_RED));
            }
        }
    }

    public boolean playerExist (UUID uuid) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM `player_data` WHERE `UUID`='" + uuid.toString() + "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean playerExist (String name) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM `player_data` WHERE `Name`='" + name + "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @CheckForNull
    public UUID getUUID (String name) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM `player_data` WHERE `Name`='" + name + "'");
            rs.next();
            return UUID.fromString(rs.getString("UUID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isBanned (UUID uuid) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM `player_data` WHERE `UUID`='" + uuid.toString() + "'");
            rs.next();
            return rs.getBoolean("Banned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void banPlayer (UUID uuid, Boolean banStatus) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            PreparedStatement pstm = con.prepareStatement("UPDATE `player_data` SET Banned=? WHERE UUID='" + uuid.toString() + "'");
            pstm.setBoolean(1, banStatus);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayerData (UUID uuid, String name) {
        checkConnection();
        try {
            PreparedStatement stm = con.prepareStatement("INSERT INTO `player_data`(`UUID`, `Name`, `Money`, `LastOnline`, `Banned`) VALUES (?, ?, ?, ?, ?)");
            stm.setString(1, uuid.toString());
            stm.setString(2, name);
            stm.setInt(3, 0);
            stm.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            stm.setBoolean(5, false);
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean newDay (UUID uuid) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT LastOnline FROM `player_data` WHERE `UUID`='" + uuid.toString() + "'");
            if(rs.next()) {
                LocalDate date = rs.getDate("LastOnline").toLocalDate();
                PreparedStatement pstm = con.prepareStatement("UPDATE `player_data` SET LastOnline=? WHERE UUID='" + uuid.toString() + "'");
                pstm.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                pstm.executeUpdate();
                return date.isAfter(LocalDate.now());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getMoney (UUID uuid) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT Money FROM `player_data` WHERE `UUID`='" + uuid.toString() + "'");
            if(rs.next()) {
                return rs.getInt("Money");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setMoney (UUID uuid, int amount) {
        checkConnection();
        try {
            Statement stm = con.createStatement();
            PreparedStatement pstm = con.prepareStatement("UPDATE `player_data` SET Money=? WHERE UUID='" + uuid.toString() + "'");
            pstm.setInt(1, amount);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
