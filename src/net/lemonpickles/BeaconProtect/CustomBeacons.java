package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class CustomBeacons {
    private BeaconProtect plugin;
    private BukkitTask task;
    private static final int[] defaultBeaconRange = new int[4];
    private static final int[] defaultBeaconMultiplier = new int[4];
    static {
        FileConfiguration config = BeaconProtect.getPlugin(BeaconProtect.class).getConfig();
        defaultBeaconRange[0] = config.getInt("beacon_tiers.tier1.range");
        defaultBeaconRange[1] = config.getInt("beacon_tiers.tier2.range");
        defaultBeaconRange[2] = config.getInt("beacon_tiers.tier3.range");
        defaultBeaconRange[3] = config.getInt("beacon_tiers.tier4.range");
        defaultBeaconMultiplier[0] = config.getInt("beacon_tiers.tier1.reinforce");
        defaultBeaconMultiplier[1] = config.getInt("beacon_tiers.tier2.reinforce");
        defaultBeaconMultiplier[2] = config.getInt("beacon_tiers.tier3.reinforce");
        defaultBeaconMultiplier[3] = config.getInt("beacon_tiers.tier4.reinforce");
    }
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
    private static boolean checkFriendly(Player player, Block block, Group group){
        if(!group.checkMember(player)) {
            List<Location> beacons = checkForBlocks(block, blockLocationsToMap(group.getBeacons()));
            for (Location location : group.getBeacons()) {
                if (beacons.contains(location)) {
                    return false;
                }
            }
        }
        return true;
    }
    static boolean checkFriendly(Player player, Block block, Map<UUID, Group> groups){//true if player is friendly with beacon
        for(Map.Entry<UUID, Group> entry:groups.entrySet()){
            if(!checkFriendly(player, block, entry.getValue())){return false;}
        }
        return true;
    }
    static Map<Location, Block> blockLocationsToMap(List<Location> locations){
        Map<Location, Block> returnVal = new HashMap<>();
        for(Location location:locations){
            returnVal.put(location, location.getBlock());
        }
        return returnVal;
    }
    static int getMaxDurability(Block block, Map<Location, Block> beacons){
        int maxTier = 0;
        List<Location> locations = checkForBlocks(block, beacons);
        for(Location location:locations){
            Beacon beacon = (Beacon)location.getBlock().getState();
            int tier = beacon.getTier();
            if(tier>maxTier){maxTier = tier;}
        }
        if(maxTier==0){return 0;}
        return getMaxTier(maxTier);
    }
    private static int getMaxTier(int tier){
        return defaultBeaconMultiplier[tier-1];
    }
    int getMaxPenalty(Player player, Block block){
        Group group = null;
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()) {
            for (Location location : entry.getValue().getBeacons()) {
                BlockState a = location.getBlock().getState();
                if(a instanceof Beacon) {//safe cast of beacon to ensure no errors if the block is not a beacon
                    if (checkInRange(block.getLocation(), location, ((Beacon)a).getTier())) {
                        group = entry.getValue();
                    }
                }
            }
        }
        if(group!=null){return getMaxPenalty(player, block, group);}
        return 0;
    }
    static private int getMaxPenalty(Player player, Block block, Group group){//friendly players have no penalty
        if(checkFriendly(player, block, group)){
            return 0;//friendly players bypass beacon
        }else{
            return getMaxHit(block, group);
        }
    }
    private static int getMaxHit(Block block, Group group){
        return group.getMaterialInVaults(block.getType());
    }

    private static Boolean checkInRange(Location block, Location beacon, int tier){
        if(tier!=0){
            tier = defaultBeaconRange[tier-1];
            return(block.toVector().isInAABB(new Vector(beacon.getBlockX()-tier, 0, beacon.getBlockZ()-tier), new Vector(beacon.getBlockX()+tier, 256, beacon.getBlockZ()+tier)));
        }
        return false;
    }
    public static List<Location> checkForBlocks(Block blk, Map<Location, Block> beacons){//returns beacons that touch the block
        List<Location> blocks = new ArrayList<>();
        for(Map.Entry<Location, Block> entry:beacons.entrySet()){
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
                try {
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
                }catch(ClassCastException e){
                    plugin.logger.warning("Block at "+block.getX()+", "+block.getY()+", "+block.getZ()+" is not a beacon");
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
