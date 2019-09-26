package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BeaconProtect extends JavaPlugin {
    public Map<Location, Block> beacons = new HashMap<>();
    public Map<Location, BlockDurability> durabilities = new HashMap<>();
    public Map<Player, BossBar> durabilityBars = new HashMap<>();
    public Map<Material, DefaultBlockDurability> defaultBlockDurabilities = new HashMap<>();
    public int defaultBlockDurability = 1;//= new DefaultBlockDurability(1,1);//set to 1 in case the config can't be read
    public DurabilityBar DurabilityBar;
    public BeaconList beaconList;
    public CustomBeacons CustomBeacons;
    public BeaconEvent beaconEvent;
    public BlockDurability BlockDurability;
    public Logger logger = getLogger();
    @Override
    public void onEnable(){
        //config
        this.saveDefaultConfig();
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }
        getConfig().options().copyDefaults(true);
        saveConfig();
        int defaultDur = getConfig().getInt("default_durability");
        defaultBlockDurability = defaultDur;
        //defaultBlockDurability.setMaxBlockDurability(defaultDur);
        logger.info("Loaded global default durability of "+defaultBlockDurability);
        //build defaultDurabilities
        //yaml with bukkit sucks
        List<String> durs = getConfig().getStringList("block_durabilities");
        for(String line:durs){
            String[] split = line.split(":",2);
            String[] split2 = split[1].split(",",2);//can have 2 args, default and max
            String mat = split[0].replaceAll("\\s","");//material
            int dur = Integer.parseInt(split2[0].replaceAll("\\s",""));//durability

            Material material = Material.getMaterial(split[0].replaceAll("\\s",""));
            if(material==null){
                logger.warning("Could not convert "+mat+" to a Bukkit material");
            }else{
                if(split2.length==2){
                    defaultBlockDurabilities.put(material, new DefaultBlockDurability(dur, Integer.parseInt(split2[1].replaceAll("\\s",""))));//max durability
                }else{
                    defaultBlockDurabilities.put(material, new DefaultBlockDurability(dur));
                }
                logger.info(material+": "+defaultBlockDurabilities.get(material).getDefaultBlockDurability()+", "+defaultBlockDurabilities.get(material).getMaxBlockDurability());
            }
        }
        logger.info("Loaded "+defaultBlockDurabilities.size()+" blocks with custom durabilities");
        //initialize
        DurabilityBar = new DurabilityBar(this);
        //initialize beacons
        beaconList = new BeaconList(this);
        beaconList.load();
        logger.info("Loaded "+beacons.size()+" beacons");
        //CustomBeacons event
        CustomBeacons = new CustomBeacons(this);
        CustomBeacons.startBeacons();
        logger.info("Started updating all beacons");
        //block events
        beaconEvent = new BeaconEvent(this);
        //commands
        getCommand("beacon").setExecutor(new BeaconCmd(this));

        //done
        logger.info("BeaconProtect has been enabled");
    }

    @Override
    public void onDisable(){
        CustomBeacons.stopBeacons();
        beaconList.save();
        getLogger().info("BeaconProtect has been disabled");
    }

}
