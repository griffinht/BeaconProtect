package net.lemonpickles.BeaconProtect;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DurabilityBar {
    private BeaconProtect plugin;
    public DurabilityBar(BeaconProtect plugin){
        this.plugin = plugin;
    }
    public void addTimedBar(BossBar bar, Player player, int ticks){
        new bossBarRemove(plugin, bar, player, ticks);
    }
    public class bossBarRemove extends BukkitRunnable {
        private final BeaconProtect plugin;
        private BossBar bar;
        public bossBarRemove(BeaconProtect plugin, BossBar bar, Player player, int ticks){
            this.plugin = plugin;
            this.bar = bar;
            if(plugin.durabilityBars.containsKey(player)){//TODO:unnecessary check?
                plugin.durabilityBars.get(player).removeAll();
            }
            plugin.durabilityBars.put(player, bar);
            runTaskTimer(plugin, ticks, 1);
        }
        @Override
        public void run(){
            bar.removeAll();
            this.cancel();
        }
    }
}
