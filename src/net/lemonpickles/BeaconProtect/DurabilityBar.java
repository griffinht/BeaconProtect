package net.lemonpickles.BeaconProtect;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class DurabilityBar {
    private BeaconProtect plugin;
    DurabilityBar(BeaconProtect plugin){
        this.plugin = plugin;
    }
    void addTimedBar(BossBar bar, Player player, int ticks){
        new bossBarRemove(plugin, bar, player, ticks);
    }
    public static class bossBarRemove extends BukkitRunnable {
        private BossBar bar;
        bossBarRemove(BeaconProtect plugin, BossBar bar, Player player, int ticks){
            this.bar = bar;
            if(plugin.durabilityBars.containsKey(player)){
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
