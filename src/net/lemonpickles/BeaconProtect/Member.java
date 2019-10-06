package net.lemonpickles.BeaconProtect;

import org.bukkit.OfflinePlayer;

public class Member {
    public OfflinePlayer player;
    public String role;
    public Member(OfflinePlayer player, String role){
        this.player = player;
        this.role = role;
    }
    public Member(OfflinePlayer player){
        this.player = player;
        this.role = "default or something";
    }
}
