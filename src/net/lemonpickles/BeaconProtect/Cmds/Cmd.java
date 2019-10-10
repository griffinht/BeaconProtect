package net.lemonpickles.BeaconProtect.Cmds;

import net.lemonpickles.BeaconProtect.BeaconProtect;
import net.lemonpickles.BeaconProtect.Group;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Cmd {
    protected BeaconProtect plugin;
    Cmd(BeaconProtect plugin){
        this.plugin = plugin;
    }
    Map<String, List<String>> usages = new HashMap<>();
    String blockToCoordinates(Block block){
        return "("+block.getX()+", "+block.getY()+", "+block.getZ()+")";
    }
    void usage(CommandSender sender, String usage){
        sender.sendMessage("Usage for "+usage);
        for(String string:usages.get(usage)){
            sender.sendMessage(string);
        }
    }
    Group findGroup(Player player){
        for(Group group:plugin.groups.values()){
            if(group.checkMember(player)){return group;}
        }
        return null;
    }
    boolean checkCompletions(String a, String arg){
        if(a.length()<arg.length()){return false;}
        return a.substring(0,arg.length()).equalsIgnoreCase(arg);
    }
    List<String> getGroupMembers(CommandSender sender, String arg){
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
