package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockDurability {
    private Block block;
    private int maxDurability;
    private int durability;
    private int setDurability;
    private List<Location> beacons = new ArrayList<>();//TODO:handle new and old beacons
    public BlockDurability(Block block, int durability, int setDurability, int maxDurability){//for loading BlockDurability from disk
        this.block = block;
        this.durability = durability;
        this.setDurability = setDurability;
        this.maxDurability = maxDurability;
    }
    public BlockDurability(BeaconProtect plugin, Block block, Player player, int changeDur){
        this.block = block;
        DefaultBlockDurability defaultDur = plugin.defaultBlockDurabilities.getOrDefault(block.getType(), plugin.defaultBlockDurability);
        this.maxDurability = defaultDur.getMaxBlockDurability();
        int defaultDurability = defaultDur.getDefaultBlockDurability();
        this.durability = defaultDurability;
        this.setDurability = defaultDurability;
        changeDurability(changeDur);
        beacons = plugin.CustomBeacons.checkForBlocks(block);
        if(durability>1) {//only show boss bar if the block has more than 1 durability
            if(changeDur>0){
                playerBar(plugin, player, true);
            }else {
                playerBar(plugin, player, false);
            }
        }else{
            if(plugin.durabilityBars.containsKey(player)){
                plugin.durabilityBars.get(player).removeAll();
            }
        }
        addToHash(this, plugin);
    }

    private void playerBar(BeaconProtect plugin, Player player, boolean reinforcing) {
        DefaultBlockDurability a = plugin.defaultBlockDurability;
        if(plugin.defaultBlockDurabilities.containsKey(block.getType())){
            a = plugin.defaultBlockDurabilities.get(block.getType());
        }
        if(!(a.getDefaultBlockDurability()==durability&&a.getMaxBlockDurability()==maxDurability&&a.getDefaultBlockDurability()==setDurability)){
            BossBar bar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
            int b = setDurability;
            if(reinforcing){b=maxDurability;}
            float bossDur = (float) durability / b;
            if (bossDur > 1) {
                bossDur = 1;
            }
            bar.setProgress(bossDur);
            bar.addPlayer(player);
            plugin.DurabilityBar.addTimedBar(bar, player, 40);
        }else{
            if(plugin.durabilityBars.containsKey(player)){
                plugin.durabilityBars.get(player).removeAll();
            }
        }
    }
    private void addToHash(BlockDurability blockDurability, BeaconProtect plugin){
        plugin.durabilities.put(blockDurability.block.getLocation(), blockDurability);
    }
    private int checkDurability(int newDurability){
        if(newDurability>maxDurability){return maxDurability;}else{return newDurability;}
    }
    private int takeFromBeacons(int amount){
        for(Location location:beacons){
            //check beacon if it has inventory
        }
        return amount;
    }



    private boolean setDurability(int newDurability, boolean setDurability){
        int oldDur = durability;
        durability = takeFromBeacons(checkDurability(newDurability));
        if(setDurability&&durability>this.setDurability){
            this.setDurability = durability;
        }//durability should only be set when a block is reinforced not broken
        return oldDur!=durability;
    }
    private boolean changeDurability(int changeDurability){
        return setDurability(durability+changeDurability, false);
    }



    public boolean setDurability(BeaconProtect plugin, Player player, int newDurability, boolean setDurability){
        boolean value = setDurability(newDurability, setDurability);
        playerBar(plugin, player, false);
        return value;
    }
    public boolean changeDurability(BeaconProtect plugin, Player player, int changeDurability, boolean setDurability){
        boolean value = setDurability(durability+changeDurability, setDurability);
        if(changeDurability>0) {
            playerBar(plugin, player, true);
        }else{
            playerBar(plugin, player, false);
        }
        return value;
    }

    public int getDurability(){return durability;}
    public int getMaxDurability(){return maxDurability;}
    public int getSetDurability(){return setDurability;}
    public Block getBlock(){return block;}
}
