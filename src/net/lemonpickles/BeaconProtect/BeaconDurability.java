package net.lemonpickles.BeaconProtect;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BeaconDurability extends BlockDurability {
    Player owner;
    public Map<Player, Member> members = new HashMap<>();

    public BeaconDurability(BeaconProtect plugin, Block beacon, Player owner, int changeDur) {
        super(plugin, beacon, owner, changeDur);
        this.owner = owner;
    }
    public boolean checkMember(Player player){
        return members.containsKey(player);
    }
    public void addMember(Player player, String role){
        members.put(player, new Member(player, role));
    }
    public void removeMember(Player player){
        members.remove(player);
    }
}
