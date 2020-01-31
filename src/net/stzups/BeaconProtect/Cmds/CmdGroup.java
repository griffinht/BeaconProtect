package net.stzups.BeaconProtect.Cmds;

import com.sun.istack.internal.NotNull;
import net.stzups.BeaconProtect.*;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Bukkit.*;
import static org.bukkit.Material.BEACON;

public class CmdGroup extends Cmd implements CommandExecutor, TabCompleter {
    public CmdGroup(BeaconProtect plugin){
        super(plugin);
        PluginCommand pluginCommand = plugin.getCommand("group");
        if(pluginCommand!=null){
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
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
        list.add("/group promote <player> - promote a player");
        list.add("/group demote <player> - demote a player");
        usages.put("group", list);
        list = new ArrayList<>();
        list.add("/group set - set properties related to your group");
        list.add("/group set name <name> - sets a new name");
        list.add("/group set description <description> - sets a new description");
        list.add("/group set owner - sets a new owner, removing the old owner");
        usages.put("set", list);
        list = new ArrayList<>();
        list.add("Default: not much");
        list.add("Member: more");
        list.add("Trusted: Invite other players, add blocks to vaults");
        list.add("Assistant: Nearly all group permissions");
        list.add("Owner: All group permissions");
        usages.put("roles", list);
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(args.length==0) {
            if (sender instanceof Player) {
                if(sender.hasPermission("beaconprotect.group")) {
                    Map.Entry<UUID, Group> groupEntry = findGroup((Player) sender);
                    if (groupEntry != null) {
                        Group group = groupEntry.getValue();
                        sender.sendMessage(group.getName());
                        sender.sendMessage("Creation Date: "+new Date(group.getCreationDate()));
                        sender.sendMessage("Description: " + group.getDescription());
                        sender.sendMessage("Owner: " + group.getOwner().getName());
                        sender.sendMessage("Beacons: " + group.getBeaconsAsString());
                        sender.sendMessage("Members:");
                        sender.sendMessage(group.getMembersAsString());
                    } else {
                        usage(sender, "group");
                        sender.sendMessage("You must be in a group to run that command");
                        return true;
                    }
                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                return true;
            }else{sender.sendMessage("You must be a player to use that command");}
        }else if(args.length==1) {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                Map.Entry<UUID, Group> groupEntry = findGroup(player);
                if (player != null) {
                    if(groupEntry!=null) {
                        Group group = groupEntry.getValue();
                        if (args[0].equalsIgnoreCase("claimbeacon")) {
                            if(sender.hasPermission("beaconprotect.group.claimbeacon")&&group.checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)) {
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
                                        Location claimed = CustomBeacons.checkOverlap(location,plugin.groups);
                                        if(claimed==null) {
                                            group.addBeacon(location);
                                            sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been claimed to group " + group.getName());
                                        }else{
                                            System.out.println(claimed+", "+ CustomBeacons.getOwner(claimed,plugin.groups));
                                            Group group1 = CustomBeacons.getOwner(claimed,plugin.groups);
                                            if(group1!=null&&group1.checkMember(player)){
                                                group.addBeacon(location);
                                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been claimed to group " + group.getName());

                                            }else{
                                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " is too close to another claimed beacon");
                                            }
                                        }
                                    } else {
                                        sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has already been claimed to group " + claimedGroup.getName());
                                    }
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + DisplayName.materialToDisplayName(beacon.getType()) + ", maybe move closer?");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("unclaimbeacon")) {
                            if(sender.hasPermission("beaconprotect.group.unclaimbeacon")&&group.checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)) {
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
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + DisplayName.materialToDisplayName(beacon.getType()) + ", maybe move closer?");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("addvault")) {
                            if(sender.hasPermission("beaconprotect.group.addvault")&&group.checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)) {
                                Block block = player.getTargetBlock(null, 5);
                                Location location = block.getLocation();
                                boolean inRange = false;
                                for (Location beacon:CustomBeacons.checkForBlocks(block.getLocation(), plugin.beacons)) {
                                    int tier = ((Beacon)beacon.getBlock().getState()).getTier();
                                    if(location.toVector().isInAABB(new Vector(beacon.getX()-tier,beacon.getY()-tier,beacon.getZ()-tier),new Vector(beacon.getX()+tier,beacon.getY(),beacon.getZ()+tier))) {
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
                                        sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " too far from a beacon");
                                    }
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " is not a chest (found " + DisplayName.materialToDisplayName(block.getType()) + ", maybe move closer?");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("removevault")) {
                            if(sender.hasPermission("beaconprotect.group.removevault")&&group.checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)) {
                                Block block = player.getTargetBlock(null, 5);
                                Location location = block.getLocation();
                                if (group.checkVault(location)) {
                                    group.removeVault(location);
                                    sender.sendMessage("The vault at " + blockToCoordinates(block) + " has been removed from group " + group.getName());
                                } else {
                                    sender.sendMessage("The block you are looking at " + blockToCoordinates(block) + " has not been registered to group " + group.getName() + " yet");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        } else if (args[0].equalsIgnoreCase("leave")) {
                            if(sender.hasPermission("beaconprotect.group.leave")) {
                                if (!group.getOwner().getUniqueId().equals(player.getUniqueId())) {
                                    group.removeMember(player);
                                    sender.sendMessage("Left group " + group.getName());
                                } else {
                                    sender.sendMessage("You cannot leave your group as the owner. Use /group delete to delete your group instead, or make another player the owner");
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        }else if(args[0].equalsIgnoreCase("delete")) {
                            if (sender.hasPermission("beaconprotect.group.leave") && group.checkPlayerPermission((Player) sender, PlayerRole.OWNER)) {
                                if (group.getOwner().getUniqueId().equals(player.getUniqueId())) {
                                    String name = group.getName();
                                    for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {
                                        if (entry.getKey().equals(groupEntry.getKey())) {
                                            plugin.groups.remove(entry.getKey());
                                            sender.sendMessage("Deleted your group " + name);
                                            return true;
                                        }
                                    }
                                    sender.sendMessage("Could not find your group. That was not supposed to happen...");
                                } else {
                                    sender.sendMessage("You must be the owner of your group to delete it");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
                                return true;
                            }
                        }else if(args[0].equalsIgnoreCase("promote")||args[0].equalsIgnoreCase("demote")){
                            if(group.checkPlayerPermission(player, PlayerRole.ASSISTANT)&&(sender.hasPermission("beaconprotect.group.demote")&&args[0].equalsIgnoreCase("demote"))||(args[0].equalsIgnoreCase("promote")&&sender.hasPermission("beaconprotect.group.promte"))){
                                sender.sendMessage("Please specify a player");
                                usage(sender, "roles");
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        } else {
                            if(sender.hasPermission("beaconprotect.group.others")) {
                                for (Group g : plugin.groups.values()) {
                                    if (g.getName().equalsIgnoreCase(args[0])) {
                                        sender.sendMessage("Group:");
                                        sender.sendMessage("Name: " + g.getName() + ", Description: " + g.getDescription() + ", Owner: " + g.getOwner().getName() + ", Beacons: " + g.getBeaconsAsString() + ", Members: " + g.getMembersAsString());
                                        return true;
                                    }
                                }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");return true;}
                        }
                        sender.sendMessage("Incorrect argument");
                        usage(sender, "group");
                        return true;
                    }else{
                        if(args[0].equalsIgnoreCase("join")) {
                            if(sender.hasPermission("beaconprotect.group.join")) {
                                Group group = null;
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
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        }else if(args[0].equalsIgnoreCase("create")){
                            if(sender.hasPermission("beaconprotect.group.create")) {
                                sender.sendMessage("Usage: /group create <name>");
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                            return true;
                        }else{
                            usage(sender, "group");
                            sender.sendMessage("You must be in a group to run that command");
                            return true;
                        }
                    }
                } else {sender.sendMessage("Error: Could not get player (this should not happen)");return true;}
            }else{
                if(sender.hasPermission("beaconprotect.group.others")) {
                    for (Group group : plugin.groups.values()) {
                        if (group.getName().equalsIgnoreCase(args[0])) {
                            sender.sendMessage("Group:");
                            sender.sendMessage("Name: " + group.getName() + ", Creation Date: "+new Date(group.getCreationDate())+", Description: " + group.getDescription() + ", Owner: " + group.getOwner().getName() + ", Beacons: " + group.getBeaconsAsString() + ", Members: " + group.getMembersAsString());
                        }
                    }
                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                return true;
            }
        }else if(args.length==2){
            if(sender instanceof Player) {
                Player player = ((Player) sender);
                if (args[0].equalsIgnoreCase("invite")) {
                    Group group = findGroup(player).getValue();
                    if (group != null) {
                        if(sender.hasPermission("beaconprotect.group.invite")&&group.checkPlayerPermission((Player)sender, PlayerRole.TRUSTED)) {
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
                        }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                    } else {
                        sender.sendMessage("You must be in a group to use that command");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("kick")) {
                    Group group = findGroup(player).getValue();
                    if (group != null) {
                        if(sender.hasPermission("beaconprotect.group.kick")&&group.checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)) {
                            //deja vu i have seen this code before
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
                        }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                    } else {
                        sender.sendMessage("You must be in a group to use that command");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("join")) {
                    if(sender.hasPermission("beaconprotect.group.join")) {
                        for (Group group : plugin.groups.values()) {
                            if (group.getName().equalsIgnoreCase(args[1])) {
                                if (!group.checkMember(player)) {
                                    if (group.checkInvite(player)) {
                                        group.addMember(player);
                                        group.removeInvite(player);
                                        sender.sendMessage("You have joined " + group.getName());
                                        sender.sendMessage("Use /group for information about your group");
                                    } else {
                                        sender.sendMessage("You have not yet been invited to this group");
                                    }
                                    return true;
                                } else {
                                    sender.sendMessage("You are already in this group!");
                                    return true;
                                }
                            }
                        }
                        sender.sendMessage("Could not find a group named " + args[1] + ". Check /groups list");
                    }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                    return true;
                }else if(args[0].equalsIgnoreCase("create")) {
                    if (sender.hasPermission("beaconprotect.group.create")) {
                        if (findGroup(player) == null) {
                            plugin.groups.put(UUID.randomUUID(), new Group(args[1], player, plugin.customReinforce));
                            sender.sendMessage("Created a new group named " + args[1]);
                        } else {
                            sender.sendMessage("You must leave your current group before you can create a new one");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("promote")) {
                    Group group = findGroup(player).getValue();
                    if (group != null) {
                        if (sender.hasPermission("beaconprotect.group.promote") && group.checkPlayerPermission(player, PlayerRole.ASSISTANT)) {
                            Player p = Bukkit.getPlayer(args[1]);
                            if(group.checkOwner(p)){
                                sender.sendMessage("You cannot promote the owner");
                            }else if (p!=null&&group.checkMember(p)) {
                                PlayerRole role = group.getRole(p);
                                if (role == PlayerRole.ASSISTANT) {
                                    sender.sendMessage(p.getDisplayName()+" is already Assistant rank. Use /group set owner <player> to set this player as the owner");
                                } else if (role == PlayerRole.TRUSTED) {
                                    group.setRole(p, PlayerRole.ASSISTANT);
                                    sender.sendMessage("Promoted " + p.getDisplayName() + " to Assistant");
                                    p.sendMessage("You have been promoted to Assistant");
                                } else if (role == PlayerRole.MEMBER) {
                                    group.setRole(p, PlayerRole.TRUSTED);
                                    sender.sendMessage("Promoted " + p.getDisplayName() + " to Trusted");
                                    p.sendMessage("You have been promoted to Trusted");
                                } else if (role == PlayerRole.DEFAULT) {
                                    group.setRole(p, PlayerRole.MEMBER);
                                    sender.sendMessage("Promoted " + p.getDisplayName() + " to Member");
                                    p.sendMessage("You have been promoted to Member");
                                }else if(role==PlayerRole.OWNER){
                                    sender.sendMessage("You cannot promote the Owner.");
                                } else {
                                    sender.sendMessage("Error: Couldn't find role or something");
                                }
                            } else {
                                sender.sendMessage("That player is not in your group");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
                        }
                    } else {
                        sender.sendMessage("You must be in a group to run that command");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("demote")){
                    Group group = findGroup(player).getValue();
                    if(group!=null) {
                        if (sender.hasPermission("beaconprotect.group.demote") && group.checkPlayerPermission(player, PlayerRole.ASSISTANT)) {
                            Player p = Bukkit.getPlayer(args[1]);
                            if(group.checkOwner(p)){
                                sender.sendMessage("You cannot demote the owner. Use /group set owner <player> to change group owners");
                            }else if(p!=null&&group.checkMember(p)){
                                PlayerRole role = group.getRole(p);
                                if(role==PlayerRole.ASSISTANT){
                                    group.setRole(p, PlayerRole.TRUSTED);
                                    sender.sendMessage("Demoted "+p.getDisplayName()+" to Trusted");
                                    p.sendMessage("You have been demoted to Trusted");
                                }else if(role==PlayerRole.TRUSTED){
                                    group.setRole(p, PlayerRole.MEMBER);
                                    sender.sendMessage("Demoted "+p.getDisplayName()+" to Member");
                                    p.sendMessage("You have been demote to Member");
                                }else if(role==PlayerRole.MEMBER){
                                    group.setRole(p, PlayerRole.DEFAULT);
                                    sender.sendMessage("Demoted "+p.getDisplayName()+" to Default");
                                    p.sendMessage("You have been promoted to Trusted");
                                }else if(role==PlayerRole.DEFAULT) {
                                    sender.sendMessage(p.getDisplayName() + " is already Default rank. Use /group kick <player> to kick this player");
                                }else if(role==PlayerRole.OWNER){
                                    sender.sendMessage("You cannot demote the owner. Use /group set owner <player> to change group owners");
                                }else{sender.sendMessage("Error: Couldn't find role or something");}
                            }else{sender.sendMessage("That player is not in your group");}
                        } else {
                            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
                        }
                    }else{sender.sendMessage("You must be in a group to run that command");}
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
                                if(sender.hasPermission("beaconprotect.group.set.owner")&&entry.getValue().checkPlayerPermission((Player)sender, PlayerRole.OWNER)) {
                                    try {
                                        Player p = getServer().getPlayer(args[2]);
                                        Group group = plugin.groups.get(key);
                                        group.setOwner(p);
                                        group.setRole(p, PlayerRole.OWNER);
                                        group.setRole((Player)sender, PlayerRole.ASSISTANT);
                                        sender.sendMessage("Set " + args[2] + " as owner of " + name);
                                    } catch (Exception e) {
                                        sender.sendMessage("Could not find player by the name of " + args[2]);
                                    }
                            }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                                return true;
                            }else if(args[1].equalsIgnoreCase("name")&&entry.getValue().checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)){
                                if(sender.hasPermission("beaconprotect.group.set.name")) {
                                    String oldName = plugin.groups.get(key).getName();
                                    plugin.groups.get(key).setName(args[2]);
                                    sender.sendMessage("Changed name of group from " + oldName + " to " + args[2]);
                                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
                                return true;
                            }else if(args[1].equalsIgnoreCase("description")&&entry.getValue().checkPlayerPermission((Player)sender, PlayerRole.ASSISTANT)) {
                                if(sender.hasPermission("beaconprotect.group.set.description")) {
                                    StringBuilder builder = new StringBuilder();
                                    for(int i = 0; i<args.length; i++){
                                        if(i>1){
                                            if(i!=2)builder.append(" ");
                                            builder.append(args[i]);
                                        }
                                    }
                                    plugin.groups.get(key).setDescription(builder.toString());
                                    sender.sendMessage("Set description of " + name);
                                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
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
            for(String string:new String[]{"claimbeacon","unclaimbeacon","addvault","invite","set","join","leave","create","delete","promote","demote"})if(sender.hasPermission("beaconprotect.group."+string)&&checkCompletions(string,args[0]))completions.add(string);
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
                for(String string:new String[]{"name","description","owner"})if(sender.hasPermission("beaconprotect.group.set."+string)&&checkCompletions(string, args[1]))completions.add(string);
                return completions;
            }else if(sender.hasPermission("beaconprotect.group.invite")&&args[0].equalsIgnoreCase("invite")){
                return null;//online players
            }else if(sender.hasPermission("beaconprotect.group.kick")&&args[0].equalsIgnoreCase("kick")||(sender.hasPermission("beaconprotect.group.promote")&&args[0].equalsIgnoreCase("promote")||(sender.hasPermission("beaconprotect.group.demote")&&args[0].equalsIgnoreCase("demote")))) {
                return getGroupMembers(sender, args[1]);
            }else if(sender.hasPermission("beaconprotect.group.create")&&args[0].equalsIgnoreCase("create")){
                return new ArrayList<>();
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
