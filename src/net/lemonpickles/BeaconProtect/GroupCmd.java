package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

import static org.bukkit.Material.BEACON;

public class GroupCmd implements CommandExecutor {
    private BeaconProtect plugin;

    GroupCmd(BeaconProtect plugin) {
        this.plugin = plugin;
    }
    //add() and remove() and blockToCoordinates() were copied from beaconCmd
    //I could probably combine them
    private String blockToCoordinates(Block block){
        return "("+block.getX()+", "+block.getY()+", "+block.getZ()+")";
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length==0){
            if(sender instanceof Player){
                Player player = ((Player) sender);
                sender.sendMessage("Group:");
                for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()) {
                    Group g = entry.getValue();
                    if (g.checkMember(player)) {
                        sender.sendMessage("Name: " + g.getName() + ", Description: " + g.getDescription() + ", Owner: " + g.getOwner().getName() + ", Beacons: " + g.getBeaconsAsString() + ", Members: " + g.getMembersAsString());
                    }
                }
            }
        }else if(args.length==1){
            if(args[0].equalsIgnoreCase("list")){
                sender.sendMessage("Groups:");
                for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
                    Group g = entry.getValue();
                    OfflinePlayer a = g.getOwner();
                    String owner;
                    if(a==null){
                        owner = "";
                    }else{owner = a.getName();}
                    sender.sendMessage(g.getName());
                    sender.sendMessage("Owner: "+owner);
                    sender.sendMessage("Description: "+g.getDescription());
                    sender.sendMessage("Beacons: "+g.getBeaconsAsString());
                    sender.sendMessage("Vaults: "+g.getVaultsAsString());
                    sender.sendMessage("Members: "+g.getMembersAsString());
                }
            }else if(sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                Group group = null;
                for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {
                    if (entry.getValue().checkMember(player)) {
                        group = entry.getValue();
                    }
                }
                if(group!=null&&player!=null) {
                    if (args[0].equalsIgnoreCase("addBeacon")) {
                        Block beacon = player.getTargetBlock(null, 5);
                        Location location = beacon.getLocation();
                        if (beacon.getType() == BEACON) {
                            if (!group.checkBeacon(location)) {
                                group.addBeacon(location);
                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been registered to group "+group.getName());
                            } else {
                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has already been registered to group "+group.getName());
                            }
                        } else {
                            sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ", maybe move closer?");
                        }
                    } else if (args[0].equalsIgnoreCase("removeBeacon")) {
                        Block beacon = player.getTargetBlock(null, 5);
                        Location location = beacon.getLocation();
                        if (beacon.getType() == BEACON) {
                            if (group.checkBeacon(location)) {
                                group.removeBeacon(location);
                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been removed from group "+group.getName());
                            } else {
                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " is not owner by "+group.getName());
                            }
                        } else {
                            sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ", maybe move closer?");
                        }
                    }else if(args[0].equalsIgnoreCase("addVault")){
                        Block block = player.getTargetBlock(null, 5);
                        Location location = block.getLocation();
                        boolean inRange = false;
                        if(plugin.CustomBeacons.checkForBlocks(block).size()>0){//this better be a beacon or something will break just kidding
                            inRange = true;
                        }
                        if(block.getType()==Material.CHEST) {
                            if(inRange) {
                                if (!group.checkVault(location)) {
                                    group.addVault(location);
                                    sender.sendMessage("The chest at " + blockToCoordinates(block) + " has been registered to the group " + group.getName());
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " has already been registered to group " + group.getName());
                                }
                            }else{
                                sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " is not in the range of a beacon");
                            }
                        }else{
                            sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " is not a chest (found " + block.getType() + ", maybe move closer?");
                        }
                    }else if(args[0].equalsIgnoreCase("removeVault")){
                        Block block = player.getTargetBlock(null, 5);
                        Location location = block.getLocation();
                        if (group.checkVault(location)) {
                            group.removeVault(location);
                            sender.sendMessage("The vault at "+blockToCoordinates(block)+" has been removed from group "+group.getName());
                        } else {
                            sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " has not been registered to group " + group.getName() + " yet");
                        }
                    }else{return false;}
                    return true;
                }
            }else{sender.sendMessage("You must be a player to run that command!");}
            return true;
        }else if(args.length==3){
            for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
                UUID key = entry.getKey();
                String name = entry.getValue().getName();
                if(args[0].equalsIgnoreCase(name)){
                    if(args[1].equalsIgnoreCase("addMember")) {
                        try {
                            Player player = Bukkit.getServer().getPlayer(args[2]);
                            plugin.groups.get(key).addMember(player, new Member(player, "yeet"));
                            sender.sendMessage("Added " + args[2] + " to " + name);
                        } catch (Exception e) {
                            sender.sendMessage("Could not find player by the name of " + args[2]);
                        }
                    }else if(args[1].equalsIgnoreCase("removeMember")){
                        try {
                            Player player = Bukkit.getServer().getPlayer(args[2]);
                            plugin.groups.get(key).removeMember(player);
                            sender.sendMessage("Removed " + args[2] + " from " + name);
                        } catch (Exception e) {
                            sender.sendMessage("Could not find player by the name of " + args[2]);
                        }
                    }else if(args[1].equalsIgnoreCase("setOwner")){
                        try{
                            OfflinePlayer player = Bukkit.getServer().getPlayer(args[2]);
                            plugin.groups.get(key).setOwner(player);
                            sender.sendMessage("Set "+args[2]+" as owner of "+name);
                        }catch(Exception e){
                            sender.sendMessage("Could not find player by the name of "+args[2]);
                        }
                    }else if(args[1].equalsIgnoreCase("setDescription")) {
                        plugin.groups.get(key).setDescription(args[2]);
                        sender.sendMessage("Set description of "+name);
                    }else if(args[1].equalsIgnoreCase("setName")){
                        String oldName = plugin.groups.get(key).getName();
                        plugin.groups.get(key).setName(args[2]);
                        sender.sendMessage("Changed name of group from "+oldName+" to "+args[2]);
                    }else{
                        return false;
                    }
                    return true;
                }
            }
            sender.sendMessage("Could not find a group named "+args[0]);
            return true;
        }
        return false;
    }
}