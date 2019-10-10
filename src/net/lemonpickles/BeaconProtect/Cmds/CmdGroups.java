package net.lemonpickles.BeaconProtect.Cmds;

import com.sun.istack.internal.NotNull;
import net.lemonpickles.BeaconProtect.BeaconProtect;
import net.lemonpickles.BeaconProtect.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.List;

public class CmdGroups extends Cmd implements CommandExecutor, TabCompleter {
    public CmdGroups(BeaconProtect plugin){
        super(plugin);
        List<String> list = new ArrayList<>();
        list.add("/groups - list all groups");
        list.add("/groups top <method> - list all groups sorted by a certain method");
        list.add("Valid sort methods are by members");
        usages.put("groups", list);
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull  String[] args) {
        if(sender.hasPermission("beaconprotect.groups")) {
            if (args.length == 0) {
                sender.sendMessage("Groups:");
                for (Group group : plugin.groups.values()) {
                    sender.sendMessage(group.getName() + ": " + group.getMembersSize());
                }
                return true;
            }else if(args.length==1){
                if (args[0].equalsIgnoreCase("top")) {
                    sender.sendMessage("Please specify how to sort groups");
                    return true;
                }
            }else if(args.length==2){
                if(args[0].equalsIgnoreCase("top")) {
                    if(args[1].equalsIgnoreCase("members")) {
                        sender.sendMessage("Groups by Members:");
                        List<Group> groups = new ArrayList<>(plugin.groups.values());
                        groups.sort(Comparator.comparing(Group::getMembersSize));
                        Collections.reverse(groups);
                        for (Group group : groups) {
                            sender.sendMessage(group.getName() + ": " + group.getMembersSize());
                        }
                    }else{sender.sendMessage("Can't sort groups by "+args[1]+". Valid methods are by members");}
                    return true;
                }
            }
            usage(sender, "groups");
            return true;
        }else{sender.sendMessage(ChatColor.RED+"You do not have permission to use that command");}
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args){
        if(sender.hasPermission("beaconprotect.groups")) {
            List<String> completions = new ArrayList<>();
            if(args.length==1){
                if(checkCompletions("top",args[0])){completions.add("top");}
            }else if(args.length==2){
                if(args[0].equalsIgnoreCase("top")){
                    if(checkCompletions("members",args[1])){completions.add("members");}
                }
            }
            return completions;
        }
        return null;
    }
}
