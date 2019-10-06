package net.lemonpickles.BeaconProtect.Lists;

import net.lemonpickles.BeaconProtect.BeaconProtect;
import net.lemonpickles.BeaconProtect.Group;
import net.lemonpickles.BeaconProtect.Member;
import net.lemonpickles.util.FileMgmt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;


import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class GroupList extends FileMgmt {
    private BeaconProtect plugin;
    public GroupList(BeaconProtect plugin) {
        super(plugin, "groups.yml");
        this.plugin = plugin;
        FileConfiguration beacons = super.getConfig();
        if(super.getConfig().get("groups")==null){
            beacons.createSection("groups");
        }
        super.save();
    }
    public void save(){
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
            String path = "groups."+entry.getKey();
            Group value = entry.getValue();
            config.createSection(path);
            //config.createSection(path+".name");
            config.set(path+".name", value.getName());
            //config.createSection(path+".owner");
            if(value.getOwner()!=null) {
                config.set(path + ".owner", value.getOwner().getUniqueId().toString());
            }
            //config.createSection(path+".description");
            config.set(path+".description", value.getDescription());
            //config.createSection(path+".members");
            if(value.getMembers()!=null) {
                for (Map.Entry<OfflinePlayer, Member> membersEntry : value.getMembers().entrySet()) {
                    config.set(path + ".members." + membersEntry.getKey().getUniqueId().toString(), membersEntry.getValue().role);
                }
            }
            if(value.getBeacons()!=null) {
                List<String> cfg = new ArrayList<>();
                for(Location location:value.getBeacons()){
                    cfg.add("["+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+"]");
                }
                config.set(path + ".beacons", cfg);
            }
            if(value.getVaults()!=null){
                List<String> cfg = new ArrayList<>();
                for(Location location:value.getVaults()){
                    cfg.add("["+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+"]");
                }
                config.set(path+".vaults", cfg);
            }
        }
        super.save();
    }
    public void load(){
        super.load();
        plugin.groups.clear();
        //i hate this
        //lol it works
        //is it overcomplicated?
        //TODO also i should probably check for nulls and clean up variable names
        //and maybe actually do some stuff with bukkit's serialization
        for(Map.Entry<String, Object> entry1:config.getValues(false).entrySet()){
            if(entry1.getValue() instanceof MemorySection){
                MemorySection memorySection1 = (MemorySection) entry1.getValue();
                for(Map.Entry<String, Object> entry2:memorySection1.getValues(false).entrySet()){
                    if(entry2.getValue() instanceof MemorySection){
                        MemorySection memorySection2 = (MemorySection) entry2.getValue();
                        String name = memorySection2.getString("name");
                        String ownerStr = memorySection2.getString("owner");
                        OfflinePlayer owner = null;
                        if(ownerStr!=null){
                            try{
                                owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerStr));
                            }catch(IllegalArgumentException e){
                                //lol whatever its kind of handled below
                            }
                            if(owner==null){
                                plugin.logger.warning("Could not find owner of "+name+" from UUID: "+ownerStr);
                            }
                        }
                        String description = memorySection2.getString("description");

                        Map<OfflinePlayer, Member> members = new HashMap<>();
                        ConfigurationSection a = memorySection2.getConfigurationSection("members");
                        if (a != null) {
                            for(Map.Entry<String, Object> entry3:a.getValues(false).entrySet()){
                                OfflinePlayer player = null;
                                try{
                                    player = Bukkit.getOfflinePlayer(UUID.fromString(entry3.getKey()));
                                }catch(IllegalArgumentException e){
                                    //also whatever
                                }
                                if(player!=null){
                                    members.put(player, new Member(player, entry3.getValue().toString()));
                                }else{
                                    plugin.logger.warning("Could not find member of "+name+" from UUID: "+entry3.getKey());
                                }
                            }
                        }

                        List<Location> beacons = new ArrayList<>();
                        for(String string:memorySection2.getStringList("beacons")){
                            string = string.substring(1, string.length() - 1);
                            int[] loc = new int[3];
                            int i = 0;
                            for(String coord:string.split(",")) {
                                loc[i] = Integer.parseInt(coord.trim());
                                i++;
                            }
                            beacons.add(new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]));
                        }

                        List<Location> vaults = new ArrayList<>();
                        for(String string:memorySection2.getStringList("vaults")){
                            string = string.substring(1, string.length() - 1);
                            int[] loc = new int[3];
                            int i = 0;
                            for(String coord:string.split(",")) {
                                loc[i] = Integer.parseInt(coord.trim());
                                i++;
                            }
                            vaults.add(new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]));
                        }

                        plugin.groups.put(UUID.fromString(entry2.getKey()), new Group(name, description, owner, members, beacons, vaults, plugin.defaultBeaconRange));
                    }
                }
            }
        }
    }
}
