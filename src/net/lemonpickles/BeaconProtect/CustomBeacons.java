package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomBeacons {
    private BeaconProtect plugin;
    private BukkitTask task;
    CustomBeacons(BeaconProtect plugin){
        this.plugin = plugin;
        //refreshBeacons();
    }
    //clear customBeacons and add again
    public void stopBeacons(){
        if(task!=null){
            task.cancel();
            task = null;
        }
    }
    public void startBeacons(){
        if(task==null) {
            task = new CustomBeaconsUpdate(plugin).runTaskTimer(plugin, 0, 80);
        }
    }
    boolean checkFriendly(Player player, Block block){//true if player is friendly with beacon
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
            Group group = entry.getValue();
            if(!group.checkMember(player)){
                List<Location> beacons = checkForBlocks(block);
                for(Location location:group.getBeacons()){
                    if(beacons.contains(location)){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    int getMaxDurability(Block block){
        int maxTier = 0;
        List<Location> locations = checkForBlocks(block);
        for(Location location:locations){
            Beacon beacon = (Beacon)location.getBlock().getState();
            int tier = beacon.getTier();
            if(tier>maxTier){maxTier = tier;}
        }
        if(maxTier==0){return 0;}
        return getMaxTier(maxTier);
    }
    private int getMaxTier(int tier){
        return plugin.defaultBeaconMultiplier[tier-1];
    }
    int getMaxPenalty(Player player, Block block){
        Group group = null;
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()) {
            for (Location location : entry.getValue().getBeacons()) {
                if (entry.getValue().checkInRange(block.getLocation(), location, ((Beacon) location.getBlock().getState()).getTier())) {
                    group = entry.getValue();
                }
            }
        }
        if(group!=null){return getMaxPenalty(player, block, group);}
        return 0;
    }
    private int getMaxPenalty(Player player, Block block, Group group){//friendly players have no penalty
        if(checkFriendly(player, block)){
            return 0;//friendly players bypass beacon
        }else{
            return getMaxHit(block, group);
        }
    }
    private int getMaxHit(Block block, Group group){
        return group.getMaterialInVaults(block.getType());
    }

    private Boolean checkInRange(Location block, Location beacon, int tier){
        if(tier!=0){
            tier = plugin.defaultBeaconRange[tier-1];
            return(block.toVector().isInAABB(new Vector(beacon.getBlockX()-tier, 0, beacon.getBlockZ()-tier), new Vector(beacon.getBlockX()+tier, 256, beacon.getBlockZ()+tier)));
        }
        return false;
    }
    public List<Location> checkForBlocks(Block blk){//returns beacons that touch the block
        List<Location> blocks = new ArrayList<>();
        for(Map.Entry<Location, Block> entry:plugin.beacons.entrySet()){
            Block block = entry.getValue();
            Beacon beacon = (Beacon) block.getState();
            if(checkInRange(blk.getLocation(), block.getLocation(), beacon.getTier())){
                blocks.add(block.getLocation());
            }
        }
        return blocks;
    }
    private void beaconEffectPlayers(){
        for(Player player: Bukkit.getOnlinePlayers()){
            Location playerLocation = player.getLocation();
            for(Map.Entry<Location, Block> entry:plugin.beacons.entrySet()){
                Block block = entry.getValue();
                Beacon beacon = ((Beacon) block.getState());
                int beaconTier = beacon.getTier();
                if(beaconTier!=0){
                    if(!beacon.getEntitiesInRange().contains(player)){
                        if(checkInRange(playerLocation, block.getLocation(), beaconTier)){
                            PotionEffect effectPrimary = beacon.getPrimaryEffect();
                            PotionEffect effectSecondary = beacon.getSecondaryEffect();

                            if(effectPrimary!=null) {
                                player.removePotionEffect(effectPrimary.getType());
                                player.addPotionEffect(effectPrimary);
                            }
                            if(effectSecondary!=null){
                                player.removePotionEffect(effectSecondary.getType());
                                player.addPotionEffect(effectSecondary);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class CustomBeaconsUpdate extends BukkitRunnable {
        private final BeaconProtect plugin;
        CustomBeaconsUpdate(BeaconProtect plugin){
            this.plugin = plugin;

        }
        @Override
        public void run(){
            this.plugin.CustomBeacons.beaconEffectPlayers();
        }
    }
}
