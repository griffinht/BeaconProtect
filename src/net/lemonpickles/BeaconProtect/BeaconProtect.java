package net.lemonpickles.BeaconProtect;

import net.lemonpickles.BeaconProtect.Cmds.CmdBeaconprotect;
import net.lemonpickles.BeaconProtect.Cmds.CmdGroup;
import net.lemonpickles.BeaconProtect.Cmds.CmdGroups;
import net.lemonpickles.BeaconProtect.Lists.BeaconList;
import net.lemonpickles.BeaconProtect.Lists.DurabilityList;
import net.lemonpickles.BeaconProtect.Lists.GroupList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class BeaconProtect extends JavaPlugin {
    public Map<Location, Block> beacons = new HashMap<>();
    public Map<Location, BlockDurability> durabilities = new HashMap<>();
    Map<Player, BossBar> durabilityBars = new HashMap<>();
    public Map<Material, DefaultBlockDurability> defaultBlockDurabilities = new HashMap<>();
    public Map<UUID, Group> groups = new HashMap<>();
    public DefaultBlockDurability defaultBlockDurability = new DefaultBlockDurability(1,1);//set to 1 (like vanilla minecraft) in case the config can't be read
    private GroupList groupList;
    DurabilityBar DurabilityBar;
    private BeaconList beaconList;
    public CustomBeacons CustomBeacons;
    public DurabilityList durabilityList;
    public Logger logger = getLogger();
    public List<Player> bypass = new ArrayList<>();//list of admins currently in bypass mode
    private FileConfiguration config;
    boolean interactProtectBlacklist = true;//default value to protect all blocks from interaction in case config breaks
    Map<Material,Boolean> interactProtection = new HashMap<>();
    public Map<Material,Map<Material,Integer>> customReinforce = new HashMap<>();
    private Map<String,List<Material>> alternates = new HashMap<>();
    public boolean ready = false;//used so no blocks are broken before ready in BeaconEvent
    @Override
    public void onEnable(){
        long start = System.currentTimeMillis();
        //config
        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists()){
            try{
                if(file.createNewFile()){logger.info("Created config.yml");}else{logger.info("Could not create config.yml");}
                InputStream inputStream = getResource("config.yml");
                if(inputStream==null){
                    logger.warning("Could not load default config from plugin to config.yml");
                }else{
                    FileUtils.copyToFile(inputStream, new File(getDataFolder(),"config.yml"));
                }
            }catch(IOException e){
                logger.warning("Could not create config.yml");
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        //defaults
        defaultBlockDurability = new DefaultBlockDurability(getConfig().getInt("default_durability"), getConfig().getInt("default_max_durability"));
        //defaultBlockDurability.setMaxBlockDurability(defaultDur);
        logger.info("Loaded global default durability of "+defaultBlockDurability.getDefaultBlockDurability()+" and max durability of "+defaultBlockDurability.getMaxBlockDurability());

        //yaml with bukkit sucks
        //probably because i don't know how to use it

        //load custom reinforce alternates from config
        List<String> alts = config.getStringList("alternates");
        for(String line:alts){
            line = line.replaceAll("\\s","");
            String[] split = line.split(":",2);
            if(split.length!=2){
                logger.warning("Could not parse "+line+" for alternates in config");
                break;
            }
            List<Material> materials = new ArrayList<>();
            String[] split2 = split[1].split(",");
            for(String string:split2){
                Material mat = Material.getMaterial(string);
                if(mat==null){
                    logger.warning("Could not convert "+string+" to a Bukkit material");
                }else{
                    materials.add(mat);
                }
            }
            alternates.put(split[0],materials);
        }

        //load default block durabilities
        List<String> durs = getConfig().getStringList("block_durabilities");
        for(String line:durs){
            line = line.replaceAll("\\s","");
            String[] split = line.split(":",2);
            if(split.length!=2){
                logger.warning("Could not parse "+line+" for block_durabilities in config");
                break;
            }
            String[] split2 = split[1].split(",",2);//can have 2 args, default and max
            String mat = split[0];//material
            int dur = Integer.parseInt(split2[0]);//durability

            Material material = Material.getMaterial(mat);
            if(material==null){
                if(alternates.containsKey(split[0])){
                    for(Material material1:alternates.get(split[0])){
                        if(split2.length==2){
                            defaultBlockDurabilities.put(material1, new DefaultBlockDurability(dur, Integer.parseInt(split2[1])));//max durability
                        }else{
                            defaultBlockDurabilities.put(material1, new DefaultBlockDurability(dur, defaultBlockDurability.getMaxBlockDurability()));
                        }
                    }
                }else {
                    logger.warning("Could not convert " + split[0] + " to a Bukkit material");
                }
            }else{
                if(split2.length==2){
                    defaultBlockDurabilities.put(material, new DefaultBlockDurability(dur, Integer.parseInt(split2[1])));//max durability
                }else{
                    defaultBlockDurabilities.put(material, new DefaultBlockDurability(dur, defaultBlockDurability.getMaxBlockDurability()));
                }
            }
        }
        logger.info("Loaded "+defaultBlockDurabilities.size()+" materials with a default durability");

        //load interact protection from config
        interactProtectBlacklist = config.getBoolean("interact_protect.blacklist");
        List<String> protections = config.getStringList("interact_protect.list");
        List<String> unlockProtections = config.getStringList("interact_protect.unlockable");
        for(String line:protections){
            line = line.replaceAll("\\s","");
            Material material = Material.getMaterial(line);
            if(material==null) {
                if(alternates.containsKey(line)){
                    for(Material material1:alternates.get(line)){
                        interactProtection.put(material1,false);
                    }
                }else {
                    logger.warning("Could not convert " + line + " to a Bukkit material");
                }
            }else{interactProtection.put(material,false);}
        }
        for(String line:unlockProtections){
            line = line.replaceAll("\\s","");
            Material material = Material.getMaterial(line);
            if(material==null){
                if(alternates.containsKey(line)){
                    for(Material material1:alternates.get(line)){
                        interactProtection.put(material1,true);
                    }
                }else {
                    logger.warning("Could not convert " + line + " to a Bukkit material");
                }
            }else{interactProtection.put(material,true);}
        }
        logger.info("Loaded "+interactProtection.size()+" blocks to protect from interaction");

        //load custom reinforce from config
        List<String> reinforces = config.getStringList("custom_reinforce");
        for(String line:reinforces){
            line = line.replaceAll("\\s","");//remove spaces
            String[] split = line.split(":",2);
            if(split.length!=2){
                logger.warning("Could not parse "+line+" for custom_reinforce in config");
                break;
            }
            String[] split2 = split[1].split(",");
            Map<Material, Integer> mats = new HashMap<>();
            for(String string:split2){
                String[] split3 = string.split(":",2);
                if(split3.length!=2){
                    logger.warning("Could not parse "+line+" for custom_reinforce in config");
                    break;
                }
                Material material = Material.getMaterial(split3[0]);
                try{
                    int inty=Integer.parseInt(split3[1]);
                    if(material==null){
                        if(alternates.containsKey(split3[0])){
                            for(Material material1:alternates.get(split3[0])){
                                mats.put(material1,inty);
                            }
                        }else{
                            logger.warning("Could not convert " + split3[0] + " to a Bukkit material");
                        }
                    }else{mats.put(material,inty);}
                }catch(NumberFormatException e){
                    logger.warning("Could not parse "+split3[1]+" as an integer");
                }
            }
            for(String string:split[0].split(",")){
                Material material = Material.getMaterial(string);
                if(material!=null){
                    customReinforce.put(material,mats);
                }else{
                    logger.warning("Could not convert "+string+" to a Bukkit material");
                }
            }
        }
        logger.info("Loaded "+customReinforce.size()+" custom reinforce values");
        //initialize
        DurabilityBar = new DurabilityBar(this);
        //initialize object
        beaconList = new BeaconList(this);
        durabilityList = new DurabilityList(this);
        groupList = new GroupList(this);
        //load from file
        logger.info("Loading beacons, block durabilities, and groups from disk");
        long a = System.nanoTime();

        beaconList.load();
        long b = System.nanoTime();
        logger.info("Loaded "+beacons.size()+" beacons in "+((b-a)/1000000)+"ms");

        durabilityList.load();
        long c = System.nanoTime();
        logger.info("Loaded "+durabilities.size()+" block durabilities in "+((c-b)/1000000)+"ms");

        groupList.load();
        logger.info("Loaded "+groups.size()+" groups in "+((System.nanoTime()-c)/1000000)+"ms");

        //CustomBeacons event
        CustomBeacons = new CustomBeacons(this);
        CustomBeacons.startBeacons();
        logger.info("Started updating all beacons");
        //block events
        new BeaconEvent(this);
        //commands
        new CmdBeaconprotect(this);
        new CmdGroup(this);
        new CmdGroups(this);
        //done
        logger.info("BeaconProtect has been enabled ("+(System.currentTimeMillis()-start)+"ms)");
    }

    @Override
    public void onDisable(){
        for(Map.Entry<Player, BossBar> entry:durabilityBars.entrySet()){//remove all active boss bars for durability
            entry.getValue().removeAll();
        }
        CustomBeacons.stopBeacons();
        logger.info("Saving beacons, block durabilities, and groups to disk");
        long a = System.nanoTime();

        beaconList.save();
        long b = System.nanoTime();
        logger.info("Saved "+beacons.size()+" beacons in "+((b-a)/1000000)+"ms");

        durabilityList.save();
        long c = System.nanoTime();
        logger.info("Cleaned and saved "+durabilities.size()+" block durabilities in "+((c-b)/1000000)+"ms");

        groupList.save();
        logger.info("Saved "+groups.size()+" groups in "+((System.nanoTime()-c)/1000000)+"ms");

        getLogger().info("BeaconProtect has been disabled");
    }
    public FileConfiguration getConfig(){return config;}
}
