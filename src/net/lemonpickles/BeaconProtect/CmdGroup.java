package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Material.BEACON;

public class CmdGroup implements CommandExecutor, TabCompleter {
    private BeaconProtect plugin;

    //add() and remove() and blockToCoordinates() were copied from beaconCmd
    //I could probably combine them
    //I really should make a command class
    //i hate this whole class its so long and hard to read
    private Map<String, List<String>> usages = new HashMap<>();
    CmdGroup(BeaconProtect plugin){
        this.plugin = plugin;
        List<String> list = new ArrayList<>();
        list.add("/group - commands related to managing your group");
        list.add("/group claimbeacon - claim a new protection beacon");
        list.add("/group unclaimbeacon - unclaim a protection beacon");
        list.add("/group addvault - adds a physical chest to your group's vaults");
        list.add("/group removevault - removes a chest from your group's vaults");
        list.add("/group invite <player> - invite a player to your group");
        list.add("/group kick <player> - kick a player from your group");
        list.add("/group join <group> - join an open group or a group that you were invited to");
        list.add("/group set - set properties related to your group");
        list.add("/group leave - leave your current group");
        usages.put("group", list);
        list = new ArrayList<>();
        list.add("/group set - set properties related to your group");
        list.add("/group set name <name> - sets a new name");
        list.add("/group set description <description> - sets a new description");
        list.add("/group set owner - sets a new owner, removing the old owner");
        usages.put("set", list);
    }
    private String blockToCoordinates(Block block){
        return "("+block.getX()+", "+block.getY()+", "+block.getZ()+")";
    }
    private void usage(CommandSender sender, String usage){
        sender.sendMessage("Usage for "+usage);
        for(String string:usages.get(usage)){
            sender.sendMessage(string);
        }
    }
    private Group findGroup(Player player){
        for(Group group:plugin.groups.values()){
            if(group.checkMember(player)){return group;}
        }
        return null;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length==0) {
            if (sender instanceof Player) {
                Group group = findGroup((Player) sender);
                if(group!=null){
                    sender.sendMessage("Group:");
                    sender.sendMessage("Name: " + group.getName() + ", Description: " + group.getDescription() + ", Owner: " + group.getOwner().getName() + ", Beacons: " + group.getBeaconsAsString() + ", Members: " + group.getMembersAsString());
                }else{
                    sender.sendMessage("You are not currently in a group.");
                    usage(sender, "group");
                }
                return true;
            }else{sender.sendMessage("You must be a player to use that command!");}
        }else if(args.length==1) {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                Group group = findGroup(player);
                if (player != null) {
                    if(group!=null) {
                        if (args[0].equalsIgnoreCase("claimbeacon")) {
                            Block beacon = player.getTargetBlock(null, 5);
                            Location location = beacon.getLocation();
                            if (beacon.getType() == BEACON) {
                                if (!group.checkBeacon(location)) {
                                    group.addBeacon(location);
                                    sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been claimed to group " + group.getName());
                                } else {
                                    sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has already been claimed to group " + group.getName());
                                }
                            } else {
                                sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ", maybe move closer?");
                            }
                        } else if (args[0].equalsIgnoreCase("unclaimbeacon")) {
                            Block beacon = player.getTargetBlock(null, 5);
                            Location location = beacon.getLocation();
                            if (beacon.getType() == BEACON) {
                                if (group.checkBeacon(location)) {
                                    group.removeBeacon(location);
                                    sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been unclaimed from group " + group.getName());
                                } else {
                                    sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " is not claimed by " + group.getName());
                                }
                            } else {
                                sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ", maybe move closer?");
                            }
                        } else if (args[0].equalsIgnoreCase("addvault")) {
                            Block block = player.getTargetBlock(null, 5);
                            Location location = block.getLocation();
                            boolean inRange = false;
                            if (plugin.CustomBeacons.checkForBlocks(block).size() > 0) {//this better be a beacon or something will break just kidding
                                inRange = true;
                            }
                            if (block.getType() == Material.CHEST) {
                                if (inRange) {
                                    if (!group.checkVault(location)) {
                                        group.addVault(location);
                                        sender.sendMessage("The chest at " + blockToCoordinates(block) + " has been registered to the group " + group.getName());
                                    } else {
                                        sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " has already been registered to group " + group.getName());
                                    }
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " is not in the range of a beacon");
                                }
                            } else {
                                sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " is not a chest (found " + block.getType() + ", maybe move closer?");
                            }
                        } else if (args[0].equalsIgnoreCase("removevault")) {
                            Block block = player.getTargetBlock(null, 5);
                            Location location = block.getLocation();
                            if (group.checkVault(location)) {
                                group.removeVault(location);
                                sender.sendMessage("The vault at " + blockToCoordinates(block) + " has been removed from group " + group.getName());
                            } else {
                                sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " has not been registered to group " + group.getName() + " yet");
                            }
                        } else if (args[0].equalsIgnoreCase("leave")) {
                            group.removeMember(player);
                            sender.sendMessage("Left group " + group.getName());
                        } else {
                            sender.sendMessage("Incorrect argument");
                            usage(sender, "group");
                            return true;
                        }
                        return true;
                    }else{
                        if(args[0].equalsIgnoreCase("join")){
                            for(Group a:plugin.groups.values()){
                                if(a.checkInvite(player)){group = a;}
                            }
                            if(group!=null){
                                group.removeInvite(player);
                                group.addMember(player);
                                sender.sendMessage("You have joined "+group.getName());
                            }else{sender.sendMessage("You have not been invited to any groups");}
                        }else{sender.sendMessage("You must be in a group to run that command");}

                    }
                } else {sender.sendMessage("Error: Could not get player (this should not happen)");}
            } else {
                sender.sendMessage("You must be a player to run that command!");
            }
            return true;
        }else if(args.length==2){
            if(sender instanceof Player) {
                Player player = ((Player) sender);
                if (args[0].equalsIgnoreCase("invite")) {
                    Group group = findGroup(player);
                    if(group!=null) {
                        try {
                            Player a = Bukkit.getPlayer(args[1]);
                            if(a!=null) {
                                if (!group.checkMember(a)) {
                                    group.addInvite(a);
                                    sender.sendMessage("Invited player to join " + group.getName());
                                    a.sendMessage("You have been invited to "+group.getName()+". Use /group join to accept");
                                } else {
                                    sender.sendMessage("That player is already a member of " + group.getName());
                                }
                            }else{sender.sendMessage("Could not find player "+args[1]);}
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Could not find player " + args[1]);
                        }
                        return true;
                    }else{sender.sendMessage("You must be in a group to use that command");}
                } else if (args[0].equalsIgnoreCase("kick")) {
                    //deja vu i have seen this code before
                    Group group = findGroup(player);
                    if(group!=null) {
                        try {
                            Player a = Bukkit.getPlayer(args[1]);
                            if(!group.checkMember(player)&&a!=null){
                                group.removeMember(a);
                                sender.sendMessage("Kicked "+a.getDisplayName()+" from "+group.getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage("Could not find player " + args[1]);
                        }
                    }else{sender.sendMessage("You must be in a group to use that command");}
                }else if(args[0].equalsIgnoreCase("join")){
                    for(Group group:plugin.groups.values()){
                        if(group.getName().equalsIgnoreCase(args[1])){
                            if(group.checkMember(player)){
                                if(group.checkInvite(player)){
                                    group.addMember(player, new Member(player, "poopoo"));
                                    group.removeInvite(player);
                                    sender.sendMessage("You have join the group "+group.getName());//TODO info for noobs like the spawn????
                                }else{sender.sendMessage("You have not yet been invited to this group");}
                            }else{sender.sendMessage("You are already in this group!");}
                        }else{sender.sendMessage("Could not find a group named "+group.getName()+". Check /groups list");}
                    }
                }else{
                    sender.sendMessage("Unknown argument");
                    usage(sender, "group");
                }
            }else{sender.sendMessage("You must be a player to run this command");}
            sender.sendMessage("Unknown argument");
            return true;
        }else if(args.length==3){
            for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
                UUID key = entry.getKey();
                String name = entry.getValue().getName();
                if(args[0].equalsIgnoreCase(name)){
                    if(args[1].equalsIgnoreCase("addMember")) {
                        try {
                            Player player = getServer().getPlayer(args[2]);
                            plugin.groups.get(key).addMember(player, new Member(player, "yeet"));
                            sender.sendMessage("Added " + args[2] + " to " + name);
                        } catch (Exception e) {
                            sender.sendMessage("Could not find player by the name of " + args[2]);
                        }
                    }else if(args[1].equalsIgnoreCase("removeMember")){
                        try {
                            Player player = getServer().getPlayer(args[2]);
                            plugin.groups.get(key).removeMember(player);
                            sender.sendMessage("Removed " + args[2] + " from " + name);
                        } catch (Exception e) {
                            sender.sendMessage("Could not find player by the name of " + args[2]);
                        }
                    }else if(args[1].equalsIgnoreCase("setOwner")){
                        try{
                            OfflinePlayer player = getServer().getPlayer(args[2]);
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
                }else{sender.sendMessage("Could not find group by the name of "+args[0]);}
            }
            sender.sendMessage("Could not find a group named "+args[0]);
            return true;
        }
        usage(sender, "group");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
        List<String> completions = new ArrayList<>();
        if(args.length==1){
            if(checkCompletions("claimbeacon", args[0])) {completions.add("claimbeacon");}
            if(checkCompletions("unclaimbeacon", args[0])) {completions.add("unclaimbeacon");}
            if(checkCompletions("addvault", args[0])) {completions.add("addvault");}
            if(checkCompletions("removevault", args[0])) {completions.add("removevault");}
            if(checkCompletions("invite", args[0])) {completions.add("invite");}
            if(checkCompletions("set", args[0])) {completions.add("set");}
            if(checkCompletions("join", args[0])){completions.add("join");}
            if(checkCompletions("leave",args[0])){completions.add("leave");}
            return completions;
        }else if(args.length==2){
            if(args[0].equalsIgnoreCase("set")){
                if(checkCompletions("name", args[0])) {completions.add("name");}
                if(checkCompletions("description", args[0])) {completions.add("description");}
                if(checkCompletions("owner", args[0])) {completions.add("owner");}
                return completions;
            }else if(args[0].equalsIgnoreCase("invite")){
                return null;//online players
            }else if(args[0].equalsIgnoreCase("kick")){
                return getGroupMembers(sender, args[1]);
            }else if(args[0].equalsIgnoreCase("join")){
                for(Group group:plugin.groups.values()){
                    if(checkCompletions(group.getName(),args[1])){completions.add(group.getName());}
                }
                return completions;
            }
        }else if(args.length==3){
            if(args[0].equalsIgnoreCase("set")){
                if(args[1].equalsIgnoreCase("name")){
                    return new ArrayList<>();
                }else if(args[1].equalsIgnoreCase("description")){
                    return new ArrayList<>();
                }else if(args[1].equalsIgnoreCase("owner")){
                    return getGroupMembers(sender, args[2]);
                }
            }
        }
        return null;
    }
    private boolean checkCompletions(String a, String arg){
        if(a.length()<arg.length()){return false;}
        return a.substring(0,arg.length()).equals(arg);
    }
    private List<String> getGroupMembers(CommandSender sender, String arg){
        List<String> completions = new ArrayList<>();
        for(Group group:plugin.groups.values()){
            if(group.checkMember(((Player) sender))) {
                for(OfflinePlayer offlinePlayer:group.getMembers().keySet()){
                    String name = offlinePlayer.getName();
                    if(name!=null) {
                        if (checkCompletions(name, arg)) {completions.add(name);}
                    }
                }
            }
        }
        return completions;
    }
}
//TODO this needs to go in /groups
/*}else if(args.length==1){
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
                */