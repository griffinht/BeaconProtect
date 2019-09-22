package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static java.lang.Integer.parseInt;
import static org.bukkit.Material.BEACON;

public class BeaconCmd implements CommandExecutor {
    private BeaconProtect plugin;
    public BeaconCmd(BeaconProtect plugin){
        this.plugin = plugin;
    }
    private String blockToCoordinates(Block block){
        return "("+block.getX()+", "+block.getY()+", "+block.getZ()+")";
    }
    private boolean add(Location location){
        if(!this.plugin.beacons.contains(location)){
            this.plugin.beacons.add(location);
            return false;
        }else{return true;}
    }
    private boolean remove(Location location){
        if(this.plugin.beacons.contains(location)){
            this.plugin.beacons.remove(location);
            return true;
        }else{return false;}
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        World world = Bukkit.getServer().getWorld("world");
        if (args.length == 0) {
            sender.sendMessage("Not enough arguments");
            return false;
        //add
        }else if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 1) {
                if(sender instanceof Player) {
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
                }else{sender.sendMessage("You must be a player to run that command");}
            } else if (args.length == 4) {
                try {
                    Block beacon = world.getBlockAt(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));
                    Location location = beacon.getLocation();
                    if (beacon.getType() == BEACON) {
                        if(add(location)) {
                            sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " has been registered");
                        }else{
                            sender.sendMessage("The beacon at " + blockToCoordinates(beacon) + " has been already been registered");
                        }
                    } else {
                        sender.sendMessage("The block at "+blockToCoordinates(beacon)+" is not a beacon (found " + beacon.getType() + ")");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Error: please use integer coordinates");
                }
            } else {
                sender.sendMessage("Error: Incorrect arguments. Please use /beacon add x y z");
            }
        //remove
        } else if(args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                if(sender instanceof Player) {
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
                }else{sender.sendMessage("You must be a player to use that command");}
            } else if (args.length == 4) {
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
        }else if(args[0].equalsIgnoreCase("list")) {
            if(this.plugin.beacons.size()>0) {
                sender.sendMessage("List of registered beacons:");
            for (Location location : this.plugin.beacons) {
                sender.sendMessage(blockToCoordinates(location.getBlock()));
            }
        }else{sender.sendMessage("There are no currently registered beacons");}
        }else if(args[0].equalsIgnoreCase("loadBeacons")) {
            this.plugin.beaconList.load();
            sender.sendMessage("Beacons have been loaded from disk");
        }else if(args[0].equalsIgnoreCase("saveBeacons")) {
            this.plugin.beaconList.save();
            sender.sendMessage("Beacons have been saved to disk");
        }else if(args[0].equalsIgnoreCase("stop")) {
            this.plugin.CustomBeacons.stopBeacons();
            sender.sendMessage("Stopped running beacons");
        }else if(args[0].equalsIgnoreCase("start")){
            this.plugin.CustomBeacons.startBeacons();
            sender.sendMessage("Started running beacons");
        }else {
            sender.sendMessage("Incorrect argument");
            return false;
        }
        return true;
    }
}
