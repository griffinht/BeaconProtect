package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Group {
    private String name;
    private String description;
    private OfflinePlayer owner;
    //TODO private roles for roles and stuff
    private List<Location> beacons;
    private List<Location> vaults;
    int[] tiers;
    private Map<OfflinePlayer, Member> members;
    public Group(String name, String description, OfflinePlayer owner, Map<OfflinePlayer, Member> members, List<Location> beacons, List<Location> vaults, int[] tiers){
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members = members;
        this.beacons = beacons;
        this.vaults = vaults;
        this.tiers = tiers;
    }
    public OfflinePlayer getOwner(){
        return owner;
    }
    public void setOwner(OfflinePlayer owner){
        this.owner = owner;
    }
    public Map<OfflinePlayer, Member> getMembers(){
        return members;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    //member stuff
    public void addMember(Player player, Member member){
        members.put(player, member);
    }
    public void removeMember(Player player){
        members.remove(player);
    }
    public boolean checkMember(Player player){
        return members.containsKey(player);
    }
    public String getMembersAsString(){
        String members = "";
        for(Map.Entry<OfflinePlayer, Member> entry:this.members.entrySet()){
            members = members+entry.getKey().getName()+", ";
        }
        if(members.length()>2){
            members = members.substring(0,members.length()-2);
        }
        return members;
    }
    public List<Location> getBeacons(){
        return beacons;
    }
    public String getBeaconsAsString(){
        String beacons = "";
        for(Location location:getBeacons()){
            beacons = beacons+"["+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+"], ";
        }
        if(beacons.length()>2){
            beacons = beacons.substring(0,beacons.length()-2);
        }
        return beacons;
    }
    public void addBeacon(Location location){
        if(!beacons.contains(location)){
            beacons.add(location);
        }
    }
    public void removeBeacon(Location location){
        beacons.remove(location);
    }
    public boolean checkBeacon(Location location){
        return beacons.contains(location);
    }
    public List<Location> getVaults(){
        return vaults;
    }
    public void addVault(Location location){
        if(getVaults().contains(location)){
            beacons.add(location);
        }
    }
    public int getMaterialInVaults(Material material){
        int materialAmt = 0;
        for(Location location:vaults){
            Inventory inventory = ((Chest) location.getBlock().getState()).getInventory();
            for (ItemStack is : inventory) {
                if(is!=null) {
                    if (is.getType() == material) {
                        materialAmt = materialAmt + is.getAmount();
                    }
                }
            }
        }
        return materialAmt;
    }
    public List<Location> checkForBlocks(Block blk){//returns beacons that touch the block
        List<Location> blocks = new ArrayList<>();
        for(Location location:beacons) {
            if (checkInRange(blk.getLocation(), location, ((Beacon) location.getBlock().getState()).getTier())) {
                blocks.add(location);
            }
        }
        return blocks;
    }
    public boolean checkForBlock(Block blk) {//returns if block is in beacon's range
        Location finalLoc = null;
        for (Location location : beacons) {
            if (checkInRange(blk.getLocation(), location, ((Beacon) location.getBlock().getState()).getTier())) {
                finalLoc = location;
            }
        }
        return finalLoc!=null;
    }
        public void removeMaterialInVaults(Material material, int amount){
        for(Location location:vaults){
            Inventory inventory = ((Chest) location.getBlock().getState()).getInventory();
            for (ItemStack is : inventory) {
                if(is!=null) {
                    if (is.getType() == material) {
                        int oldAmt = amount;
                        if(is.getAmount()-amount<0){
                            amount = is.getAmount();
                        }
                        is.setAmount(is.getAmount()-amount);
                        amount = oldAmt-amount;
                    }
                }
            }
        }
    }
    public Boolean checkInRange(Location block, Location beacon, int tier){
        if(tier!=0){
            tier = tiers[tier-1];
            Vector blk = new Vector(block.getX(), block.getY(), block.getZ());
            Vector min = new Vector(beacon.getBlockX()-tier, 0, beacon.getBlockZ()-tier);
            Vector max = new Vector(beacon.getBlockX()+tier, 256, beacon.getBlockZ()+tier);
            if(blk.isInAABB(min, max)){
                return true;
            }
        }
        return false;
    }
    public void removeVault(Location location){
        vaults.remove(location);
    }
    public boolean checkVault(Location location){
        return vaults.contains(location);
    }
    public String getVaultsAsString(){
        String vaults = "";
        for(Location location:this.vaults){
            vaults = vaults+"["+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+"], ";
        }if(vaults.length()>2){
            vaults = vaults.substring(0,vaults.length()-2);
        }
        return vaults;
    }
}
