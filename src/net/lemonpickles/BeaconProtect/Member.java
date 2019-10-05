package net.lemonpickles.BeaconProtect;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Member {
    public OfflinePlayer player;
    public String role;
    public Member(OfflinePlayer player, String role){
        this.player = player;
        this.role = role;
    }
}
