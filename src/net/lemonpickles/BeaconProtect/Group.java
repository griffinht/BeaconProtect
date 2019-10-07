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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
    private String name;
    private String description;
    private OfflinePlayer owner;
    //TODO private roles for roles and stuff
    private List<Location> beacons;
    private List<Location> vaults;
    private int[] tiers;
    private Map<OfflinePlayer, Member> members = new HashMap<>();
    private List<Player> invites = new ArrayList<>();
    public Group(String name, String description, OfflinePlayer owner, Map<OfflinePlayer, Member> members, List<Location> beacons, List<Location> vaults, int[] tiers){
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members = members;
        this.beacons = beacons;
        this.vaults = vaults;
        this.tiers = tiers;
    }
    public Group(String name, Player owner, int[] tiers){
        this.name = name;
        this.owner = owner;
        this.tiers = tiers;
        this.description = "";
        this.members.put(owner, new Member(owner));
        this.vaults = new ArrayList<>();
        this.beacons = new ArrayList<>();
    }
    int getMembersSize(){return members.size();}
    public OfflinePlayer getOwner(){
        return owner;
    }
    void setOwner(OfflinePlayer owner){
        this.owner = owner;
    }
    public Map<OfflinePlayer, Member> getMembers(){
        return members;
    }
    public String getName(){
        return name;
    }
    void setName(String name){
        this.name = name;
    }
    public String getDescription(){
        return description;
    }
    void setDescription(String description){
        this.description = description;
    }

    //member stuff
    void addMember(Player player, Member member){
        members.put(player, member);
    }
    void removeMember(Player player){
        members.remove(player);
    }
    void addMember(Player player){members.put(player, new Member(player));}
    boolean checkMember(Player player){
        return members.containsKey(player);
    }
    String getMembersAsString(){
        StringBuilder members = new StringBuilder();
        for(Map.Entry<OfflinePlayer, Member> entry:this.members.entrySet()){
            members.append(entry.getKey().getName()).append(", ");
        }
        if(members.length()>2){
            return members.substring(0,members.length()-2);
        }
        return members.toString();
    }
    public List<Location> getBeacons(){
        return beacons;
    }
    String getBeaconsAsString(){
        StringBuilder beacons = new StringBuilder();
        for(Location location:getBeacons()){
            beacons.append("[").append(location.getBlockX()).append(",").append(location.getBlockY()).append(",").append(location.getBlockZ()).append("], ");
        }
        if(beacons.length()>2){
            return beacons.substring(0,beacons.length()-2);
        }
        return beacons.toString();
    }
     void addBeacon(Location location){
        if(!beacons.contains(location)){
            beacons.add(location);
        }
    }
    void removeBeacon(Location location){
        beacons.remove(location);
    }
    boolean checkBeacon(Location location){
        return beacons.contains(location);
    }
    public List<Location> getVaults(){
        return vaults;
    }
    void addVault(Location location){
        if(getVaults().contains(location)){
            beacons.add(location);
        }
    }
    int getMaterialInVaults(Material material){
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
    boolean checkForBlock(Block blk) {//returns if block is in beacon's range
        Location finalLoc = null;
        for (Location location : beacons) {
            if (checkInRange(blk.getLocation(), location, ((Beacon) location.getBlock().getState()).getTier())) {
                finalLoc = location;
            }
        }
        return finalLoc!=null;
    }
    void removeMaterialInVaults(Material material, int amount){
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
    Boolean checkInRange(Location block, Location beacon, int tier){
        if(tier!=0){
            tier = tiers[tier-1];
            Vector blk = new Vector(block.getX(), block.getY(), block.getZ());
            Vector min = new Vector(beacon.getBlockX()-tier, 0, beacon.getBlockZ()-tier);
            Vector max = new Vector(beacon.getBlockX()+tier, 256, beacon.getBlockZ()+tier);
            return blk.isInAABB(min, max);
        }
        return false;
    }
    void removeVault(Location location){
        vaults.remove(location);
    }
    boolean checkVault(Location location){
        return vaults.contains(location);
    }
    String getVaultsAsString(){
        StringBuilder vaults = new StringBuilder();
        for(Location location:this.vaults){
            vaults.append("[").append(location.getBlockX()).append(",").append(location.getBlockY()).append(",").append(location.getBlockZ()).append("], ");
        }if(vaults.length()>2){
            return vaults.substring(0,vaults.length()-2);
        }
        return vaults.toString();
    }
    void addInvite(Player player){
        invites.add(player);
    }
    void removeInvite(Player player){
        invites.remove(player);
    }
    boolean checkInvite(Player player){
        return invites.contains(player);
    }
}
