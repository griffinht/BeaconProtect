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
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class BeaconEvent implements Listener{
    private BeaconProtect plugin;
    BeaconEvent(BeaconProtect plugin){
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void blockPlace(BlockPlaceEvent event){
        if(!plugin.bypass.contains(event.getPlayer())) {
            Block block = event.getBlock();
            Player player = event.getPlayer();
            if (CustomBeacons.checkFriendly(player, block, plugin.groups)) {
                if (block.getType() == Material.BEACON) {
                    Location location = block.getLocation();
                    if (!plugin.beacons.containsKey(location)) {
                        plugin.beacons.put(location, block);
                        String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been registered";
                        String msg2 = "";
                        for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {//add beacon if player is in a group
                            if (entry.getValue().checkMember(player)) {
                                entry.getValue().addBeacon(location);
                                msg2 = " to group " + entry.getValue().getName();
                            }
                        }
                        msg = msg + msg2;
                        if(player.hasPermission("beaconprotect.admin")){
                            player.sendMessage(msg);
                        }
                        plugin.logger.info(msg);
                    }
                }
            } else {
                event.setCancelled(true);
                player.sendMessage("You cannot place here! This area is protected by a beacon.");
            }
        }
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent event){
        if(!plugin.bypass.contains(event.getPlayer())) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            ItemStack stack = player.getInventory().getItemInMainHand();
            boolean reinforce = player.isSneaking() && !stack.getType().isAir();//everything but air works
            if (plugin.isReinforcing.contains(player) && !reinforce) {
                plugin.isReinforcing.remove(player);
                player.sendMessage("Left block reinforce mode.");
                event.setCancelled(true);
            } else if (event.getHand() == EquipmentSlot.HAND && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (block != null&&plugin.interactProtection.containsKey(block.getType())) {
                    if (!CustomBeacons.checkFriendly(player, block, plugin.groups)) {
                        if(plugin.interactProtection.get(block.getType())){
                            if(!plugin.durabilities.containsKey(block.getLocation())){
                                new BlockDurability(plugin, block, player, 0);
                            }
                            BlockDurability a = plugin.durabilities.get(block.getLocation());
                            int dur = a.getDurability()+Math.min(plugin.CustomBeacons.getMaxPenalty(player, block),a.getBeaconDurability());
                            if(dur!=1){
                                event.setCancelled(true);
                                player.sendMessage("You cannot interact here! "+(dur-1)+" hits to unlock.");
                            }//otherwise you are good
                        }else{
                            event.setCancelled(true);
                            player.sendMessage("You cannot interact here! This block is protected by a beacon.");
                            infoClick(block, player);
                        }
                    }
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (reinforce) {
                    if (plugin.isReinforcing.contains(player) && block != null) {//in reinforce mode
                        Material blockType = block.getType();
                        Map<Material,Integer> materials = plugin.customReinforce.get(blockType);
                        Material stackType = stack.getType();
                        boolean yes = false;
                        boolean alsoYes = false;
                        int reinforceAmt = 1;
                        int stackAmt = 1;
                        int playerStackAmt = stack.getAmount();
                        if(materials!=null&&materials.containsKey(stackType)){
                            int matAmt = materials.get(stackType);
                            if(matAmt<0){//negative so needs it
                                if(Math.abs(matAmt)<playerStackAmt){
                                    yes = true;
                                    stackAmt = matAmt;
                                }else{
                                    player.sendMessage("You need "+(Math.abs(matAmt)-playerStackAmt)+" more "+DisplayName.materialToDisplayName(stackType));
                                }
                            }else{
                                reinforceAmt = Math.abs(matAmt);
                            }
                        }else{alsoYes=true;}//could be a block not on materials list
                        if(yes||stackType==blockType){//this is a warning in intellij but it is necessary
                            BlockDurability blockDur;
                            if (!plugin.durabilities.containsKey(block.getLocation())) {
                                blockDur = new BlockDurability(plugin, block, player, 0);
                            } else {
                                blockDur = plugin.durabilities.get(block.getLocation());
                            }
                            if (blockDur.changeDurability(plugin, player, reinforceAmt, true)) {//changedur returns true if there was change, so if locks must be removed from invent
                                stack.setAmount(stack.getAmount()-Math.abs(stackAmt));
                            } else {
                                player.sendMessage("This block cannot be reinforced anymore.");
                            }
                        } else if(yes||alsoYes){//doesnt have the right block thing
                            String msg;
                            if(materials!=null) {
                                StringBuilder mats = new StringBuilder();
                                int lastIndex = 0;
                                for (Material material : materials.keySet()) {
                                    lastIndex = mats.length()-1;
                                    mats.append(DisplayName.materialToDisplayName(material)).append(", ");
                                }
                                msg = mats.toString().substring(0,mats.length() - 2);
                                if(materials.keySet().size()>1) {
                                    msg = msg.substring(0, lastIndex) + " or" + msg.substring(lastIndex);
                                }
                            }else{msg = DisplayName.materialToDisplayName(blockType);}
                            player.sendMessage("You must use " + msg + " to reinforce this block");
                        }
                    } else {//set to reinforce mode
                        plugin.isReinforcing.add(player);
                        player.sendMessage("Entered block reinforce mode. Shift+Punch a block to reinforce.");
                    }
                } else if (plugin.isReinforcing.contains(player)) {
                    plugin.isReinforcing.remove(player);
                    player.sendMessage("Left block reinforce mode.");//no need to cancel event here
                } else if (block != null) {//info click
                    infoClick(block, player);
                }
            }
        }
    }
    private void infoClick(Block block, Player player){
        if (!plugin.durabilities.containsKey(block.getLocation())) {
            new BlockDurability(plugin, block, player, 0);
        } else {
            plugin.durabilities.get(block.getLocation()).changeDurability(plugin, player, 0, false);
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(!plugin.bypass.contains(event.getPlayer())) {
            if (!plugin.durabilities.containsKey(block.getLocation())) {//block durability hasn't been set yet
                new BlockDurability(plugin, block, player, -1);
            } else {//block has a durability, take away from it
                plugin.durabilities.get(block.getLocation()).changeDurability(plugin, player, -1, false);
            }
            if (plugin.durabilities.containsKey(block.getLocation())) {//the block could have been broken up there
                if (plugin.durabilities.get(block.getLocation()).getDurability() > 0) {
                    event.setCancelled(true);
                    return;
                } else {
                    plugin.durabilityBars.get(player).removeAll();
                    plugin.durabilities.remove(block.getLocation());
                }
            }
        }
        //normal block breakage
        if (block.getType() == Material.BEACON) {
            Location location = block.getLocation();
            if (plugin.beacons.containsKey(location)) {
                for(Map.Entry<UUID, Group> entry:plugin.groups.entrySet()){//remove from group ownership too
                    if(entry.getValue().checkBeacon(location)){
                        entry.getValue().removeBeacon(location);
                    }
                }
                plugin.beacons.remove(location);
                String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                if(player.hasPermission("beaconprotect.admin")){
                    player.sendMessage(msg);
                }
                plugin.logger.info(msg);
            }
        }else if(block.getType()==Material.CHEST){
            Location location = block.getLocation();
            for(Group group:plugin.groups.values()){
                if(group.checkVault(location)){
                    String msg = "The vault registered to "+group.getName()+" at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                    if(player.hasPermission("beaconprotect.admin")){
                        player.sendMessage(msg);
                    }
                    plugin.logger.info(msg);
                    group.removeVault(location);
                }
            }
        }
    }
}
