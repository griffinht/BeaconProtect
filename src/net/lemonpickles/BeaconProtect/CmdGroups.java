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

public class CmdGroups implements CommandExecutor, TabCompleter {
    private BeaconProtect plugin;

    //add() and remove() and blockToCoordinates() were copied from beaconCmd
    //I could probably combine them
    //I really should make a command class
    //i hate this whole class its so long and hard to read
    private Map<String, List<String>> usages = new HashMap<>();
    CmdGroups(BeaconProtect plugin){
        this.plugin = plugin;
        List<String> list = new ArrayList<>();
        list.add("/groups - list all groups");
        list.add("/groups top - list all groups sorted by members");
        usages.put("groups", list);
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
