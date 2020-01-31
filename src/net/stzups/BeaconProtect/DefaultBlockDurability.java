package net.stzups.BeaconProtect;

public class DefaultBlockDurability {
    private int defaultBlockDurability;
    private int maxBlockDurability;
    DefaultBlockDurability(int defaultBlockDurability, int maxBlockDurability){
        this.defaultBlockDurability = defaultBlockDurability;
        this.maxBlockDurability = maxBlockDurability;
    }
    DefaultBlockDurability(int defaultBlockDurability){
        this.defaultBlockDurability = defaultBlockDurability;
        this.maxBlockDurability = defaultBlockDurability;
    }
    public int getDefaultBlockDurability(){
        return defaultBlockDurability;
    }
    public int getMaxBlockDurability(){
        return maxBlockDurability;
    }
}
