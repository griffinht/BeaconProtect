package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class BeaconEvent implements Listener{
    private BeaconProtect plugin;
    private Map<Player, Long> isReinforcing = new HashMap<>();
    private long reinforceDelay = 5;//backup default value
    private List<EntityType> explosiveEntities = new ArrayList<>(Arrays.asList(EntityType.PRIMED_TNT,EntityType.MINECART_TNT,EntityType.CREEPER,EntityType.FIREBALL,EntityType.SMALL_FIREBALL));//backup default value
    private Map<Player,Block> lastBlock = new HashMap<>();
    private List<Entity> enemyEntityList = new ArrayList<>();
    BeaconEvent(BeaconProtect plugin){
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reinforceDelay = this.plugin.getConfig().getLong("reinforce_delay")*1000;
        List<EntityType> entityTypes = new ArrayList<>();
        for(String string:this.plugin.getConfig().getStringList("explosive_entity_protect")){
            string = string.replaceAll("\\s","");
            try {
                EntityType entityType = EntityType.valueOf(string);
                entityTypes.add(entityType);
            }catch(IllegalArgumentException e){
                plugin.logger.warning("Could not convert "+string+" to a Bukkit entity type");
            }
        }
        if(entityTypes.size()>0) {
            explosiveEntities = entityTypes;
        }
    }
    //deals with block placing
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!plugin.ready){
            event.getPlayer().sendMessage("Please wait until BeaconProtect has initialized");
            event.setCancelled(true);
            return;
        }
        if(!plugin.bypass.contains(event.getPlayer())) {
            Block block = event.getBlock();
            Player player = event.getPlayer();
            block = checkDoubleBlock(block);
            if (CustomBeacons.checkFriendly(player, block.getLocation(), plugin.groups)) {
                if (block.getType() == Material.BEACON) {
                    Location location = block.getLocation();
                    if (!plugin.beacons.containsKey(location)) {
                        plugin.beacons.put(location, block);
                        String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been registered";
                        String msg2 = "";
                        Location claimed = CustomBeacons.checkOverlap(location,plugin.groups);
                        if(claimed==null) {
                            for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {//add beacon if player is in a group
                                if (entry.getValue().checkMember(player)) {
                                    entry.getValue().addBeacon(location);
                                    msg2 = " to group " + entry.getValue().getName();
                                }
                            }
                        }else{
                            System.out.println(claimed+", "+ CustomBeacons.getOwner(claimed,plugin.groups));
                            Group group1 = CustomBeacons.getOwner(claimed,plugin.groups);
                            if(group1!=null&&group1.checkMember(player)){
                                for (Map.Entry<UUID, Group> entry : plugin.groups.entrySet()) {//add beacon if player is in a group
                                    if (entry.getValue().checkMember(player)) {
                                        entry.getValue().addBeacon(location);
                                        msg2 = " to group " + entry.getValue().getName();
                                    }
                                }

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
    //deals with player interaction
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
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
            Action action = event.getAction();
            if (isReinforcing.containsKey(player) && !reinforce) {
                isReinforcing.remove(player);
                player.sendMessage("Left block reinforce mode.");
                event.setCancelled(true);
            } else if (event.getHand() == EquipmentSlot.HAND && action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(checkInteractProtect(block,player,true));
            } else if (action == Action.LEFT_CLICK_BLOCK) {
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
            }else if(action==Action.PHYSICAL){
                event.setCancelled(checkInteractProtect(block,player,false));
            }
        }
    }
    private boolean checkInteractProtect(Block block, Player player, boolean click) {
        if (block != null&&!CustomBeacons.checkFriendly(player, block.getLocation(), plugin.groups)) {
            if (plugin.interactProtection.containsKey(block.getType())) {
                if (plugin.interactProtection.get(block.getType())) {
                    if (!plugin.durabilities.containsKey(block.getLocation())) {
                        new BlockDurability(plugin, block, player, 0);
                    }
                    BlockDurability a = plugin.durabilities.get(block.getLocation());
                    int dur = a.getDurability() + Math.min(plugin.CustomBeacons.getMaxPenalty(player, block), a.getBeaconDurability());
                    if (dur != 1) {
                        if(click||block!=lastBlock.get(player)) {
                            player.sendMessage("You cannot interact here! " + (dur - 1) + " hits to unlock.");
                            infoClick(block, player);
                        }
                        lastBlock.put(player,block);
                        return true;
                    }//otherwise you are good
                } else if(!plugin.interactProtectBlacklist){//if whitelist
                    if(click||!block.equals(lastBlock.get(player))) {
                        player.sendMessage("You cannot interact here! This block is protected by a beacon.");
                        infoClick(block, player);
                    }
                    lastBlock.put(player,block);
                    return true;
                }
            }else{
                if(plugin.interactProtectBlacklist){
                    if(click||block!=lastBlock.get(player)) {
                        player.sendMessage("You cannot interact here! This block is protected by a beacon.");
                        infoClick(block, player);
                    }
                    lastBlock.put(player,block);
                    return true;
                }else{
                    return false;//whitelist dont cancel
                }
            }
        }
        return false;
    }
    private void infoClick(Block block, Player player){
        if (!plugin.durabilities.containsKey(block.getLocation())) {
            new BlockDurability(plugin, block, player, 0);
        } else {
            plugin.durabilities.get(block.getLocation()).changeDurability(plugin, player, 0, false);
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
    //deals with block breakage
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
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
    //deals with pistons
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event){
        event.setCancelled(onPiston(event.getBlocks(),event.getBlock(),event.getDirection()));
    }
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event){
        event.setCancelled(onPiston(event.getBlocks(),event.getBlock(),event.getDirection()));
    }
    private boolean onPiston(List<Block> blocks,Block piston,BlockFace blockFace){
        if(blocks.size()>0){
            Vector vector = new Vector(blockFace.getModX(),blockFace.getModY(),blockFace.getModZ());
            for(Map.Entry<Location,Block> entry:plugin.beacons.entrySet()){
                BlockState blockState = entry.getValue().getState();
                if(blockState instanceof Beacon&&CustomBeacons.checkInRange(piston.getLocation(),entry.getKey(),((Beacon)blockState).getTier())){
                    return false;//dont cancel if piston is already in beacon range therefore friendly beacon
                }
                Block beacon = entry.getValue();
                BlockState beaconState = beacon.getState();
                if(beaconState instanceof Beacon){
                    for(Block block:blocks){
                        if(CustomBeacons.checkInRange(block.getLocation().add(vector),entry.getKey(),((Beacon)beaconState).getTier())||CustomBeacons.checkInRange(block.getLocation(),entry.getKey(),((Beacon)beaconState).getTier())){//cancel if block is currently or will be in beacon range
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    //deals with liquid flow
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event){//only allow flow out of beacon range, not in
        Block block = event.getBlock();
        for(Map.Entry<Location,Block> entry:plugin.beacons.entrySet()){
            if(CustomBeacons.checkInRange(block.getLocation(),entry.getKey(),((Beacon)entry.getValue().getState()).getTier())){
                return;//don't cancel if the original block is already in beacon range
            }
            if(CustomBeacons.checkInRange(event.getToBlock().getLocation(),entry.getKey(),((Beacon)entry.getValue().getState()).getTier())){
                event.setCancelled(true);//cancel if the block to be flowed in is in range of beacon
                return;
            }
        }
    }
    //deals with lit tnt and other entity explosions
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        boolean friendly = true;
        if(CustomBeacons.checkAllRanges(event.getEntity().getLocation(),plugin.beacons)){
            if(enemyEntityList.contains(event.getEntity()))friendly = false;
        }
        onExplode(event.getEntity().getLocation(),event.blockList(),friendly);
    }
    //deals with beds maybe? don't worry it probably still works
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        onExplode(event.getBlock().getLocation(),event.blockList(),CustomBeacons.checkAllRanges(event.getBlock().getLocation(),plugin.beacons));
    }
    private void onExplode(Location boom, List<Block> blockList, boolean friendly){
        for(Map.Entry<Location,Block> entry:plugin.beacons.entrySet()){
            int tier = ((Beacon)entry.getValue().getState()).getTier();
            boolean useBeaconDurability = !(CustomBeacons.checkInRange(boom,entry.getKey(),tier)&&friendly);
            blockList.removeIf(block-> {
                Location location = block.getLocation();
                if(CustomBeacons.checkInRange(location,entry.getKey(),tier)) {
                    if (plugin.durabilities.containsKey(location)) {
                        plugin.durabilities.get(location).changeDurability(plugin, null, -1, false, useBeaconDurability);
                    } else {
                        new BlockDurability(plugin, block, null, -1, useBeaconDurability);
                    }
                    if (plugin.durabilities.containsKey(location)) {
                        if (plugin.durabilities.get(location).getDurability() > 0) {
                            return true;//block has 0 durability remaining so allow it to be blown up
                        }
                    }
                }
                return false;
            });
        }
    }
    //check if new entities are primed tnt, if so check if they should be allowed to blow up in claimed land
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event){
        if(explosiveEntities.contains(event.getEntityType()))explosiveEntityHandler(event.getEntity());
    }
    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event){
        if(explosiveEntities.contains(event.getVehicle().getType()))explosiveEntityHandler(event.getVehicle());
    }
    private void explosiveEntityHandler(Entity entity){
        for(Map.Entry<Location,Block> entry:plugin.beacons.entrySet()){
            if(!CustomBeacons.checkInRange(entity.getLocation(),entry.getKey(),((Beacon)entry.getValue().getState()).getTier())){
                enemyEntityList.add(entity);
                return;
            }
        }
    }
}
