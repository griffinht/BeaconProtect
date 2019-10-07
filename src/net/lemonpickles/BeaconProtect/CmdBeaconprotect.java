package net.lemonpickles.BeaconProtect;

import com.sun.istack.internal.NotNull;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import sun.util.resources.ca.LocaleNames_ca;

import java.util.*;

import static java.lang.Integer.parseInt;
import static org.bukkit.Material.BEACON;

public class CmdBeaconprotect extends Cmd implements CommandExecutor, TabCompleter {
    CmdBeaconprotect(BeaconProtect plugin){
        super(plugin);
        this.plugin = plugin;
        List<String> list = new ArrayList<>();
        list.add("/bp - commands related to managing BeaconProtect");
        list.add("/bp add <x,y,z> - add a new protection beacon if it does not exist");
        list.add("/bp remove <x,y,z> - remove a protection beacon if it does not exist");
        list.add("/bp list - lists all registered protection beacons");
        list.add("/bp stop - stops giving player effects from protection beacons");
        list.add("/bp start - starts giving player effects from protection beacons");
        list.add("/bp durability - commands related to custom block durability");
        list.add("/bp bypass - toggle bypassing BeaconProtect protections");
        usages.put("beaconprotect", list);
        list = new ArrayList<>();
        list.add("/bp durability - commands related to custom block durability");
        list.add("/bp durability list - lists all blocks with set durability");
        list.add("/bp durability size - returns the amount of blocks with a set durability");
        list.add("/bp durability clean - removes block durabilities that are default");
        usages.put("durability", list);
    }
    private boolean add(Location location){
        if(!plugin.beacons.containsKey(location)){
            plugin.beacons.put(location, location.getBlock());
            return false;
        }else{return true;}
    }
    private boolean remove(Location location){
        if(plugin.beacons.containsKey(location)){
            plugin.beacons.remove(location);
            return true;
        }else{return false;}
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("beaconprotect.bp")) {
            World world = Bukkit.getServer().getWorld("world");
            if (args.length == 0) {
                usage(sender, "beaconprotect");
                return false;
                //add
            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Block beacon = player.getTargetBlock(null, 5);
                        Location location = beacon.getLocation();

                        if (beacon.getType() == BEACON) {
                            if (!add(location)) {
                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has been registered");
                            } else {
                                sender.sendMessage("The beacon you are looking at " + blockToCoordinates(beacon) + " has already been registered");
                            }
                        } else {
                            sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ", maybe move closer?");
                        }
                    } else {
                        sender.sendMessage("You must be a player to run that command");
                    }
                } else if (args.length == 4 && world != null) {
                    try {
                        Block beacon = world.getBlockAt(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));
                        Location location = beacon.getLocation();
                        if (beacon.getType() == BEACON) {
                            if (add(location)) {
                                sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " has been registered");
                            } else {
                                sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " has been already been registered");
                            }
                        } else {
                            sender.sendMessage("The block at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ")");
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Error: please use integer coordinates");
                    }
                } else {
                    sender.sendMessage("Error: Incorrect arguments. Please use /beacon add x y z");
                }
                //remove
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Block beacon = player.getTargetBlock(null, 5);
                        Location location = beacon.getLocation();

                        if (remove(location)) {
                            sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " has been removed");
                        } else {
                            if (beacon.getType() == BEACON) {
                                sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " does not already exist");
                            } else {
                                sender.sendMessage("The block at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ")");
                            }
                        }
                    } else {
                        sender.sendMessage("You must be a player to use that command");
                    }
                } else if (args.length == 4 && world != null) {
                    Block beacon = world.getBlockAt(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));
                    Location location = beacon.getLocation();
                    if (remove(location)) {
                        sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " has been removed");
                    } else {
                        if (beacon.getType() == BEACON) {
                            sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " does not already exist");
                        } else {
                            sender.sendMessage("The block at " + blockToCoordinates(beacon) + " is not a beacon (found " + beacon.getType() + ")");
                        }
                    }
                } else {
                    sender.sendMessage("Pleased specify which beacon to remove");
                    return false;
                }
                //list
            } else if (args[0].equalsIgnoreCase("list")) {
                if (this.plugin.beacons.size() > 0) {
                    sender.sendMessage("List of registered beacons:");
                    for (Map.Entry<Location, Block> entry : plugin.beacons.entrySet()) {
                        sender.sendMessage(blockToCoordinates(entry.getValue()));
                    }
                } else {
                    sender.sendMessage("There are no currently registered beacons");
                }
            } else if (args[0].equalsIgnoreCase("stop")) {
                plugin.CustomBeacons.stopBeacons();
                sender.sendMessage("Stopped running beacons");
            } else if (args[0].equalsIgnoreCase("start")) {
                plugin.CustomBeacons.startBeacons();
                sender.sendMessage("Started running beacons");
            }else if(args[0].equalsIgnoreCase("bypass")){
                if(sender.hasPermission("beaconprotect.bp.bypass")){
                    if(sender instanceof Player){
                        Player player = (Player) sender;
                        if(plugin.bypass.contains(player)){
                            plugin.bypass.remove(player);
                            sender.sendMessage("Toggled admin bypass mode off");
                        }else{
                            plugin.bypass.add(player);
                            sender.sendMessage("Toggled admin bypass mode on");
                        }
                    }else{sender.sendMessage("You must be a player to run this command!");}
                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use this command!");}
            } else if (args[0].equalsIgnoreCase("durability") && args.length >= 2) {
                if (args[1].equalsIgnoreCase("list")) {
                    sender.sendMessage("Listing all set block durabilities (" + plugin.durabilities.size() + ")");
                    for (BlockDurability blockDurability : plugin.durabilities.values()) {
                        Block block = blockDurability.getBlock();
                        Location location = block.getLocation();
                        sender.sendMessage("[" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "] - " + block.getType() + ": " + blockDurability.getDurability() + "/" + blockDurability.getMaxDurability()+", "+blockDurability.getBeaconDurability()+"/"+blockDurability.getMaxBeaconDurability());
                    }
                } else if (args[1].equalsIgnoreCase("size")) {
                    sender.sendMessage("There are " + plugin.durabilities.size() + " blocks with a set durability");
                } else if (args[1].equalsIgnoreCase("clean")) {
                    int start = plugin.durabilities.size();
                    sender.sendMessage("Starting check durabilities " + start + " for unnecessary entries");
                    long startTime = System.currentTimeMillis();
                    for(Iterator<Map.Entry<Location, BlockDurability>> iterator = plugin.durabilities.entrySet().iterator(); iterator.hasNext();){
                        BlockDurability blockDurability = iterator.next().getValue();
                        DefaultBlockDurability defaultBlockDurability = plugin.defaultBlockDurabilities.getOrDefault(blockDurability.getBlock().getType(), plugin.defaultBlockDurability);
                        if (defaultBlockDurability.getDefaultBlockDurability() == blockDurability.getDurability() && (blockDurability.getBeaconDurability() == blockDurability.getMaxBeaconDurability()||blockDurability.getBeaconDurability()==0)) {
                            iterator.remove();
                        }
                    }
                    sender.sendMessage("Removed " + (start - plugin.durabilities.size()) + " of " + start + " entries ("+(System.currentTimeMillis()-startTime)+"ms)");
                } else {
                    usage(sender, "durability");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("groups")) {
                sender.sendMessage("Groups:");
                for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {
                    Group g = entry.getValue();
                    OfflinePlayer a = g.getOwner();
                    String owner;
                    if (a == null) {
                        owner = "";
                    } else {
                        owner = a.getName();
                    }
                    sender.sendMessage(g.getName());
                    sender.sendMessage("Owner: " + owner);
                    sender.sendMessage("Description: " + g.getDescription());
                    sender.sendMessage("Beacons: " + g.getBeaconsAsString());
                    sender.sendMessage("Vaults: " + g.getVaultsAsString());
                    sender.sendMessage("Members: " + g.getMembersAsString());
                }
            } else if (args[0].equalsIgnoreCase("durability")) {
                usage(sender, "durability");
            } else {
                usage(sender, "beaconprotect");
            }
        }else{
            sender.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args){
        if(sender.hasPermission("beaconprotect.bp")){
            List<String> completions = new ArrayList<>();
            if(args.length==1){
                if(checkCompletions("add", args[0])) {completions.add("add");}
                if(checkCompletions("remove", args[0])) {completions.add("remove");}
                if(checkCompletions("list", args[0])) {completions.add("list");}
                if(checkCompletions("stop", args[0])) {completions.add("stop");}
                if(checkCompletions("start", args[0])) {completions.add("start");}
                if(checkCompletions("durability", args[0])) {completions.add("durability");}
                if(checkCompletions("bypass", args[0])){completions.add("bypass");}
                return completions;
            }else if(args.length==2){
                if(args[0].equalsIgnoreCase("durability")){
                    if(checkCompletions("list", args[1])) {completions.add("list");}
                    if(checkCompletions("size", args[1])) {completions.add("size");}
                    if(checkCompletions("clean", args[1])) {completions.add("clean");}
                    return completions;
                }else if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    completions.add("x y z");
                    return completions;
                }
            }else if(args.length==3){
                if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    completions.add("y z");
                    return completions;
                }
            }else if(args.length==4){
                if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    completions.add("z");
                    return completions;
                }
            }
        }
        return null;
    }
}
