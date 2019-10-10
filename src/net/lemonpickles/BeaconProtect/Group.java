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
    private Map<OfflinePlayer,PlayerRole> members = new HashMap<>();
    private List<Player> invites = new ArrayList<>();
    public Group(String name, String description, OfflinePlayer owner, Map<OfflinePlayer,PlayerRole> members, List<Location> beacons, List<Location> vaults, int[] tiers){
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
        this.members.put(owner, PlayerRole.OWNER);
        this.vaults = new ArrayList<>();
        this.beacons = new ArrayList<>();
    }
    public int getMembersSize(){return members.size();}
    public OfflinePlayer getOwner(){
        return owner;
    }
    public void setOwner(OfflinePlayer owner){
        this.owner = owner;
    }
    public Map<OfflinePlayer,PlayerRole> getMembers(){
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
    void addMember(OfflinePlayer player, PlayerRole role){
        members.put(player, role);
    }
    public void addMember(OfflinePlayer player){members.put(player, PlayerRole.DEFAULT);}
    public void removeMember(Player player){members.remove(player);}
    public boolean checkMember(Player player){return members.containsKey(player);}
    public String getMembersAsString(){
        String thingy = "";
        String a = getMembersByRoleAsString(PlayerRole.DEFAULT);
        if(!a.equals("")){thingy+="Default: "+a;
        }
        a = getMembersByRoleAsString(PlayerRole.MEMBER);
        if(!a.equals("")){if(!thingy.equals("")){thingy+=", ";}thingy+="Member: "+a;}

        a = getMembersByRoleAsString(PlayerRole.TRUSTED);
        if(!a.equals("")){if(!thingy.equals("")){thingy+=", ";}thingy+="Trusted: "+a;}

        a = getMembersByRoleAsString(PlayerRole.ASSISTANT);
        if(!a.equals("")){if(!thingy.equals("")){thingy+=", ";}thingy+="Assistant: "+a;}

        a = getMembersByRoleAsString(PlayerRole.OWNER);
        if(!a.equals("")){if(!thingy.equals("")){thingy+=", ";}thingy+="Owner: "+a;}
        return thingy;
    }
    public List<Location> getBeacons(){
        return beacons;
    }
    public String getBeaconsAsString(){
        StringBuilder beacons = new StringBuilder();
        for(Location location:getBeacons()){
            beacons.append("[").append(location.getBlockX()).append(",").append(location.getBlockY()).append(",").append(location.getBlockZ()).append("], ");
        }
        if(beacons.length()>2){
            return beacons.substring(0,beacons.length()-2);
        }
        return beacons.toString();
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
    int getMaterialInVaults(Material material){
        int materialAmt = 0;
        List<Location> badVault = new ArrayList<>();//vaults that no longer exist as chests go here to be removed outside the loop
        for(Location location:vaults){
            Block block = location.getBlock();
            if(block.getType()==Material.CHEST){
                Inventory inventory = ((Chest) block.getState()).getInventory();
                for (ItemStack is : inventory) {
                    if(is!=null) {
                        if (is.getType() == material) {
                            materialAmt = materialAmt + is.getAmount();
                        }
                    }
                }
            }else{
                badVault.add(location);
            }
        }
        for(Location location:badVault){//was getting concurrentModificationException so I had to remove vaults outside of the loop
            removeVault(location);
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
    public void removeVault(Location location){
        vaults.remove(location);
    }
    public boolean checkVault(Location location){
        return vaults.contains(location);
    }
    public String getVaultsAsString(){
        StringBuilder vaults = new StringBuilder();
        for(Location location:this.vaults){
            vaults.append("[").append(location.getBlockX()).append(",").append(location.getBlockY()).append(",").append(location.getBlockZ()).append("], ");
        }if(vaults.length()>2){
            return vaults.substring(0,vaults.length()-2);
        }
        return vaults.toString();
    }
    public void addInvite(Player player){
        invites.add(player);
    }
    public void removeInvite(Player player){
        invites.remove(player);
    }
    public boolean checkInvite(Player player){
        return invites.contains(player);
    }
    public PlayerRole getRole(Player player){return members.get(player);}
    public boolean checkPlayerPermission(Player player, PlayerRole role){
        PlayerRole actualRole = members.get(player);
        if(role==PlayerRole.DEFAULT){
            return actualRole==PlayerRole.DEFAULT||actualRole==PlayerRole.MEMBER||actualRole==PlayerRole.TRUSTED||actualRole==PlayerRole.ASSISTANT||actualRole==PlayerRole.OWNER;
        }else if(role==PlayerRole.MEMBER){
            return actualRole==PlayerRole.MEMBER||actualRole==PlayerRole.TRUSTED||actualRole==PlayerRole.ASSISTANT||actualRole==PlayerRole.OWNER;
        }else if(role==PlayerRole.TRUSTED){
            return actualRole==PlayerRole.TRUSTED||actualRole==PlayerRole.ASSISTANT||actualRole==PlayerRole.OWNER;
        }else if(role==PlayerRole.ASSISTANT){
            return actualRole==PlayerRole.ASSISTANT||actualRole==PlayerRole.OWNER;
        }else if(role==PlayerRole.OWNER){
            return actualRole==PlayerRole.OWNER;
        }

        return false;
    }
    public void setRole(Player player, PlayerRole newRole){members.put(player, newRole);}
    private String getMembersByRoleAsString(PlayerRole role){
        StringBuilder string = new StringBuilder();
        for(Map.Entry<OfflinePlayer, PlayerRole> playerRole:members.entrySet()){
            if(playerRole.getValue()==role){
                string.append(playerRole.getKey().getName());
                string.append(", ");
            }
        }
        String aString = string.toString();
        if(aString.length()>=2){aString = aString.substring(0,aString.length()-2);}
        return aString;
    }
}
