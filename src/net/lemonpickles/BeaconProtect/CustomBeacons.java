package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Material.BEACON;

public class CustomBeacons {
    private BeaconProtect plugin;
    private BukkitTask task;
    public CustomBeacons(BeaconProtect plugin){
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
    public void refreshBeacons(){
        World world = Bukkit.getServer().getWorld("world");
        this.plugin.beacons.clear();
        for(Map.Entry<Location, Block> entry:plugin.beacons.entrySet()){
            Location location = entry.getKey();
            Block block = world.getBlockAt(location);
            if(block.getType()==BEACON) {
                plugin.beacons.put(location,block);
            }
        }
    }
    //check individual beacon
    public void refreshBeacon(Location location){
        World world = Bukkit.getServer().getWorld("world");
        Block block = world.getBlockAt(location);
        //check for changed blocks
        checkBeacon(block, world);
        //check if new block is beacon
        if(block.getType()==BEACON&&!plugin.beacons.containsKey(block.getLocation())){
            plugin.beacons.put(block.getLocation(),block);
        }else if(block.getType()!=BEACON&&plugin.beacons.containsKey(block.getLocation())){
            this.plugin.beacons.remove(block.getLocation());
        }
    }
    //check if block is still a beacon
    private void checkBeacon(Block block, World world){
        if(world.getBlockAt(block.getLocation()).getType()!=BEACON){
            this.plugin.beacons.remove(block.getLocation());
        }
    }
    public boolean checkOwner(Player player, Block block){//true if player is also beacon's owner
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
            if(entry.getValue().getOwner()==player){
                if(entry.getValue().checkBeacon(block.getLocation())){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean checkFriendly(Player player, Block block){//true if player is friendly with beacon
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
    public int getMaxDurability(Block block){
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
    public int getMaxTier(int tier){
        return plugin.defaultBeaconMultiplier[tier-1];
    }
    public int getMaxPenalty(Player player, Block block){
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
    public int getMaxPenalty(Player player, Block block, Group group){//friendly players have no penalty
        if(checkFriendly(player, block)){
            return 0;//friendly players bypass beacon
        }else{
            return getMaxHit(block, group);
        }
    }
    private int getMaxHit(Block block, Group group){
        int maxHit = group.getMaterialInVaults(block.getType());
        return maxHit;
    }

    public Boolean checkInRange(Location block, Location beacon, int tier){
        if(tier!=0){
            tier = plugin.defaultBeaconRange[tier-1];
            Vector blk = new Vector(block.getX(), block.getY(), block.getZ());
            Vector min = new Vector(beacon.getBlockX()-tier, 0, beacon.getBlockZ()-tier);
            Vector max = new Vector(beacon.getBlockX()+tier, 256, beacon.getBlockZ()+tier);
            if(blk.isInAABB(min, max)){
                return true;
            }
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
    public void beaconEffectPlayers(){
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

    public class CustomBeaconsUpdate extends BukkitRunnable {
        private final BeaconProtect plugin;
        public CustomBeaconsUpdate(BeaconProtect plugin){
            this.plugin = plugin;

        }
        @Override
        public void run(){
            this.plugin.CustomBeacons.beaconEffectPlayers();
        }
    }
    public ArrayList<Block> locToBlock(ArrayList<Location> blocks){
        ArrayList<Block> blk = new ArrayList<>();
        World w = Bukkit.getWorld("world");
        for (Location l:blocks) {
            blk.add(w.getBlockAt(l));
        }
        return blk;
    }
}
