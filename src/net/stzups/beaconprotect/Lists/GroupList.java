package net.stzups.beaconprotect.Lists;

import net.stzups.beaconprotect.BeaconProtect;
import net.stzups.beaconprotect.Group;
import net.stzups.beaconprotect.PlayerRole;
import net.stzups.util.bukkit.FileManagement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;


import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class GroupList extends FileManagement {
    private BeaconProtect plugin;

    public GroupList(BeaconProtect plugin) {
        super(plugin, "groups.yml");
        this.plugin = plugin;
        if (config.get("groups") == null) {
            config.createSection("groups");
        }
        super.save();
    }

    public void save() {
        config.set("groups", null);//clear old config values TODO backup system
        for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {
            String path = "groups." + entry.getKey();
            Group value = entry.getValue();
            config.createSection(path);
            config.set(path + ".name", value.getName());
            if (value.getOwner() != null) {
                config.set(path + ".owner", value.getOwner().getUniqueId().toString());
            }
            config.set(path + ".description", value.getDescription());
            config.set(path + ".creationDate", value.getCreationDate());
            if (value.getMembers() != null) {
                for (Map.Entry<OfflinePlayer, PlayerRole> membersEntry : value.getMembers().entrySet()) {
                    config.set(path + ".members." + membersEntry.getKey().getUniqueId().toString(), membersEntry.getValue().toString());
                }
            }
            if (value.getBeacons() != null) {
                List<String> cfg = new ArrayList<>();
                for (Location location : value.getBeacons()) {
                    cfg.add("[" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]");
                }
                config.set(path + ".beacons", cfg);
            }
            if (value.getVaults() != null) {
                List<String> cfg = new ArrayList<>();
                for (Location location : value.getVaults()) {
                    cfg.add("[" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]");
                }
                config.set(path + ".vaults", cfg);
            }
        }
        super.save();
    }

    public void load() {
        super.load();
        plugin.groups.clear();
        //maybe actually do some stuff with bukkit's serialization
        for (Map.Entry<String, Object> entry1 : config.getValues(false).entrySet()) {
            if (entry1.getValue() instanceof MemorySection) {
                MemorySection memorySection1 = (MemorySection) entry1.getValue();
                for (Map.Entry<String, Object> entry2 : memorySection1.getValues(false).entrySet()) {
                    if (entry2.getValue() instanceof MemorySection) {
                        MemorySection memorySection2 = (MemorySection) entry2.getValue();
                        String name = memorySection2.getString("name");
                        String ownerStr = memorySection2.getString("owner");
                        OfflinePlayer owner = null;
                        if (ownerStr != null) {
                            try {
                                owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerStr));
                            } catch (IllegalArgumentException e) {
                                //lol whatever its kind of handled below
                            }
                            if (owner == null) {
                                plugin.logger.warning("Could not find owner of " + name + " from UUID: " + ownerStr);
                            }
                        }
                        String description = memorySection2.getString("description");
                        long date = memorySection2.getLong("creationDate");
                        if (date == 0) plugin.logger.warning("Group " + name + " has a date of " + new Date(date));
                        Map<OfflinePlayer, PlayerRole> members = new HashMap<>();
                        ConfigurationSection a = memorySection2.getConfigurationSection("members");
                        if (a != null) {
                            for (Map.Entry<String, Object> entry3 : a.getValues(false).entrySet()) {
                                OfflinePlayer player = null;
                                try {
                                    player = Bukkit.getOfflinePlayer(UUID.fromString(entry3.getKey()));
                                } catch (IllegalArgumentException e) {
                                    //also whatever
                                }
                                if (player != null) {
                                    String playerR = entry3.getValue().toString();
                                    PlayerRole role = null;
                                    if (playerR.equalsIgnoreCase("DEFAULT")) {
                                        role = PlayerRole.DEFAULT;
                                    } else if (playerR.equalsIgnoreCase("MEMBER")) {
                                        role = PlayerRole.MEMBER;
                                    } else if (playerR.equalsIgnoreCase("TRUSTED")) {
                                        role = PlayerRole.TRUSTED;
                                    } else if (playerR.equalsIgnoreCase("ASSISTANT")) {
                                        role = PlayerRole.ASSISTANT;
                                    } else if (playerR.equalsIgnoreCase("OWNER")) {
                                        role = PlayerRole.OWNER;
                                    } else {
                                        plugin.logger.warning("Could not convert " + playerR + " to an enumerated role, will use Default");
                                    }
                                    if (role == null) {
                                        role = PlayerRole.DEFAULT;
                                    }
                                    members.put(player, role);
                                } else {
                                    plugin.logger.warning("Could not find member of " + name + " from UUID: " + entry3.getKey());
                                }
                            }
                        }

                        List<Location> beacons = new ArrayList<>();
                        for (String string : memorySection2.getStringList("beacons")) {
                            string = string.substring(1, string.length() - 1);
                            int[] loc = new int[3];
                            int i = 0;
                            for (String coord : string.split(",")) {
                                loc[i] = Integer.parseInt(coord.trim());
                                i++;
                            }
                            beacons.add(new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]));
                        }
                        List<Location> vaults = new ArrayList<>();
                        for (String string : memorySection2.getStringList("vaults")) {
                            string = string.substring(1, string.length() - 1);
                            int[] loc = new int[3];
                            int i = 0;
                            for (String coord : string.split(",")) {
                                loc[i] = Integer.parseInt(coord.trim());
                                i++;
                            }
                            vaults.add(new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]));
                        }
                        plugin.groups.put(UUID.fromString(entry2.getKey()), new Group(name, description, owner, members, beacons, vaults, plugin.customReinforce, date));
                    }
                }
            }
        }
    }
}
