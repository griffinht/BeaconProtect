package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BeaconEvent implements Listener{
    private BeaconProtect plugin;
    public BeaconEvent(BeaconProtect plugin){
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }




    @EventHandler
    public void blockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(!this.plugin.CustomBeacons.checkForBlocks(block)){
            if(block.getType()==Material.BEACON){
                Location location = block.getLocation();
                if(!this.plugin.beacons.contains(location)){
                    this.plugin.beacons.add(location);
                    String msg = "The beacon at " +block.getX()+", "+block.getY()+", "+block.getZ() + " has been registered";
                    player.sendMessage(msg);
                    this.plugin.logger.info(msg);
                }
            }
        }else {
            event.setCancelled(true);
            player.sendMessage("You cannot place here! This area is protected by a beacon");
        }
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent event){
        if(event.getHand()== EquipmentSlot.HAND&&event.getAction()== Action.RIGHT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            if(block!=null) {
                if (block.getType() == Material.CHEST) {
                    Player player = event.getPlayer();
                    if (this.plugin.CustomBeacons.checkForBlocks(block)) {
                        event.setCancelled(true);
                        player.sendMessage("You cannot interact here! This area is protected by a beacon");
                    }
                }
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(this.plugin.CustomBeacons.checkForBlocks(block)) {
            if (this.plugin.durabilities.containsKey(block.getLocation())) {
                this.plugin.durabilities.get(block.getLocation()).durability--;
                if (this.plugin.durabilities.get(block.getLocation()).durability > 0) {
                    player.sendMessage("This block has " + this.plugin.durabilities.get(block.getLocation()).durability + " durability remaining");
                    event.setCancelled(true);
                    return;
                }
            } else {//block durability hasnt been broken yet
                this.plugin.durabilities.put(block.getLocation(), new BlockDurability(block, true));
                if (this.plugin.durabilities.get(block.getLocation()).durability > 0) {
                    player.sendMessage("This block has " + this.plugin.durabilities.get(block.getLocation()).durability + " durability remaining");
                    event.setCancelled(true);
                    return;
                }
            }
        }
        //normal block breakage
        if (block.getType() == Material.BEACON) {
            Location location = block.getLocation();
            if (this.plugin.beacons.contains(location)) {
                this.plugin.beacons.remove(location);
                String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                player.sendMessage(msg);
                this.plugin.logger.info(msg);
            }
        }
    }
}
