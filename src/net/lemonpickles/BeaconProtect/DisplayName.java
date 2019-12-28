package net.lemonpickles.BeaconProtect;

import org.bukkit.Material;

public class DisplayName {
    static public String materialToDisplayName(Material material){
        String ret = material.toString().replaceAll("_"," ").toLowerCase();
        boolean newWord = true;
        for(int i=0;i<ret.length();i++){
            if(newWord){ret=ret.substring(0,i)+ret.substring(i,i+1).toUpperCase()+ret.substring(i+1);newWord=false;}else{
                if(ret.substring(i,i+1).equalsIgnoreCase(" ")){newWord=true;}
            }
        }
        return ret;
    }
}
