package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockDurability {
    public Block block;
    public int durability;
    private int defaultDurabilty = 5;
    public BlockDurability(Block block, int durability, boolean broken){
        this.block = block;
        int a = 0;
        if(broken){a++;}
        this.durability = durability;
    }
    public BlockDurability(Block block, boolean broken){
        this.block = block;
        int a = 0;
        if(broken){a++;}
        this.durability = defaultDurabilty-a;
    }

    public int getDurability() {
        return durability;
    }

    public Block getBlock(){
        return block;
    }
}
