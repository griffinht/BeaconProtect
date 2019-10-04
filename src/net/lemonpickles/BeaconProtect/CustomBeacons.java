package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public int checkForMaterial(Block block){
        List<Location> beacons = checkForBlocks(block);
        for(Location location:beacons){
            Material material = block.getType();
            //TODO:check if beacon's invent has the stuff
        }
        return 0;
    }
    public boolean checkOwner(Player player){//true if player is also beacon's owner
        return true;//TODO
    }
    public boolean checkFriendly(Player player){//true if player is friendly with beacon
        return false;//TODO
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
        return plugin.defaultBeaconMultiplier[maxTier-1];
    }
    public int getMaxPenalty(Player player, Block block){//friendly players have no penalty
        if(checkFriendly(player)){
            return 0;//friendly players bypass beacon
        }else{
            return getMaxHit(block);
        }
    }
    private int getMaxHit(Block block){
        List<Location> locations = checkForBlocks(block);
        for(Location location:locations){
            Beacon beacon = (Beacon)location.getBlock().getState();
            //TODO check if beacon has the blocks
        }
        return 5;
    }



    public List<Location> checkForBlocks(Block blk){
        List<Location> blocks = new ArrayList<>();
        Location l = blk.getLocation();
        Vector vector = new Vector(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        for(Map.Entry<Location, Block> entry:plugin.beacons.entrySet()){
            Block block = entry.getValue();
            Beacon beacon = (Beacon) block.getState();
            int tier;
            int beaconTier = beacon.getTier();
            if(beaconTier!=0){
                tier = plugin.defaultBeaconRange[beaconTier-1];
                Location location = block.getLocation();
                Vector min = new Vector(location.getBlockX()-tier, 0, location.getBlockZ()-tier);
                Vector max = new Vector(location.getBlockX()+tier, 256, location.getBlockZ()+tier);
                if(vector.isInAABB(min, max)){
                    blocks.add(block.getLocation());
                }
            }
        }
        return blocks;
    }
    public void beaconEffectPlayers(){
        for(Player player: Bukkit.getOnlinePlayers()){
            Location playerLocation = player.getLocation();
            Vector vector = new Vector(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
            for(Map.Entry<Location, Block> entry:plugin.beacons.entrySet()){
                Block block = entry.getValue();
                Beacon beacon = ((Beacon) block.getState());
                int tier;
                int beaconTier = beacon.getTier();
                if(beaconTier!=0){
                    tier = plugin.defaultBeaconRange[beaconTier-1];
                    Location location = block.getLocation();
                    Vector min = new Vector(location.getBlockX()-tier, 0, location.getBlockZ()-tier);
                    Vector max = new Vector(location.getBlockX()+tier, 256, location.getBlockZ()+tier);
                    if(!beacon.getEntitiesInRange().contains(player)){
                        if(vector.isInAABB(min, max)){
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
