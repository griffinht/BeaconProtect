package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class Group {
    private String name;
    private String description;
    private OfflinePlayer owner;
    //TODO private roles for roles and stuff
    private List<Location> beacons;//TODO list all owned beacons here or something
    private Map<OfflinePlayer, Member> members;
    public Group(BeaconProtect plugin, String name, String description, OfflinePlayer owner, Map<OfflinePlayer, Member> members, List<Location> beacons){
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members = members;
        this.beacons = beacons;
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
    public void setName(BeaconProtect plugin, String name){
        plugin.groups.remove(this.name);
        this.name = name;
        plugin.groups.put(this.name, this);
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
        for(Location location:this.beacons){
            beacons = beacons+"["+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+"], ";
        }
        if(beacons.length()>2){
            beacons = beacons.substring(0,beacons.length()-2);
        }
        return beacons;
    }
    public void addBeacon(Location location){
        beacons.add(location);
    }
    public void removeBeacon(Location location){
        beacons.remove(location);
    }
    public boolean checkBeacon(Location location){
        return beacons.contains(location);
    }
}
