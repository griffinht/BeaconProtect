package net.stzups.beaconprotect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class BlockDurability {
    private Block block;
    private int maxDurability;
    private int durability;
    private int setDurability;
    private int beaconDurability = -69;//this is bad
    private int maxBeaconDurability;//may not be accurate, its only set by the last time CustomBeacons was checked

    private Material material;

    public BlockDurability(Block block, int durability, int setDurability, int maxDurability, int beaconDurability, int maxBeaconDurability){//for loading BlockDurability from disk
        this.block = block;
        this.durability = durability;
        this.setDurability = setDurability;
        this.maxDurability = maxDurability;
        this.beaconDurability = beaconDurability;
        this.maxBeaconDurability = maxBeaconDurability;
    }
    BlockDurability(BeaconProtect plugin, Block block, Player player, int changeDur){
        blockDurability(plugin,block,player,changeDur,true);
    }
    BlockDurability(BeaconProtect plugin, Block block, Player player, int changeDur, boolean useBeaconDurability){
        blockDurability(plugin,block,player,changeDur,useBeaconDurability);
    }
    //actual constructor here
    private void blockDurability(BeaconProtect plugin, Block block, Player player, int changeDur, boolean useBeaconDurability){
        this.block = block;
        DefaultBlockDurability defaultDur = plugin.defaultBlockDurabilities.getOrDefault(block.getType(), plugin.defaultBlockDurability);
        this.maxDurability = defaultDur.getMaxBlockDurability();
        int defaultDurability = defaultDur.getDefaultBlockDurability();
        this.durability = defaultDurability;
        this.setDurability = defaultDurability;
        material = block.getType();
        if(useBeaconDurability){
            changeDurability(plugin, player, changeDur, false);
        }else {
            setDurability(durability-1,true,0);
        }
        if(player!=null) {
            if (!(durability > 1 || plugin.CustomBeacons.getMaxPenalty(player, block) > 0) && plugin.durabilityBars.containsKey(player)) {//only show boss bar if the block has more than 1 durability OR the beacon is hostile for the player
                plugin.durabilityBars.get(player).removeAll();
            }
        }
        if(durability!=0){addToHash(this, plugin);}
    }

    private void playerBar(BeaconProtect plugin, Player player, boolean reinforcing){
        DefaultBlockDurability a = plugin.defaultBlockDurability;
        if(plugin.defaultBlockDurabilities.containsKey(block.getType())){
            a = plugin.defaultBlockDurabilities.get(block.getType());
        }
        //this if statement runs if the block is at default durability and set durability
        //a.getMaxBlockDurability()==maxDurability&& is extra
        boolean isTheBeaconActiveForThePlayer = plugin.CustomBeacons.getMaxPenalty(player, block)>0;
        if((!(a.getDefaultBlockDurability()==durability&&a.getDefaultBlockDurability()==setDurability))||isTheBeaconActiveForThePlayer||!CustomBeacons.checkFriendly(player, block.getLocation(), plugin.groups)){
            BarColor color;
            if(CustomBeacons.checkFriendly(player, block.getLocation(), plugin.groups)){
                color = BarColor.WHITE;
            }else{
                if(isTheBeaconActiveForThePlayer&&beaconDurability>0){
                    color = BarColor.PURPLE;//purple if the player has to break through beacon durability
                }else{
                    color = BarColor.RED;
                }
            }
            BossBar bar = Bukkit.createBossBar("", color, BarStyle.SOLID);
            int b = setDurability;
            if(reinforcing){b=maxDurability;}
            float bossDur = (float) durability / b;
            if (bossDur > 1) {
                bossDur = 1;
            }else if(bossDur<0){
                plugin.logger.warning("Tried to set the bossbar for "+player+" to "+bossDur+" (can only be between 0 and 1)");
                bossDur = 0;
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
        return Math.min(newDurability, maxDurability);
    }

    private int setBeaconDurability(int newDurability){//beacon hit is a maximum, beacondurability is mostly just durability
        int changeDurability = newDurability-durability;
        int maxBeaconDurability = beaconDurability;
        beaconDurability = beaconDurability+changeDurability;
        if(beaconDurability>maxBeaconDurability){
            int change = maxBeaconDurability-beaconDurability;
            beaconDurability = beaconDurability+change;//xtra is lost
        }
        int importantChange = 0;
        if(beaconDurability<0){
            importantChange = importantChange-beaconDurability;
            beaconDurability = beaconDurability+importantChange;
            durability = durability-importantChange;
        }
        if(changeDurability>0) {//for reinforcing
            durability = checkDurability(durability+changeDurability);
        }
        return maxBeaconDurability-beaconDurability;//return the change in blocks that need to be removed
    }

    private int setDurability(int newDurability, boolean setDurability, int maxBeaconPenalty){
        int returnVal = 0;
        if(maxBeaconPenalty>0){//can be zero if player is friendly
            returnVal = setBeaconDurability(checkDurability(newDurability));
        }else{durability = checkDurability(newDurability);}
        if(setDurability&&durability>this.setDurability){
            this.setDurability = durability;
        }//setDurability should only be changed when a block is reinforced not broken
        return returnVal;
    }




    boolean changeDurability(BeaconProtect plugin, Player player, int changeDurability, boolean setDurability, boolean useBeaconDurability){
        int oldDurability = durability;
        if(beaconDurability==-69){beaconDurability = CustomBeacons.getMaxDurability(block.getLocation(), plugin.beacons)*this.setDurability;maxBeaconDurability=beaconDurability;}//initializer value
        if(changeDurability>0){//reinforcing so reset beacon durability
            beaconDurability = CustomBeacons.getMaxDurability(block.getLocation(), plugin.beacons);
        }
        int maxPenalty;
        if(useBeaconDurability){
            maxPenalty = plugin.CustomBeacons.getMaxPenalty(player,block);
        }else{
            maxPenalty = 0;
        }
        int value = setDurability(durability+changeDurability, setDurability, maxPenalty);
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
            Group group = entry.getValue();
            if(CustomBeacons.checkForBlocks(block.getLocation(), plugin.beacons).size()>0){
                Material material = block.getType();
                group.removeMaterialInVaults(material, value, plugin.defaultBlockDurabilities.getOrDefault(material,plugin.defaultBlockDurability).getDefaultBlockDurability());
                break;
            }
        }
        if(player!=null) {
            if (changeDurability > 0) {
                playerBar(plugin, player, true);
            } else {
                playerBar(plugin, player, false);
            }
        }
        return (changeDurability+oldDurability)==durability;
    }
    boolean changeDurability(BeaconProtect plugin, Player player, int changeDurability, boolean setDurability){
        return changeDurability(plugin,player,changeDurability,setDurability,true);
    }
    public void setMaterial(){material = block.getType();}
    public int getMaxBeaconDurability(){return maxBeaconDurability;}
    public int getDurability(){
        return durability;
    }
    public int getBeaconDurability(){return beaconDurability;}
    public int getMaxDurability(){return maxDurability;}
    public int getSetDurability(){return setDurability;}
    public Block getBlock(){return block;}
    public Material getMaterial(){return material;}
}
