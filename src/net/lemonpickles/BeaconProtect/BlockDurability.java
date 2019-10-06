package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
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
    public BlockDurability(Block block, int durability, int setDurability, int maxDurability, int beaconDurability){//for loading BlockDurability from disk
        this.block = block;
        this.durability = durability;
        this.setDurability = setDurability;
        this.maxDurability = maxDurability;
        this.beaconDurability = beaconDurability;
    }
    BlockDurability(BeaconProtect plugin, Block block, Player player, int changeDur){
        this.block = block;
        DefaultBlockDurability defaultDur = plugin.defaultBlockDurabilities.getOrDefault(block.getType(), plugin.defaultBlockDurability);
        this.maxDurability = defaultDur.getMaxBlockDurability();
        int defaultDurability = defaultDur.getDefaultBlockDurability();
        this.durability = defaultDurability;
        this.setDurability = defaultDurability;
        changeDurability(plugin, player, changeDur, false);
        if(durability>1||plugin.CustomBeacons.getMaxPenalty(player, block)>0&&beaconDurability>0) {//only show boss bar if the block has more than 1 durability OR the beacon is hostile for the player
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

    private void playerBar(BeaconProtect plugin, Player player, boolean reinforcing){
        DefaultBlockDurability a = plugin.defaultBlockDurability;
        if(plugin.defaultBlockDurabilities.containsKey(block.getType())){
            a = plugin.defaultBlockDurabilities.get(block.getType());
        }
        //this if statement runs if the block is at default durability and set durability
        //a.getMaxBlockDurability()==maxDurability&& is extra
        boolean isTheBeaconActiveForThePlayer = plugin.CustomBeacons.getMaxPenalty(player, block)>0;
        if((!(a.getDefaultBlockDurability()==durability&&a.getDefaultBlockDurability()==setDurability))||isTheBeaconActiveForThePlayer||!plugin.CustomBeacons.checkFriendly(player, block)){
            BarColor color;
            if(plugin.CustomBeacons.checkFriendly(player, block)){
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
        if(newDurability>maxDurability){return maxDurability;}else{return newDurability;}
    }

    private int setBeaconDurability(int newDurability, int maxBeaconPenalty){//beacon hit is a maximum, beacondurability is mostly just durability
        //System.out.println(" ");
        //System.out.println("old durability: "+durability+", max durability: "+maxDurability+", new durability: "+newDurability+", max beacon penalty "+maxBeaconPenalty+", old beacon durability "+beaconDurability);
        int changeDurability = newDurability-durability;
        int maxBeaconDurability = beaconDurability;
        beaconDurability = beaconDurability+changeDurability;
        if(beaconDurability>maxBeaconDurability){
            //System.out.println("beacondur is over maxbeacondur");
            int change = maxBeaconDurability-beaconDurability;
            beaconDurability = beaconDurability+change;//xtra is lost
        }
        int importantChange = 0;
        if(beaconDurability<0){
            //System.out.println("beacondur is under 0");
            importantChange = importantChange-beaconDurability;
            beaconDurability = beaconDurability+importantChange;
            durability = durability-importantChange;
        }
        if(changeDurability>0) {//for reinforcing
            //System.out.println("changedur is over 0");
            durability = checkDurability(durability+changeDurability);
        }
        //TODO test beacon durabilities and block breakage
        //System.out.println("new durability: "+durability+", beacon durability "+beaconDurability);
        //System.out.println(" ");
        return maxBeaconDurability-beaconDurability;//return the change in blocks that need to be removed
    }

    private int setDurability(int newDurability, boolean setDurability, int maxBeaconPenalty){
        int returnVal = 0;
        if(maxBeaconPenalty>0){//can be zero if player is friendly
            returnVal = setBeaconDurability(checkDurability(newDurability), maxBeaconPenalty);
        }else{durability = checkDurability(newDurability);}
        if(setDurability&&durability>this.setDurability){
            this.setDurability = durability;
        }//setDurability should only be changed when a block is reinforced not broken
        return returnVal;
    }





    boolean changeDurability(BeaconProtect plugin, Player player, int changeDurability, boolean setDurability){
        if(beaconDurability==-69){beaconDurability = plugin.CustomBeacons.getMaxDurability(block);}//initializer value
        if(changeDurability>0){//reinforcing so reset beacon durability
            beaconDurability = plugin.CustomBeacons.getMaxDurability(block);
        }
        int value = setDurability(durability+changeDurability, setDurability, plugin.CustomBeacons.getMaxPenalty(player, block));
        for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){
            Group group = entry.getValue();
            if(group.checkForBlock(block)){//this might break with overlapping beacons
                group.removeMaterialInVaults(block.getType(), value);
            }
        }
        if(changeDurability>0) {
            playerBar(plugin, player, true);
        }else{
            playerBar(plugin, player, false);
        }
        return changeDurability==durability;
    }


    public int getDurability(){
        return durability;
    }
    public int getBeaconDurability(){return beaconDurability;}
    public int getMaxDurability(){return maxDurability;}
    public int getSetDurability(){return setDurability;}
    public Block getBlock(){return block;}
}
