package net.lemonpickles.util;

import net.lemonpickles.BeaconProtect.BeaconProtect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;


public class FileMgmt {
    protected BeaconProtect plugin;
    protected File file;
    protected FileConfiguration config;

    public FileMgmt(BeaconProtect plugin, String fileName) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), fileName);
        if(!file.exists()){
            try{
                if(file.createNewFile()){plugin.logger.info("Created "+fileName);}else{plugin.logger.info("Could not create "+fileName);}
            }catch(IOException e){
                plugin.logger.warning("Could not create "+fileName);
                e.printStackTrace();
            }

        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }
    public void save(){
        try {
            this.config.save(file);
        }catch(IOException e){
            this.plugin.logger.warning("Could not save file to disk");
            e.printStackTrace();
        }
    }
    public void load(){
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}
