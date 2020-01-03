package net.lemonpickles.BeaconProtect;

import net.minecraft.server.v1_15_R1.CommandSay;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.*;

public class BeaconEvent implements Listener{
    private BeaconProtect plugin;
    private Map<Player, Long> isReinforcing = new HashMap<>();
    private long reinforceDelay = 5;//default value
    BeaconEvent(BeaconProtect plugin){
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reinforceDelay = this.plugin.getConfig().getLong("reinforce_delay")*1000;
    }
    @EventHandler
    public void blockPlace(BlockPlaceEvent event){
        if(!plugin.ready){
            event.getPlayer().sendMessage("Please wait until BeaconProtect has initialized");
            event.setCancelled(true);
            return;
        }
        if(!plugin.bypass.contains(event.getPlayer())) {
            Block block = event.getBlock();
            Player player = event.getPlayer();
            block = checkDoubleBlock(block);
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
        if(!plugin.ready){
            event.getPlayer().sendMessage("Please wait until BeaconProtect has initialized");
            event.setCancelled(true);
            return;
        }
        if(!plugin.bypass.contains(event.getPlayer())) {
            Player player = event.getPlayer();
            Block block = checkDoubleBlock(event.getClickedBlock());
            ItemStack stack = player.getInventory().getItemInMainHand();
            boolean reinforce = player.isSneaking() && !stack.getType().isAir();//everything but air works
            if (isReinforcing.containsKey(player) && !reinforce) {
                isReinforcing.remove(player);
                player.sendMessage("Left block reinforce mode.");
                event.setCancelled(true);
            } else if (event.getHand() == EquipmentSlot.HAND && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (block!=null&&plugin.interactProtection.containsKey(block.getType())) {
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
                    if (isReinforcing.containsKey(player)&&block!=null) {//in reinforce mode
                        reinforceAdd(player);
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
                                yes = true;
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
                        reinforceAdd(player);
                        player.sendMessage("Entered block reinforce mode. Shift+Punch a block to reinforce.");
                    }
                } else if (isReinforcing.containsKey(player)) {
                    isReinforcing.remove(player);
                    player.sendMessage("Left block reinforce mode.");//no need to cancel event here
                } else if(block!=null){//info click
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
        if(!plugin.ready){
            event.getPlayer().sendMessage("Please wait until BeaconProtect has initialized");
            event.setCancelled(true);
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!plugin.bypass.contains(event.getPlayer())) {
            block = checkDoubleBlock(block);
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
        Material material = block.getType();
        if (material == Material.BEACON) {
            Location location = block.getLocation();
            if (plugin.beacons.containsKey(location)) {
                for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {//remove from group ownership too
                    if (entry.getValue().checkBeacon(location)) {
                        entry.getValue().removeBeacon(location);
                    }
                }
                plugin.beacons.remove(location);
                String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                if (player.hasPermission("beaconprotect.admin")) {
                    player.sendMessage(msg);
                }
                plugin.logger.info(msg);
            }
        } else if (material == Material.CHEST) {
            Location location = block.getLocation();
            for (Group group : plugin.groups.values()) {
                if (group.checkVault(location)) {
                    String msg = "The vault registered to " + group.getName() + " at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                    if (player.hasPermission("beaconprotect.admin")) {
                        player.sendMessage(msg);
                    }
                    plugin.logger.info(msg);
                    group.removeVault(location);
                }
            }
        }
    }
    private void reinforceAdd(Player player){
        isReinforcing.put(player,System.currentTimeMillis());
        new Thread(() -> {
            try {
                Thread.sleep(reinforceDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isReinforcing.containsKey(player)&&System.currentTimeMillis()-isReinforcing.get(player)>reinforceDelay) {
                isReinforcing.remove(player);
                player.sendMessage("Left block reinforce mode");
            }
        }).start();
    }
    private Block checkDoubleBlock(Block block){
        //long start = System.nanoTime();
        if(block!=null) {
            BlockState state = block.getState();
            List<Material> doors = new ArrayList<>(Arrays.asList(Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.OAK_DOOR, Material.SPRUCE_DOOR));
            if (state instanceof Chest){
                InventoryHolder holder = ((Chest)state).getInventory().getHolder();
                if(holder instanceof DoubleChest) {
                    DoubleChest doubleChest = (DoubleChest) holder;
                    InventoryHolder left = doubleChest.getLeftSide();
                    InventoryHolder right = doubleChest.getRightSide();
                    if(left==null||right==null){
                        plugin.logger.warning("Couldn't use DoubleChest at (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ") as an InventoryHolder");
                        return block;
                    }
                    Location leftChest = Objects.requireNonNull(left.getInventory().getLocation()).getBlock().getLocation();//this is unfortunately necessary because the location is .5 off as an entity not a block
                    Location rightChest = Objects.requireNonNull(right.getInventory().getLocation()).getBlock().getLocation();
                    if (plugin.durabilities.containsKey(leftChest)) {
                        //System.out.println((System.nanoTime() - start) / 10000);
                        return leftChest.getBlock();
                    } else if (plugin.durabilities.containsKey(rightChest)) {
                        //System.out.println((System.nanoTime() - start) / 10000);
                        return rightChest.getBlock();
                    }//otherwise the block durability will be whatever is in the plugin
                }
            } else if (doors.contains(block.getType())) {
                Block topDoor;
                Block bottomDoor;
                if (doors.contains(block.getRelative(BlockFace.UP).getType())) {
                    topDoor = block.getRelative(BlockFace.UP);
                    bottomDoor = block;
                } else if (doors.contains(block.getRelative(BlockFace.DOWN).getType())) {
                    bottomDoor = block.getRelative(BlockFace.DOWN);
                    topDoor = block;
                } else {
                    plugin.logger.warning("Found malformed door at (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
                    return block;
                }
                if (plugin.durabilities.containsKey(topDoor.getLocation())) {
                    //System.out.println((System.nanoTime()-start)/10000);
                    return topDoor;
                } else if (plugin.durabilities.containsKey(bottomDoor.getLocation())) {
                    //System.out.println((System.nanoTime()-start)/10000);
                    return bottomDoor;
                }
            }
        }
        //System.out.println((System.nanoTime()-start)/10000);
        return block;//its a regular block
    }
}
