package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BlockDurability {
    public Block block;
    public int maxDurability;
    private int durability;
    private int defaultDurability;
    public BlockDurability(Block block, int durability){
        this.block = block;
        this.maxDurability = durability;
        this.durability = durability;
    }
    public BlockDurability(BeaconProtect plugin, Block block, Player player, boolean broken){
        this.block = block;
        this.maxDurability = plugin.defaultBlockDurabilities.getOrDefault(block.getType(), new DefaultBlockDurability(plugin.defaultBlockDurability)).getDefaultBlockDurability();
        this.durability = this.maxDurability;
        if(broken){this.durability--;}
        if(durability>1) {//only show boss bar if the block has more than 1 durability
            playerBar(plugin, player);
        }
        addToHash(this, plugin);
    }
    private void playerBar(BeaconProtect plugin, Player player) {
        BossBar bar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bar.setProgress((float) durability / maxDurability);
        bar.addPlayer(player);
        plugin.DurabilityBar.addTimedBar(bar, player, 40);
    }
    private void addToHash(BlockDurability blockDurability, BeaconProtect plugin){
        plugin.durabilities.put(blockDurability.block.getLocation(), blockDurability);
    }
    private int checkDurability(int newDurability){
        if(newDurability>maxDurability){return maxDurability;}else{return newDurability;}
    }



    public void setDurability(int newDurability){
        durability = checkDurability(newDurability);
    }
    public void changeDurability(int changeDurability){
        durability = checkDurability(durability+changeDurability);
    }
    public void setDurability(BeaconProtect plugin, Player player, int newDurability){
        durability = checkDurability(newDurability);
        playerBar(plugin, player);
    }
    public void changeDurability(BeaconProtect plugin, Player player, int changeDurability){
        durability = checkDurability(durability+changeDurability);
        playerBar(plugin, player);
    }

    public int getDurability() {
        return durability;
    }
    public int getMaxDurability(){
        return maxDurability;
    }
    public Block getBlock(){
        return block;
    }
}
