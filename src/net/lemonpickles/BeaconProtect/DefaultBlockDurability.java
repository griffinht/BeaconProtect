package net.lemonpickles.BeaconProtect;

public class DefaultBlockDurability {
    private int defaultBlockDurability;
    private int maxBlockDurability;
    public DefaultBlockDurability(int defaultBlockDurability, int maxBlockDurability){
        this.defaultBlockDurability = defaultBlockDurability;
        this.maxBlockDurability = maxBlockDurability;
    }
    public DefaultBlockDurability(int defaultBlockDurability){
        this.defaultBlockDurability = defaultBlockDurability;
        this.maxBlockDurability = defaultBlockDurability;
    }
    public int getDefaultBlockDurability(){
        return defaultBlockDurability;
    }
    public int getMaxBlockDurability(){
        return maxBlockDurability;
    }
    public void setDefaultBlockDurability(int defaultBlockDurability){
        this.defaultBlockDurability = defaultBlockDurability;
    }
    public void setMaxBlockDurability(int maxBlockDurability){
        this.maxBlockDurability = maxBlockDurability;
    }
}
