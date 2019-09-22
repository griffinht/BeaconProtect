package net.lemonpickles.BeaconProtect;

import net.lemonpickles.util.FileMgmt;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class BeaconList extends FileMgmt {
    private BeaconProtect plugin;
    public BeaconList(BeaconProtect plugin){
        super(plugin, "beacons.yml");
        this.plugin = plugin;
        FileConfiguration beacons = super.getConfig();
        if(super.getConfig().getList("beacons")==null){
            beacons.createSection("beacons");
        }
        super.save();
    }
    public void save(){
        ArrayList<String> locs = new ArrayList<>();
        for (Location location:this.plugin.beacons) {
            locs.add("["+location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ()+"]");
        }
        super.getConfig().set("beacons",locs);
        super.save();
    }
    public void load(){
        super.load();
        this.plugin.beacons.clear();
        //Convert each string [0,0,0] to a Location
        List<String> bacon = super.getConfig().getStringList("beacons");
        for(String line:bacon){//lol
            line = line.substring(1, line.length() - 1);
            int[] loc = new int[3];
            int i = 0;
            for(String coord:line.split(",")) {
                loc[i] = Integer.parseInt(coord.trim());
                i++;
            }
            this.plugin.beacons.add(new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]));
        }

    }
}
