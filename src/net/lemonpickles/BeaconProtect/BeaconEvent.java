package net.lemonpickles.BeaconProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
        if(!plugin.CustomBeacons.checkForBlocks(block)){
            if(block.getType()==Material.BEACON){
                Location location = block.getLocation();
                if(!plugin.beacons.containsKey(location)){
                    plugin.beacons.put(location, block);
                    String msg = "The beacon at " +block.getX()+", "+block.getY()+", "+block.getZ() + " has been registered";
                    player.sendMessage(msg);
                    plugin.logger.info(msg);
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
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if(block!=null) {
                if (block.getType() == Material.CHEST) {
                    if (plugin.CustomBeacons.checkForBlocks(block)) {
                        event.setCancelled(true);
                        player.sendMessage("You cannot interact here! This area is protected by a beacon");
                    }
                }
            }
        }else if(event.getAction()== Action.LEFT_CLICK_BLOCK){
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if(block!=null) {
                if(!plugin.durabilities.containsKey(block.getLocation())){
                    new BlockDurability(plugin, block, player,false);
                }else{plugin.durabilities.get(block.getLocation()).changeDurability(plugin, player,0);}
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(this.plugin.CustomBeacons.checkForBlocks(block)) {//block durability hasn't been broken yet
            if (!plugin.durabilities.containsKey(block.getLocation())) {
                new BlockDurability(plugin, block, player,true);
            } else {//block has a durability, take away from it
                plugin.durabilities.get(block.getLocation()).changeDurability(plugin,player, -1);
            }
            if (plugin.durabilities.get(block.getLocation()).getDurability() > 0) {
                //BlockDurability dur = plugin.durabilities.get(block.getLocation());
                //player.sendMessage("This block has " + dur.durability + " durability remaining");
                event.setCancelled(true);
                return;
            }else{
                plugin.durabilityBars.get(player).removeAll();
                plugin.durabilities.remove(block.getLocation());
            }
        }
        //normal block breakage
        if (block.getType() == Material.BEACON) {
            Location location = block.getLocation();
            if (plugin.beacons.containsKey(location)) {
                plugin.beacons.remove(location);
                String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                player.sendMessage(msg);
                plugin.logger.info(msg);
            }
        }
    }
}
