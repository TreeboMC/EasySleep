package me.shakeforprotein.easysleep;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class EasySleep extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("EasySleep enabled");
        this.getCommand("easysleep").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        if (getConfig().get("requireHalfToSleep") == null) {
            getConfig().set("requireHalfToSleep", 0);
        }
        for (World world : Bukkit.getWorlds()) {
            getConfig().set(world.getName(), 0);
        }
        saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    private void onPlayerSleepEvent(PlayerBedEnterEvent e) {
        Player p = e.getPlayer();
        if(getConfig().get("sleeping." + p.getName()) == null){
            getConfig().set("sleeping." + p.getName(), 0);
        }
        else if (getConfig().getInt("sleeping." + p.getName()) < 0){
            getConfig().set("sleeping." + p.getName(), 1);

        }
        else if (getConfig().getInt("sleeping." + p.getName()) >= 0){
            getConfig().set("sleeping." + p.getName(), getConfig().getInt("sleeping." + p.getName()) +1);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    getConfig().set("sleeping." + p.getName(), getConfig().getInt("sleeping." + p.getName()) -1);
                }
            }, 500L);
        }

        if(getConfig().getInt("sleeping." + p.getName()) > 3){
            p.sendMessage(ChatColor.RED + "You tried to hard to sleep and forgot how to wake up");
            p.setHealth(0);
            e.getBed().breakNaturally();
        }

        if(getConfig().getInt(p.getWorld().getName()) < 0){getConfig().set(p.getWorld().getName(), 0);}
        int democracy = getConfig().getInt("requireHalfToSleep");
        int playersOnWorld = e.getPlayer().getWorld().getPlayers().size();
        long currentTime = p.getWorld().getFullTime();
        long currentDay = currentTime / 24000;
        long newDay = (currentDay+1) * 24000;

        if (playersOnWorld % 2 != 0){
            playersOnWorld = playersOnWorld +1;
        }



        if ((p.getWorld().getTime() > 12860 && p.getWorld().getTime() < 23000) || p.getWorld().isThundering()) {
            if (democracy == 1) {
                if(p.isSleeping()){
                    getConfig().set(p.getWorld().getName(), getConfig().getInt(p.getWorld().getName()) + 1);


                    if (getConfig().getInt(p.getWorld().getName()) >= ((playersOnWorld / 2))) {
                        p.getWorld().setFullTime(newDay);
                        for (Player player : p.getWorld().getPlayers()) {
                            player.sendMessage(ChatColor.GOLD + "Wakey wakey and welcome to a brand new day");
                            getConfig().set(p.getWorld().getName(), 0);
                        }
                    } else {
                        for (Player player : p.getWorld().getPlayers()) {
                            player.sendMessage(ChatColor.GOLD + "There are " + ChatColor.GREEN + getConfig().get(p.getWorld().getName()) + ChatColor.GOLD + " players of a required " + ChatColor.RED + (playersOnWorld / 2) + ChatColor.GOLD + " who are in bed");
                        }
                    }
            }}
            else {
                    p.getWorld().setFullTime(newDay);
                    for (Player player : p.getWorld().getPlayers()) {
                        player.sendMessage(ChatColor.GOLD + "Wakey wakey and welcome to a brand new day");
                    }
                getConfig().set(p.getWorld().getName(), 0);

            }
        } else {
            p.sendMessage("You may only sleep at night or during a storm");
        }

    }

    @EventHandler
    private void onPlayerLeaveBedEvent(PlayerBedLeaveEvent e) {
        Player p = e.getPlayer();
        getConfig().set(p.getWorld().getName(), getConfig().getInt(p.getWorld().getName()) - 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player){Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("easysleep") && sender.hasPermission("easysleep.toggle")) {
            if (getConfig().getInt("requireHalfToSleep") == 1) {
                getConfig().set("requireHalfToSleep", 0);
                p.sendMessage("It now only requires one player to sleep");
                p.sendMessage(getConfig().getInt(((Player) sender).getWorld().getName()) + "");
            } else {
                getConfig().set("requireHalfToSleep", 1);
                p.sendMessage("It now requires half of a worlds players sleep");
                p.sendMessage(getConfig().getInt(((Player) sender).getWorld().getName()) + "");
            }
            saveConfig();
        }}
        return true;
    }

}
