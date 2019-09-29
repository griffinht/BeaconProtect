package net.lemonpickles.BeaconProtect;

import net.lemonpickles.util.FileMgmt;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class DurabilityList extends FileMgmt {
    private BeaconProtect plugin;
    public DurabilityList(BeaconProtect plugin){
        super(plugin, "durabilities.yml");
        this.plugin = plugin;
        FileConfiguration beacons = super.getConfig();
        if(super.getConfig().getList("durabilities")==null){
            beacons.createSection("durabilities");
        }
        super.save();
    }
    public void save(){
        ArrayList<String> durs = new ArrayList<>();
        for(Map.Entry<Location, BlockDurability> entry:plugin.durabilities.entrySet()){
            Location location = entry.getKey();
            BlockDurability blockDurability = entry.getValue();
            DefaultBlockDurability a = plugin.defaultBlockDurabilities.get(blockDurability.getBlock().getType());
            if(!(a!=null&&a.getDefaultBlockDurability()==blockDurability.getDurability())){
                durs.add("[" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]," + blockDurability.getDurability() + "," + blockDurability.getSetDurability() + "," + blockDurability.getMaxDurability());
            }
        }
        super.getConfig().set("durabilities",durs);
        super.save();
    }
    public void load(){
        super.load();
        plugin.durabilities.clear();
        //deserialize
        for(String rawData:super.getConfig().getStringList("durabilities")){
            rawData = rawData.replaceAll("\\s","");//remove spaces
            int open = rawData.indexOf("[");
            int close = rawData.indexOf("]");

            //find location
            String locs = rawData.substring(open+1,close);
            int[] loc = new int[3];
            int i = 0;
            for(String split:locs.split(",")) {
                loc[i] = Integer.parseInt(split);
                i++;
            }
            Block block = new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]).getBlock();
            rawData = rawData.substring(0,open)+rawData.substring(close+2);//trim out location [0,0,0] from rawData

            //find ints
            String[] split = rawData.split(",",3);
            int durability = Integer.parseInt(split[0]);
            int setDurability = Integer.parseInt(split[1]);
            int maxDurability = Integer.parseInt(split[2]);

            BlockDurability blockDurability = new BlockDurability(block, durability, setDurability, maxDurability);
            plugin.durabilities.put(blockDurability.getBlock().getLocation(),blockDurability);
        }
    }
}
