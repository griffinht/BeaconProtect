package net.stzups.beaconprotect.Lists;

import net.stzups.beaconprotect.BeaconProtect;
import net.stzups.util.bukkit.FileManagement;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class BeaconList extends FileManagement {
    private BeaconProtect plugin;

    public BeaconList(BeaconProtect plugin) {
        super(plugin, "beacons.yml");
        this.plugin = plugin;
        if (config.getList("beacons") == null) {
            config.createSection("beacons");
        }
        super.save();
    }

    public void save() {
        ArrayList<String> locs = new ArrayList<>();
        for (Map.Entry<Location, Block> entry : plugin.beacons.entrySet()) {
            Location location = entry.getKey();
            locs.add("[" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]");
        }
        config.set("beacons", locs);
        super.save();
    }

    public void load() {
        super.load();
        plugin.beacons.clear();
        //Convert each string [0,0,0] to a Location
        for (String rawData : config.getStringList("beacons")) {
            rawData = rawData.substring(1, rawData.length() - 1);
            int[] loc = new int[3];
            int i = 0;
            for (String coord : rawData.split(",")) {
                loc[i] = Integer.parseInt(coord.trim());
                i++;
            }
            Location location = new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]);
            plugin.beacons.put(location, location.getBlock());
        }
    }
}
