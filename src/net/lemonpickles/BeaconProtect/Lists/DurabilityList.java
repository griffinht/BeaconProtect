package net.lemonpickles.BeaconProtect.Lists;

import net.lemonpickles.BeaconProtect.BeaconProtect;
import net.lemonpickles.BeaconProtect.BlockDurability;
import net.lemonpickles.BeaconProtect.DefaultBlockDurability;
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
            DefaultBlockDurability defaultBlockDurability = plugin.defaultBlockDurabilities.getOrDefault(blockDurability.getBlock().getType(), plugin.defaultBlockDurability);
            if (!(defaultBlockDurability.getDefaultBlockDurability() == blockDurability.getDurability() && (blockDurability.getBeaconDurability() == blockDurability.getMaxBeaconDurability()||blockDurability.getBeaconDurability()==0))) {
                durs.add("[" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]," + blockDurability.getDurability() + "," + blockDurability.getSetDurability() + "," + blockDurability.getMaxDurability()+","+blockDurability.getBeaconDurability()+","+blockDurability.getMaxBeaconDurability());
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
            String original = rawData;
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
            String[] split = rawData.split(",");
            if(split.length==5){
                int durability = Integer.parseInt(split[0]);
                int setDurability = Integer.parseInt(split[1]);
                int maxDurability = Integer.parseInt(split[2]);
                int beaconDurability = Integer.parseInt(split[3]);
                int maxBeaconDurability = Integer.parseInt(split[4]);
                BlockDurability blockDurability = new BlockDurability(block, durability, setDurability, maxDurability, beaconDurability, maxBeaconDurability);
                plugin.durabilities.put(blockDurability.getBlock().getLocation(),blockDurability);
            }else{
                plugin.logger.warning("Could not the following line as a set block durability from disk: "+original);
            }
        }
    }
}
