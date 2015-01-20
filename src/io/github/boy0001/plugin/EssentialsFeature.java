package io.github.boy0001.plugin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;

public class EssentialsFeature  implements Listener {
	Plugin essentialsPlugin;
    public EssentialsFeature() {
    }
    public Location getHome(Player player,String home) {
    	Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        try {
        	List<String> homes = ess.getUser(player).getHomes();
        	for (String current:homes) {
        		if (current.equalsIgnoreCase(home)) {
        			return ess.getUser(player).getHome(current);
        		}
        	}
        }
        catch (Exception e) {
        }
        return null;
    }
}
