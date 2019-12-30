package net.lemonpickles.BeaconProtect.Cmds;

import com.sun.istack.internal.NotNull;
import net.lemonpickles.BeaconProtect.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

import static java.lang.Integer.parseInt;
import static org.bukkit.Material.BEACON;

public class CmdBeaconprotect extends Cmd implements CommandExecutor, TabCompleter {
    public CmdBeaconprotect(BeaconProtect plugin){
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
        list.add("/bp durability defaultlist - lists all blocks with set default durability");
        list.add("/bp durability size - returns the amount of blocks with a set durability");
        list.add("/bp durability clean - removes block durabilities that are default");
        usages.put("durability", list);
        list = new ArrayList<>();
        list.add("/bp group - commands related to group management");
        list.add("/bp group clean - removes groups with no members or owner");
        list.add("/bp group size - returns amount of currently registered groups");
        usages.put("group", list);
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
                            sender.sendMessage("The block you are looking at " + blockToCoordinates(beacon) + " is not a beacon (found "+ DisplayName.materialToDisplayName(beacon.getType()) + ", maybe move closer?");
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
                            sender.sendMessage("The block at " + blockToCoordinates(beacon) + " is not a beacon (found " + DisplayName.materialToDisplayName(beacon.getType()) + ")");
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
                                sender.sendMessage("The block at " + blockToCoordinates(beacon) + " is not a beacon (found " +DisplayName.materialToDisplayName(beacon.getType())+ ")");
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
                            sender.sendMessage("The block at " + blockToCoordinates(beacon) + " is not a beacon (found " +DisplayName.materialToDisplayName(beacon.getType()) + ")");
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
                    }else{sender.sendMessage("You must be a player to run this command");}
                }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");}
            } else if (args[0].equalsIgnoreCase("durability") && args.length >= 2) {
                if (args[1].equalsIgnoreCase("list")) {
                    sender.sendMessage("Listing all set block durabilities (" + plugin.durabilities.size() + ")");
                    String[] msg = new String[plugin.durabilities.size()];
                    int i = 0;
                    for (BlockDurability blockDurability : plugin.durabilities.values()) {
                        Block block = blockDurability.getBlock();
                        Location location = block.getLocation();
                        msg[i] = ("[" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "] - " + DisplayName.materialToDisplayName(block.getType()) + ": " + blockDurability.getDurability() + "/" + blockDurability.getMaxDurability() + ", " + blockDurability.getBeaconDurability() + "/" + blockDurability.getMaxBeaconDurability());
                        i++;
                    }
                    sender.sendMessage(msg);
                } else if (args[1].equalsIgnoreCase("defaultlist")) {
                    sender.sendMessage("Listing all set default block durabilities (" + plugin.defaultBlockDurabilities.size() + ")");
                    sender.sendMessage("Global default block durability is "+plugin.defaultBlockDurability.getDefaultBlockDurability()+", "+plugin.defaultBlockDurability.getMaxBlockDurability());
                    String[] msg = new String[plugin.defaultBlockDurabilities.size()];
                    int i = 0;
                    for (Map.Entry<Material, DefaultBlockDurability> defaultBlockDurability : plugin.defaultBlockDurabilities.entrySet()) {
                        msg[i] = (defaultBlockDurability.getKey()+": "+defaultBlockDurability.getValue().getDefaultBlockDurability()+", "+defaultBlockDurability.getValue().getMaxBlockDurability());
                        i++;
                    }
                    sender.sendMessage(msg);
                } else if (args[1].equalsIgnoreCase("size")) {
                    sender.sendMessage("There are " + plugin.durabilities.size() + " blocks with a set durability");
                } else if (args[1].equalsIgnoreCase("clean")) {
                    int start = plugin.durabilities.size();
                    sender.sendMessage("Starting check durabilities " + start + " for unnecessary entries");
                    long startTime = System.currentTimeMillis();
                    plugin.durabilityList.clean();
                    sender.sendMessage("Removed " + (start - plugin.durabilities.size()) + " of " + start + " entries (" + (System.currentTimeMillis() - startTime) + "ms)");
                } else {
                    sender.sendMessage("Unknown argument. Use /bp durability for help");
                }
                return true;
            }else if(args[0].equalsIgnoreCase("group")&&args.length>=2){
                if(args[1].equalsIgnoreCase("clean")){
                    int start = plugin.groups.size();
                    sender.sendMessage("Starting check groups " + start + " for unnecessary entries");
                    long startTime = System.currentTimeMillis();
                    for (Iterator<Map.Entry<UUID, Group>> iterator = plugin.groups.entrySet().iterator(); iterator.hasNext(); ) {
                        Group group = iterator.next().getValue();
                        if (group.getOwner()==null||group.getMembersSize()==0) {
                            iterator.remove();
                        }
                    }
                    sender.sendMessage("Removed " + (start - plugin.groups.size()) + " of " + start + " entries (" + (System.currentTimeMillis() - startTime) + "ms)");
                }else if(args[1].equalsIgnoreCase("size")){
                    sender.sendMessage("There are " + plugin.groups.size() + " registered groups");
                }else{
                    sender.sendMessage("Unknown argument. Use /bp group for help");
                }
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
            }else if(args[0].equalsIgnoreCase("group")) {
                usage(sender, "group");
            } else {
                sender.sendMessage("Unknown argument. Use /bp for help");
                usage(sender, "beaconprotect");
            }
        }else{
            sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args){
        if(sender.hasPermission("beaconprotect.bp")){
            List<String> completions = new ArrayList<>();
            if(args.length==1){
                for(String string:new String[]{"add","remove","list","stop","start","durability","bypass","group"})if(checkCompletions(string,args[0]))completions.add(string);
                return completions;
            }else if(args.length==2){
                if(args[0].equalsIgnoreCase("durability")) {
                    for (String string : new String[]{"list", "size", "clean","defaultlist"})if (checkCompletions(string, args[1])) completions.add(string);
                    return completions;
                }else if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    completions.add("0 0 0");
                    return completions;
                }else if(args[0].equalsIgnoreCase("group")){
                    for(String string:new String[]{"clean","size"})if(checkCompletions(string,args[1]))completions.add(string);
                    return completions;
                }
            }else if(args.length==3){
                if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    completions.add("0 0");
                    return completions;
                }
            }else if(args.length==4){
                if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    completions.add("0");
                    return completions;
                }
            }
        }
        return null;
    }
}
