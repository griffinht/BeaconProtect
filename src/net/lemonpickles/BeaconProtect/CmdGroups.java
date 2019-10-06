package net.lemonpickles.BeaconProtect;

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
        if(args.length==0) {
            sender.sendMessage("Groups:");
            for (Group group : plugin.groups.values()) {
                sender.sendMessage(group.getName()+": "+group.getMembers().size());
            }
            return true;
        }
        usage(sender, "groups");
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