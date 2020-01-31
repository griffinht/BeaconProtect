package net.stzups.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;


public class FileManagement {
    private JavaPlugin plugin;
    protected File file;
    protected FileConfiguration config;

    public FileManagement(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        if(!plugin.getDataFolder().exists())if(!plugin.getDataFolder().mkdir())plugin.getLogger().warning("Could not create plugin data folder");
        file = new File(plugin.getDataFolder(), fileName);
        if(!file.exists()){
            try{
                if(file.createNewFile()){plugin.getLogger().info("Created "+fileName);}else{plugin.getLogger().info("Could not create "+fileName);}
            }catch(IOException e){
                plugin.getLogger().warning("Could not create "+fileName);
                e.printStackTrace();
            }

        }
        config = YamlConfiguration.loadConfiguration(file);
    }
    public void save(){
        try {
            config.save(file);
        }catch(IOException e){
            plugin.getLogger().warning("Could not save file to disk");
            e.printStackTrace();
        }
    }
    public void load(){
        config = YamlConfiguration.loadConfiguration(file);
    }
}
