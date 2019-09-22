package net.lemonpickles.util;

import net.lemonpickles.BeaconProtect.BeaconProtect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;


public class FileMgmt {
    private BeaconProtect plugin;//protected?
    private File file;
    protected FileConfiguration config;

    public FileMgmt(BeaconProtect plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
        if(!this.file.exists()){
            try{
                this.file.createNewFile();
                this.plugin.logger.info("Created "+fileName);
            }catch(IOException e){
                this.plugin.logger.warning("Could not create "+fileName);
                System.out.println(e);
            }

        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }
    public void save(){
        try {
            this.config.save(this.file);
        }catch(IOException e){
            this.plugin.logger.warning("Could not save file to disk");
            System.out.println(e);
        }
    }
    public void load(){
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig(){
        return config;
    }

    public File getFile(){
        return file;
    }
}
