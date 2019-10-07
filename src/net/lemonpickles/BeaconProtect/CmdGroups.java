package net.lemonpickles.BeaconProtect;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;

public class CmdGroups extends Cmd implements CommandExecutor, TabCompleter {

    //add() and remove() and blockToCoordinates() were copied from beaconCmd
    //I could probably combine them
    //I really should make a command class
    //i hate this whole class its so long and hard to read
    CmdGroups(BeaconProtect plugin){
        super(plugin);
        List<String> list = new ArrayList<>();
        list.add("/groups - list all groups");
        list.add("/groups top - list all groups sorted by members");
        usages.put("groups", list);
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("beaconprotect.groups")) {
            if (args.length == 0) {
                sender.sendMessage("Groups:");
                for (Group group : plugin.groups.values()) {
                    sender.sendMessage(group.getName() + ": " + group.getMembers().size());
                }
                return true;
            }
            usage(sender, "groups");
            return true;
        }else{sender.sendMessage(ChatColor.RED+"You do not have permission to do that command!");}
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
        if(sender.hasPermission("beaconprotect.groups")) {
            List<String> completions = new ArrayList<>();
            return completions;
        }
        return null;
    }
}
