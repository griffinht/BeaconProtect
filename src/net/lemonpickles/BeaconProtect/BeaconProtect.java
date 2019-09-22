package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BeaconProtect extends JavaPlugin {
    public ArrayList<Location> beacons = new ArrayList<>();
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
        this.beaconList = new BeaconList(this);
        this.beaconList.load();
        logger.info("Loaded "+this.beacons.size()+" beacon(s)");
        //CustomBeacons event
        this.CustomBeacons = new CustomBeacons(this);
        this.CustomBeacons.startBeacons();
        logger.info("Started updating all beacons");
        //block events
        this.beaconEvent = new BeaconEvent(this);
        //commands
        this.getCommand("beacon").setExecutor(new BeaconCmd(this));

        //done
        logger.info("BeaconProtect has been enabled");
    }

    @Override
    public void onDisable(){
        this.CustomBeacons.stopBeacons();
        this.beaconList.save();
        getLogger().info("BeaconProtect has been disabled");
    }

}
