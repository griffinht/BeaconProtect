package net.lemonpickles.BeaconProtect;

import com.sun.istack.internal.NotNull;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Material.BEACON;

public class CmdGroup extends Cmd implements CommandExecutor, TabCompleter {
    CmdGroup(BeaconProtect plugin){
        super(plugin);
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
        list.add("/group create <name> - create your own group");
        usages.put("group", list);
        list = new ArrayList<>();
        list.add("/group set - set properties related to your group");
        list.add("/group set name <name> - sets a new name");
        list.add("/group set description <description> - sets a new description");
        list.add("/group set owner - sets a new owner, removing the old owner");
        usages.put("set", list);
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(args.length==0) {
            if (sender instanceof Player) {
                if(sender.hasPermission("beaconprotect.group")) {
                    Group group = findGroup((Player) sender);
                    if (group != null) {
                        sender.sendMessage("Group:");
                        sender.sendMessage("Name: " + group.getName() + ", Description: " + group.getDescription() + ", Owner: " + group.getOwner().getName() + ", Beacons: " + group.getBeaconsAsString() + ", Members: " + group.getMembersAsString());
                    } else {
                        sender.sendMessage("You are not currently in a group.");
                        usage(sender, "group");
                    }
                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                return true;
            }else{sender.sendMessage("You must be a player to use that command!");}
        }else if(args.length==1) {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                Group group = findGroup(player);
                if (player != null) {
                    if(group!=null) {
                        if (args[0].equalsIgnoreCase("claimbeacon")) {
                            if(sender.hasPermission("beaconprotect.group.claimbeacon")) {
                                Block beacon = player.getTargetBlock(null, 5);
                                Location location = beacon.getLocation();
                                if (beacon.getType() == BEACON) {
                                    Group claimedGroup = null;
                                    for(Group groups:plugin.groups.values()){
                                        if(groups.checkBeacon(location)){
                                            claimedGroup = groups;
                                            break;
                                        }
                                    }
                                    if (claimedGroup==null) {
                                        group.addBeacon(location);
                                        sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been claimed to group " + group.getName());
                                    } else {
                                        sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has already been claimed to group " + claimedGroup.getName());
                                    }
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ", maybe move closer?");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("unclaimbeacon")) {
                            if(sender.hasPermission("beaconprotect.group.unclaimbeacon")) {
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
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("addvault")) {
                            if(sender.hasPermission("beaconprotect.group.addvault")) {
                                Block block = player.getTargetBlock(null, 5);
                                Location location = block.getLocation();
                                boolean inRange = false;
                                for (Location loc:plugin.CustomBeacons.checkForBlocks(block)) {
                                    if(location.toVector().isInAABB(new Vector(loc.getX()-3,0,loc.getZ()-3),new Vector(loc.getX()+3,256,loc.getZ()+3))) {
                                        inRange = true;
                                        break;
                                    }
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
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("removevault")) {
                            if(sender.hasPermission("beaconprotect.group.removevault")) {
                                Block block = player.getTargetBlock(null, 5);
                                Location location = block.getLocation();
                                if (group.checkVault(location)) {
                                    group.removeVault(location);
                                    sender.sendMessage("The vault at " + blockToCoordinates(block) + " has been removed from group " + group.getName());
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " has not been registered to group " + group.getName() + " yet");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("leave")) {
                            if(sender.hasPermission("beaconprotect.group.leave")) {
                                if (!group.getOwner().getUniqueId().equals(player.getUniqueId())) {
                                    System.out.println(group.getOwner().getUniqueId() + " and " + player.getUniqueId());
                                    group.removeMember(player);
                                    sender.sendMessage("Left group " + group.getName());
                                } else {
                                    sender.sendMessage("You cannot leave your group as the owner. Use /group delete to delete your group instead, or make another player the owner");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        }else if(args[0].equalsIgnoreCase("delete")){
                            if(sender.hasPermission("beaconprotect.group.leave")) {
                                if (group.getOwner().getUniqueId().equals(player.getUniqueId())) {
                                    String name = group.getName();
                                    for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {
                                        if (entry.getValue() == group) {
                                            plugin.groups.remove(entry.getKey());
                                            sender.sendMessage("Deleted your group " + name);
                                            return true;
                                        }
                                    }
                                    sender.sendMessage("Could not find your group. That was not supposed to happen...");
                                } else {
                                    sender.sendMessage("You must be the owner of your group to delete it");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");return true;}
                        } else {
                            if(sender.hasPermission("beaconprotect.group.others")) {
                                for (Group g : plugin.groups.values()) {
                                    if (g.getName().equalsIgnoreCase(args[0])) {
                                        sender.sendMessage("Group:");
                                        sender.sendMessage("Name: " + g.getName() + ", Description: " + g.getDescription() + ", Owner: " + g.getOwner().getName() + ", Beacons: " + g.getBeaconsAsString() + ", Members: " + g.getMembersAsString());
                                        return true;
                                    }
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");return true;}
                        }
                        sender.sendMessage("Incorrect argument");
                        usage(sender, "group");
                        return true;
                    }else{
                        if(args[0].equalsIgnoreCase("join")) {
                            if(sender.hasPermission("beaconprotect.group.join")) {
                                for (Group a : plugin.groups.values()) {
                                    if (a.checkInvite(player)) {
                                        group = a;
                                    }
                                }
                                if (group != null) {
                                    group.removeInvite(player);
                                    group.addMember(player);
                                    sender.sendMessage("You have joined " + group.getName());
                                } else {
                                    sender.sendMessage("You have not been invited to any groups");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        }else if(args[0].equalsIgnoreCase("create")){
                            if(sender.hasPermission("beaconprotect.group.create")) {
                                sender.sendMessage("Usage: /group create <name>");
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                            return true;
                        }else{sender.sendMessage("You must be in a group to run that command");return true;}
                    }
                } else {sender.sendMessage("Error: Could not get player (this should not happen)");return true;}
            }else{
                if(sender.hasPermission("beaconprotect.group.others")) {
                    for (Group group : plugin.groups.values()) {
                        if (group.getName().equalsIgnoreCase(args[0])) {
                            sender.sendMessage("Group:");
                            sender.sendMessage("Name: " + group.getName() + ", Description: " + group.getDescription() + ", Owner: " + group.getOwner().getName() + ", Beacons: " + group.getBeaconsAsString() + ", Members: " + group.getMembersAsString());
                        }
                    }
                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                return true;
            }
        }else if(args.length==2){
            if(sender instanceof Player) {
                Player player = ((Player) sender);
                if (args[0].equalsIgnoreCase("invite")) {
                    if(sender.hasPermission("beaconprotect.group.invite")) {
                        Group group = findGroup(player);
                        if (group != null) {
                            try {
                                Player a = Bukkit.getPlayer(args[1]);
                                if (a != null) {
                                    if (!group.checkMember(a)) {
                                        group.addInvite(a);
                                        sender.sendMessage("Invited player to join " + group.getName());
                                        a.sendMessage("You have been invited to " + group.getName() + ". Use /group join to accept");
                                    } else {
                                        sender.sendMessage("That player is already a member of " + group.getName());
                                    }
                                } else {
                                    sender.sendMessage("Could not find player " + args[1]);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage("Could not find player " + args[1]);
                            }
                        } else {
                            sender.sendMessage("You must be in a group to use that command");
                        }
                    }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                    return true;
                } else if (args[0].equalsIgnoreCase("kick")) {
                    if(sender.hasPermission("beaconprotect.group.kick")) {
                        //deja vu i have seen this code before
                        Group group = findGroup(player);
                        if (group != null) {
                            try {
                                Player a = Bukkit.getPlayer(args[1]);
                                if (!group.checkMember(player) && a != null) {
                                    group.removeMember(a);
                                    sender.sendMessage("Kicked " + a.getDisplayName() + " from " + group.getName());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage("Could not find player " + args[1]);
                            }
                        } else {
                            sender.sendMessage("You must be in a group to use that command");
                        }
                    }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                    return true;
                }else if(args[0].equalsIgnoreCase("join")) {
                    if(sender.hasPermission("beaconprotect.group.join")) {
                        for (Group group : plugin.groups.values()) {
                            if (group.getName().equalsIgnoreCase(args[1])) {
                                if (group.checkMember(player)) {
                                    if (group.checkInvite(player)) {
                                        group.addMember(player, new Member(player, "poopoo"));
                                        group.removeInvite(player);
                                        sender.sendMessage("You have joined the group " + group.getName());//TODO info for noobs like the spawn????
                                    } else {
                                        sender.sendMessage("You have not yet been invited to this group");
                                    }
                                } else {
                                    sender.sendMessage("You are already in this group!");
                                }
                            }
                        }
                        sender.sendMessage("Could not find a group named " + args[1] + ". Check /groups list");
                    }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                    return true;
                }else if(args[0].equalsIgnoreCase("create")){
                    if(sender.hasPermission("beaconprotect.group.create")) {
                        if (findGroup(player) == null) {
                            plugin.groups.put(UUID.randomUUID(), new Group(args[1], player, plugin.defaultBeaconRange));
                            sender.sendMessage("Created a new group named " + args[1]);
                        } else {
                            sender.sendMessage("You must leave your current group before you can create a new one");
                        }
                    }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                    return true;
                }else{
                    sender.sendMessage("Unknown argument");
                    usage(sender, "group");
                }
            }else{sender.sendMessage("You must be a player to run this command");return true;}
            sender.sendMessage("Unknown argument");
        }else{
            if(args[0].equalsIgnoreCase("set")){
                if(sender instanceof Player){
                    for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
                        if(entry.getValue().checkMember((Player)sender)){
                            UUID key = entry.getKey();
                            String name = entry.getValue().getName();
                            if(args[1].equalsIgnoreCase("owner")){
                                if(sender.hasPermission("beaconprotect.group.set.owner")) {
                                    try {
                                        OfflinePlayer player = getServer().getPlayer(args[2]);
                                        plugin.groups.get(key).setOwner(player);
                                        sender.sendMessage("Set " + args[2] + " as owner of " + name);
                                    } catch (Exception e) {
                                        sender.sendMessage("Could not find player by the name of " + args[2]);
                                    }
                                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                                return true;
                            }else if(args[1].equalsIgnoreCase("name")){
                                if(sender.hasPermission("beaconprotect.group.set.name")) {
                                    String oldName = plugin.groups.get(key).getName();
                                    plugin.groups.get(key).setName(args[2]);
                                    sender.sendMessage("Changed name of group from " + oldName + " to " + args[2]);
                                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                                return true;
                            }else if(args[1].equalsIgnoreCase("description")) {
                                if(sender.hasPermission("beaconprotect.group.set.description")) {
                                    StringBuilder builder = new StringBuilder();
                                    for(int i = 0; i<args.length; i++){
                                        if(i>2){
                                            builder.append(" ").append(args[i]);
                                        }
                                    }
                                    plugin.groups.get(key).setDescription(builder.toString());
                                    sender.sendMessage("Set description of " + name);
                                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command!");}
                                return true;
                            }else{
                                sender.sendMessage("Incorrect argument");
                                usage(sender, "set");
                            }
                        }
                    }
                    sender.sendMessage("You must be in a group to use that command");
                    return true;
                }
            }else{sender.sendMessage("Could not find group by the name of "+args[0]);return true;}
        }
        usage(sender, "group");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args){
        List<String> completions = new ArrayList<>();
        if(args.length==1){
            if(sender.hasPermission("beaconprotect.group.claimbeacon")&&checkCompletions("claimbeacon", args[0])) {completions.add("claimbeacon");}
            if(sender.hasPermission("beaconprotect.group.unclaimbeacon")&&checkCompletions("unclaimbeacon", args[0])) {completions.add("unclaimbeacon");}
            if(sender.hasPermission("beaconprotect.group.addvault")&&checkCompletions("addvault", args[0])) {completions.add("addvault");}
            if(sender.hasPermission("beaconprotect.group.removevault")&&checkCompletions("removevault", args[0])) {completions.add("removevault");}
            if(sender.hasPermission("beaconprotect.group.invite")&&checkCompletions("invite", args[0])) {completions.add("invite");}
            if(sender.hasPermission("beaconprotect.group.set")&&checkCompletions("set", args[0])) {completions.add("set");}
            if(sender.hasPermission("beaconprotect.group.join")&&checkCompletions("join", args[0])){completions.add("join");}
            if(sender.hasPermission("beaconprotect.group.leave")&&checkCompletions("leave",args[0])){completions.add("leave");}
            if(sender.hasPermission("beaconprotect.group.create")&&checkCompletions("create",args[0])){completions.add("create");}
            if(sender.hasPermission("beaconprotect.group.delete")&&checkCompletions("delete",args[0])){completions.add("delete");}
            if(completions.size()==0){
                if(sender.hasPermission("beaconprotect.group.other")) {
                    for (Group group : plugin.groups.values()) {
                        if (checkCompletions(group.getName(), args[0])) {
                            completions.add(group.getName());
                        }
                    }
                }else if(sender.hasPermission("beaconprotect.group")){
                    for (Group group : plugin.groups.values()) {
                        if (group.checkMember((Player)sender)&&checkCompletions(group.getName(), args[0])) {
                            completions.add(group.getName());
                        }
                    }
                }
            }
            return completions;
        }else if(args.length==2){
            if(args[0].equalsIgnoreCase("set")){
                if(sender.hasPermission("beaconprotect.group.set.name")&&checkCompletions("name", args[1])) {completions.add("name");}
                if(sender.hasPermission("beaconprotect.group.set.description")&&checkCompletions("description", args[1])) {completions.add("description");}
                if(sender.hasPermission("beaconprotect.group.set.owner")&&checkCompletions("owner", args[1])) {completions.add("owner");}
                return completions;
            }else if(sender.hasPermission("beaconprotect.group.invite")&&args[0].equalsIgnoreCase("invite")){
                return null;//online players
            }else if(sender.hasPermission("beaconprotect.group.kick")&&args[0].equalsIgnoreCase("kick")){
                return getGroupMembers(sender, args[1]);
            }else if(sender.hasPermission("beaconprotect.group.join")&&args[0].equalsIgnoreCase("join")){
                for(Group group:plugin.groups.values()){
                    if(checkCompletions(group.getName(),args[1])){completions.add(group.getName());}
                }
                return completions;
            }
        }else if(args.length>=3){
            if(args[0].equalsIgnoreCase("set")){
                if(sender.hasPermission("beaconprotect.group.name")&&args[1].equalsIgnoreCase("name")){
                    return new ArrayList<>();
                }else if(sender.hasPermission("beaconprotect.group.description")&&args[1].equalsIgnoreCase("description")){
                    return new ArrayList<>();
                }else if(sender.hasPermission("beaconprotect.group.owner")&&args[1].equalsIgnoreCase("owner")){
                    return getGroupMembers(sender, args[2]);
                }
            }
        }
        return null;
    }
}
