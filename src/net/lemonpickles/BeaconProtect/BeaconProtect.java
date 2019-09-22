package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BeaconProtect extends JavaPlugin {
    public Map<Location, Block> beacons = new HashMap<>();
    public Map<Location, BlockDurability> durabilities = new HashMap();
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
        //initialize beacons
        beaconList = new BeaconList(this);
        beaconList.load();
        logger.info("Loaded "+beacons.size()+" beacon(s)");
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
