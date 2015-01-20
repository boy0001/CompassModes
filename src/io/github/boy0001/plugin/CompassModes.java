// TODO force default (currently it just rejects)

package io.github.boy0001.plugin;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;







import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public final class CompassModes extends JavaPlugin implements Listener {
	Timer timer = new Timer ();
	EssentialsFeature ess;
	private YamlConfiguration langYAML;
    public boolean checkperm(Player player,String perm) {
    	boolean hasperm = false;
    	String[] nodes = perm.split("\\.");
    	String n2 = "";
    	if (player==null) {
    		return true;
    	}
    	else if (player.hasPermission(perm)) {
    		hasperm = true;
    	}
    	else if (player.isOp()==true) {
    		hasperm = true;
    	}
    	else {
    		for(int i = 0; i < nodes.length-1; i++) {
    			n2+=nodes[i]+".";
            	if (player.hasPermission(n2+"*")) {
            		hasperm = true;
            	}
    		}
    	}
		return hasperm;
    }
	TimerTask mytask = new TimerTask () {
		String mymode;
		@Override
	    public void run () {
	    	for(Player player:getServer().getOnlinePlayers()){
	    		try {
	    		try {
				if (getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
					mymode = getConfig().getString("multiworld."+player.getWorld().getName()+".mode");
				}
				else if (getConfig().contains("Players."+player.getName()+"."+player.getWorld().getName())) {
					mymode = "false";
				}
				else {
					mymode = getConfig().getString("multiworld."+player.getWorld().getName()+".mode");
				}
	    		}
	    		catch (Exception e) {
	    			mymode = "false";
	    		}
	    		if ( ((getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName()).equalsIgnoreCase("NEAR"))&&(checkperm(player,"compassmodes.near")) && (mymode == "false")) || (mymode.equalsIgnoreCase("NEAR"))){
	    			double last = 0;
	        		Player sp = null;
	        		
	        		for(Player all:getServer().getOnlinePlayers()){
						if ((all.getLocation().getWorld() == player.getLocation().getWorld()) && (all.getName() != player.getName())) {
							double dist = player.getLocation().distance(all.getLocation());
							if ((((sp == null) || (dist < last))&&((dist < Integer.parseInt(getConfig().getString("range")))||(getConfig().getString("range") == "0")))) {
								sp = all;
								last = dist;
							}
						}
					}
					if (sp != null) {
						if (sp.isFlying()&&(sp.isOnGround()==false)) {
							if (getConfig().getBoolean("track-flying-players")) {
								player.setCompassTarget(sp.getLocation());
							}
						}
						else 
						{
							player.setCompassTarget(sp.getLocation());
						}
					}
	        	}
	    		else if ( ((getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName()).equalsIgnoreCase("RANDOM"))&&(checkperm(player,"compassmodes.random")) && (mymode == "false")) || (mymode.equalsIgnoreCase("RANDOM"))) {
	    			Random rand1 = new Random();
	    			int random1 = rand1.nextInt(500)-250;
	    			int random2 = rand1.nextInt(500)-250;
	    			Location myloc = new Location(player.getWorld(),player.getLocation().getX()+random1,64,player.getLocation().getZ()+random2);
	    			player.setCompassTarget(myloc);
	    		}
	    		else if (((Bukkit.getPlayer(getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName())) != null)&&(checkperm(player,"compassmodes.player"))) || (mymode.equalsIgnoreCase(getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName())))) {
	    			if (Bukkit.getPlayer(getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName())).getWorld() == player.getWorld()) {
	    				double dist = player.getLocation().distance(Bukkit.getPlayer(getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName())).getLocation());
	    				if ((getConfig().getString("range")=="0")||(dist < Integer.parseInt(getConfig().getString("range")))) {
	    					player.setCompassTarget(Bukkit.getPlayer(getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName())).getLocation());
	    				}
	    				
	    			}
	    		}
    		}
    		catch (Exception e) {		
			}
	    }
		}
	};
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		
		if (event instanceof PlayerDeathEvent) 	{
        final Player player = (Player) event.getEntity();
        Location loc = player.getLocation();
        respawnitems.remove(player.getName());
        deathpoints.put(player.getName(),loc.getX()+","+loc.getZ()+","+player.getWorld().getName());
        if (getConfig().getBoolean("keep-existing-compass")) {
        	List<ItemStack> mydrops = event.getDrops();
        	for (int i = 0; i < mydrops.size(); i++) {
        		if (mydrops.get(i).getTypeId()==345) {
        			respawnitems.put(player.getName(), mydrops.get(i));
        			mydrops.set(i,null);
        			return;
        		}
        	}
        }
        //save location
	}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();

		if (respawnitems.get(player.getName()) != null) {
			if (getConfig().getString("force-compass-slot").equalsIgnoreCase("false")) {
				player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			}
			else {
				player.getInventory().setItem(getConfig().getInt("force-compass-slot"), (ItemStack) respawnitems.get(player.getName()));
			}
			respawnitems.remove(player.getName());
		}
		if (getConfig().getBoolean("give-compass-on-respawn")) {
			if (player.getInventory().contains(345)==false) {
				if (getConfig().getString("force-compass-slot").equalsIgnoreCase("false")) {
					player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
				}
				else {
					player.getInventory().setItem(getConfig().getInt("force-compass-slot"), new ItemStack(Material.COMPASS, 1));
				}
			}
		}
		
		final Player myplayer = event.getPlayer();
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				update(myplayer);				
			}
        }, 20L);
	}
	HashMap<String, String> deathpoints = new HashMap<String, String>();
	HashMap<String, Object> respawnitems = new HashMap<String, Object>();
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		if (getConfig().getString("force-compass-slot").equalsIgnoreCase("false")==false) {
			if (event.getItemDrop().getItemStack().getTypeId()==345) {
				if(inventory.getHeldItemSlot() == getConfig().getInt("force-compass-slot")) {
					event.setCancelled(true);
					}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		try {
		ItemStack item = event.getCurrentItem();
		if (item.getTypeId()==345) {
			if (getConfig().getString("force-compass-slot").equalsIgnoreCase("false")==false) {
				if (event.getSlotType().equals(InventoryType.SlotType.QUICKBAR)) {
					if (event.getSlot()==getConfig().getInt("force-compass-slot")) {
						event.setCancelled(true);
					}
				}
			}
		}
		}
		catch (Exception e) {
			
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player myplayer = event.getPlayer();
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				update(myplayer);				
			}
        }, 20L);
	}
	@Override
    public void onEnable(){ 
		getConfig().options().copyDefaults(true);
        final Map<String, Object> options = new HashMap<String, Object>();
        saveResource("english.yml", true);
        getConfig().set("version", getDescription().getVersion());
        options.put("range", "512");
        options.put("keep-existing-compass", false);
        options.put("give-compass-on-respawn", false);
        options.put("force-compass-slot", false);
        options.put("track-flying-players", true);
        options.put("language", "english");
        //TODO give compass on death
        //TODO give compass only if they die with compass
        //TODO prevent dying with a compass
        options.put("Players.Notch","RANDOM");
        for(World world : getServer().getWorlds()) {
        	options.put("multiworld."+world.getName()+".mode","DEFAULT");
        	options.put("multiworld."+world.getName()+".hotbar-message",false);
            options.put("multiworld."+world.getName()+".override",false);
        }
        
        
        for (final Entry<String, Object> node : options.entrySet()) {
        	 if (!getConfig().contains(node.getKey())) {
        		 getConfig().set(node.getKey(), node.getValue());
        	 }
        }
        saveConfig();
        langYAML = YamlConfiguration.loadConfiguration(new File(getDataFolder(), getConfig().getString("language").toLowerCase()+".yml"));
        Plugin essentialsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		if (essentialsPlugin!=null) {
			if (essentialsPlugin.isEnabled()) {
				ess = new EssentialsFeature();
				System.out.print("Found plugin Essentials, CompassModes will now use it for homes.");
			}
		}
		deathpoints = new HashMap<String, String>();
		try {
			getConfig().getString("range");
		}
		catch (Exception e) {
			getConfig().set("range","-1");
		}
		timer.schedule (mytask, 0l, 1000);
    	this.saveDefaultConfig();
    	getServer().getPluginManager().registerEvents(this, this);
    	
        // TODO get list of players and read their compass mode
    }
    // Command handling
    public void msg(Player player,String mystring) {
    	if (mystring==null||mystring.equals("")) {
    		return;
    	}
    	if (player==null) {
    		getServer().getConsoleSender().sendMessage(colorise(mystring));
    	}
    	else if (player instanceof Player==false) {
    		getServer().getConsoleSender().sendMessage(colorise(mystring));
    	}
    	else {
    		player.sendMessage(colorise(mystring));
    	}

    }
    public String colorise(String mystring) {
    	String[] codes = {"&1","&2","&3","&4","&5","&6","&7","&8","&9","&0","&a","&b","&c","&d","&e","&f","&r","&l","&m","&n","&o","&k"};
    	for (String code:codes) {
    		mystring = mystring.replace(code, "§"+code.charAt(1));
    	}
    	return mystring;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	Player player;
    	if (sender instanceof Player) {
			player = (Player) sender;
    	}
    	else {
    		player = null;
    	}
    	if(cmd.getName().equalsIgnoreCase("compasstest")){
    		msg(player,"&dThis is a totally useless test command");
    	}
    	else if (cmd.getName().equalsIgnoreCase("compass")) {
    		if (args.length > 0){
	    		if (args[0].equalsIgnoreCase("list")){
	    			if (player!=null) {
	    			msg(player,ChatColor.GOLD+"Modes:");
	    			String mycolor;
	    			String mymode = "";
	    			String mymsg = "";
	    			boolean hasperm = false;
	    			String[] Modes = {"near","random","current","location","player","default","bed","death","north","south","east","west","home"};
	    			if (getConfig().getBoolean("multiworld."+player.getWorld().getName()+".override")) {
	    				mymode = getConfig().getString("multiworld."+player.getWorld().getName()+".mode");
	    				mymsg = mymode;
	    			}
	    			else {
	    				hasperm = true;
	    				if (getConfig().contains("Players."+player.getName()+"."+player.getWorld().getName())) { 
		    				mymode = getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName());
		    				mymsg = mymode;
		    			}
	    			}
	    			if (mymode.contains(",")) { mymode="location"; }
	    			else if (mymode.contains("home:")) { mymode="home"; }
	    			else if ((Bukkit.getPlayer(mymode)!=null)&&(mymode.equals("")==false)) { mymode = "player"; }
	    			for (String i:Modes) {
	    				String message = i;
	    				if (i.equalsIgnoreCase("location")&&mymode.equals(i)) {
	    					message =i+" &7(&8"+player.getCompassTarget().getBlockX()+"&7,&8"+player.getCompassTarget().getBlockY()+"&7,&8"+player.getCompassTarget().getBlockZ()+"&7)";
	    				}
	    				else if (i.equalsIgnoreCase("player" )&&mymode.equals(i)) {
	    					message = i+" &7(&8"+mymsg+"&7)";
	    				}
	    				else if (i.equalsIgnoreCase("home")&&mymode.equals(i)) {
	    					message = i+" &7(&8"+mymsg+"&7)";
	    				}
	    				if (i.equalsIgnoreCase(mymode)) {
	    					msg(player,"&7 - &9"+message.toUpperCase());
	    				}
	    				else if (hasperm==false) {
	    					msg(player,"&7 - "+message.toUpperCase());
	    				}
	    				else if (checkperm(player, "compassmodes."+i)) {
	    					msg(player,"&7 - &a"+message.toUpperCase());
	    				}
	    				else {
	    					msg(player,"&7 - &c"+message.toUpperCase());
	    				}
	    			}
	    		}
	    			else {
	    				System.out.println("Sorry, you must be a player to perform this action.");
	    			}
	    			
	    		}
	    		else if (args[0].equalsIgnoreCase("help")){
	    			msg(player,ChatColor.GOLD+"Commands:");
	    			msg(player,ChatColor.GREEN+" - /compass <mode> - sets your compass mode");
	    			msg(player,ChatColor.GREEN+" - /compass <mode> <player> - sets a player's mode");
	    			msg(player,ChatColor.GREEN+" - /compass list - a list of all the modes");
	    			msg(player,ChatColor.GREEN+" - /compass reload - reloads the config file");
	    			msg(player,ChatColor.GREEN+" - /compass help - shows this page");
	    		}
	    		else if ((args[0].equalsIgnoreCase("reload"))){
	    			if (player!=null) {
	    				if (checkperm(player,"compassmodes.reload")) {
	    					for (Player user:Bukkit.getOnlinePlayers()) {
	    						update(user);
	    					}
	    	    			this.reloadConfig();
	    	    			this.saveDefaultConfig();
	    					msg(player,ChatColor.GRAY + "Successfully reloaded " + ChatColor.RED + "CompassModes"+ChatColor.WHITE + ".");
	    				}
	    				else {
	    					msg(player,ChatColor.RED + "Sorry, you do not have permission to perform this action.");
	    				}
	    			}
	    			else {
		    			this.reloadConfig();
		    			this.saveDefaultConfig();
	    				System.out.println("Successfully reloaded CompassModes");
	    			}
	    			// RELOAD CONFIG
	    		}
	    		else if (args.length == 1){
	    			if (sender instanceof Player) {
	    				if ((args[0].equalsIgnoreCase("CURRENT"))&&(checkperm(player,"compassmodes.current"))){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),player.getLocation().getX()+","+player.getLocation().getZ());
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
	    						player.setCompassTarget(player.getLocation());
	    						msg(player,"&7Compass set to:&a current location");
	    					}
	    					this.saveConfig();
	    					
	    				}
	    				else if ((args[0].equalsIgnoreCase("NEAR"))&&(checkperm(player,"compassmodes.near"))){
	    					this.reloadConfig();
	    	        		double last = 0;
	    	        		Player sp = null;
	    	        		for(Player all:getServer().getOnlinePlayers()){
	    						if ((all.getLocation().getWorld() == player.getLocation().getWorld()) && (all.getName() != player.getName())) {
	    							double dist = player.getLocation().distance(all.getLocation());
	    							if (((sp == null) || (dist < last))&&((dist < Integer.parseInt(getConfig().getString("range")))||(getConfig().getString("range") == "0"))) {
	    								sp = all;
	    								last = dist;
	    							}
	    						}
	    	        		}
	    					if (sp != null) {
		    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
		    						msg(player,"&7Your preference is currently being &coverridden&7.");
		    					}
		    					else {
		    						if (sp.isFlying()&&(sp.isOnGround()==false)) {
		    							if (getConfig().getBoolean("track-flying-players")) {
		    								if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
		    		    						msg(player,"&7Your preference is currently being &coverridden&7.");
		    		    					}
		    		    					else {
		    		    						player.setCompassTarget(sp.getLocation());
		    									msg(player,"&7Currently tracking: &a"+sp.getName());
		    		    					}
		    							}
		    							else {
		    								msg(player,"&c[Error] There are no players nearby");
		    							}
		    						}
		    						else 
		    						{
		    							if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
		    	    						msg(player,"&7Your preference is currently being &coverridden&7.");
		    	    					}
		    	    					else {
			    							player.setCompassTarget(sp.getLocation());
			    							msg(player,"&7Currently tracking: &a"+sp.getName());
		    	    					}
		    						}
		    					}
	    						this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"NEAR");
	    						this.saveConfig();
	    					}
	    					
	    					else {
	    						msg(player,"&c[Error] There are no players nearby");
	    					}
	    					
	    				}
	    				else if ((args[0].equalsIgnoreCase("DEFAULT"))&&(checkperm(player,"compassmodes.default"))){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"DEFAULT");
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
	    						player.setCompassTarget(player.getWorld().getSpawnLocation());
	    						msg(player,"&7Compass set to:&a spawnpoint");
	    					}
	    					this.saveConfig();
	    				}
	    				else if ((args[0].equalsIgnoreCase("NONE"))){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),null);
	    					msg(player,"&7Compass preference &acleared&7.");
	    					this.saveConfig();
	    					update((Player) sender);
	    				}
	    				else if ((args[0].equalsIgnoreCase("BED"))&&(checkperm(player,"compassmodes.bed"))){
	    					this.reloadConfig();
	    					if (player.getBedSpawnLocation() != null) {
	    						this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"BED");
	    						if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
		    						msg(player,"&7Your preference is currently being &coverridden&7.");
		    					}
		    					else {
		    						player.setCompassTarget(player.getBedSpawnLocation());
		    						msg(player,"&7Compass set to:&a your bed");
		    					}
		    					this.saveConfig();
	    					}
	    					else {
	    						msg(player,"&7You don't have a: &cbed&7 :(");
	    					}
	    				}
	    				else if ((args[0].equalsIgnoreCase("RANDOM"))&&(checkperm(player,"compassmodes.random"))){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"RANDOM");
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
	    						player.setCompassTarget(player.getLocation());
		    					msg(player,"&7Compass will now point towards a &arandom&7 location");
	    					}
	    					this.saveConfig();
	    				}
	    				else if ((args[0].equalsIgnoreCase("EAST"))&&(checkperm(player,"compassmodes.location"))){
	    					Location myloc = new Location(player.getWorld(),999999999,64,0);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),999999999+",0");
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
		    					player.setCompassTarget(myloc);
		    					msg(player,"&7Compass will now point:&a east");
	    					}
	    					this.saveConfig();
	    				}
	    				else if ((args[0].equalsIgnoreCase("WEST"))&&(checkperm(player,"compassmodes.location"))){
	    					Location myloc = new Location(player.getWorld(),-999999999,64,0);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),-999999999+",0");
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
		    					player.setCompassTarget(myloc);
		    					msg(player,"&7Compass will now point:&a west");
	    					}
	    					this.saveConfig();
	    				}
	    				else if ((args[0].equalsIgnoreCase("SOUTH"))&&(checkperm(player,"compassmodes.location"))){
	    					Location myloc = new Location(player.getWorld(),0,64,999999999);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"0,"+999999999);
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
		    					player.setCompassTarget(myloc);
		    					msg(player,"&7Compass will now point:&a south");
	    					}
	    					this.saveConfig();
	    				}
	    				else if ((args[0].equalsIgnoreCase("NORTH"))&&(checkperm(player,"compassmodes.location"))){ //TODO
	    					Location myloc = new Location(player.getWorld(),0,64,-999999999);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"0,"+(-999999999));
	    					if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
	    					else {
		    					player.setCompassTarget(myloc);
		    					msg(player,"&7Compass will now point:&a north");
	    					}
	    					this.saveConfig();
	    				}
	    				else if ((args[0].toLowerCase().contains("home:"))&&(checkperm(player,"compassmodes.home"))){
	    					if (ess!=null) {
	    						Location myloc = ess.getHome(player, args[0].substring(5));
	    						if (myloc==null) {
	    							msg(player,"&cNo home by that name exists in this world");
	    						}
	    						else {
	    							this.reloadConfig();
	    							if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    	    					}
	    	    					else {
		    							player.setCompassTarget(myloc);
		    	    					msg(player,"&7Compass set to:&a "+args[0]);
	    							}
	    							this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),args[0]);
	    							this.saveConfig();
	    						}
	    					}
	    					else {
	    						msg(player,"&cYou are homeless!");
	    					}
	    				}
	    				else if ((args[0].equalsIgnoreCase("DEATH"))&&(checkperm(player,"compassmodes.deathpoint"))){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"death");
	    					try {
	    						String[] last =  deathpoints.get(sender.getName()).split(",");
	    						Location myloc = new Location(Bukkit.getWorld(last[2]),Double.valueOf(last[0]).intValue(),player.getEyeHeight(),Double.valueOf(last[1]).intValue());
	    						if (Bukkit.getWorld(last[3]) == player.getWorld()) {
	    							if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    	    					}
	    	    					else {
	    	    						player.setCompassTarget(myloc);
	    	    						msg(player,"&7Compass set to:&a your death point");
	    	    					}
	    							this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),"DEATH");
	    							
	    						}
	    						else {
	    							msg(player,"&cYou did not die in this map.");
	    						}
	    					}
	    					catch (Exception e) {
	    						msg(player,"&cYou have not died recently.");
	    					}
	    					this.saveConfig();
	    					
	    				}
	    				else if (((StringUtils.countMatches(String.valueOf(args[0]), ",") == 2))&&(checkperm(player,"compassmodes.location"))) {
	    					try {
	    						this.reloadConfig();
	    						String[] parts = args[0].split(",");
	    						Location myloc = new Location(player.getWorld(),Double.valueOf(parts[0]).intValue(),Double.valueOf(parts[1]).intValue(),Double.valueOf(parts[2]).intValue());
	    						
	    						if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
		    						msg(player,"&7Your preference is currently being &coverridden&7.");
		    					}
		    					else {
		    						player.setCompassTarget(myloc);
		    						msg(player,"&7Compass set to:&a "+args[0]);
		    					}
	    						
	    						this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),args[0]);
	    						this.saveConfig();
	    					}
	    					catch (Exception e) {
	    						msg(player,"&7Invalid syntax, please use&c /compass X,Y,Z");
	    			        }
	    				}
	    				else if (((StringUtils.countMatches(String.valueOf(args[0]), ",") == 1))&&(checkperm(player,"compassmodes.location"))) {
	    					try {
	    						this.reloadConfig();
	    						String[] parts = args[0].split(",");
	    						Location myloc = new Location(player.getWorld(),Double.valueOf(parts[0]).intValue(),64,Double.valueOf(parts[1]).intValue());
	    						
	    						if (this.getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
		    						msg(player,"&7Your preference is currently being &coverridden&7.");
		    					}
		    					else {
		    						player.setCompassTarget(myloc);
		    						msg(player,"&7Compass set to:&a "+args[0]);
		    					}
	    						
	    						this.getConfig().set("Players."+sender.getName()+"."+((Player) sender).getWorld().getName(),args[0]);
	    						this.saveConfig();
	    					}
	    					catch (Exception e) {
	    						msg(player,"&7Invalid syntax, please use&c /compass X,Z");
	    			        }
	    				}
	    				else if (((Bukkit.getPlayer(args[0])!=null))&&(checkperm(player,"compassmodes.player"))){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+player.getName()+"."+player.getWorld().getName(),args[0]);
	    					this.saveConfig();
	    					
    						if (this.getConfig().getString("multiworld.."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
	    						msg(player,"&7Your preference is currently being &coverridden&7.");
	    					}
    						else {
    							msg(player,"&7You are now tracking:&a " + args[0]);
    						}
	    				}
	    				else if ((args[0].equalsIgnoreCase("player"))&&(checkperm(player,"compassmodes.player"))){
	    					msg(player,"&7Use &e /compass <playername>");
	    				}
	    				else if ((args[0].equalsIgnoreCase("location"))&&(checkperm(player,"compassmodes.location"))){
	    					msg(player,"&7Use &e /compass X,Z");
	    				}
	    				else if ((args[0].equalsIgnoreCase("home"))&&(checkperm(player,"compassmodes.home"))){
	    					msg(player,"&7Use &e /compass home:<name>");
	    				}
	    				else {
	    					msg(player,"&7The mode you entered doesn't exist or is denied: &c"+ args[0]+"&7. try /compass help");
	    				}
	    				
	    			}
	    			else {
	    				msg(player,"&cSorry, you do not have an inventory.");
	    			}
	    			this.saveConfig();
	    		}
	    			else {
	    			if (checkperm(player,"compassmodes.other")) {
	    			if (Bukkit.getPlayer(args[1])!=null) {
	    				Player user = Bukkit.getPlayer(args[1]);
	    				if (args[0].equalsIgnoreCase("DEFAULT")) {
	    					user.setCompassTarget(user.getWorld().getSpawnLocation());
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName()+"."+user.getWorld().getName(),"DEFAULT");
	    					this.saveConfig();
	    					msg(player,"&7Compass set to:&a DEFAULT for "+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("HERE")) {
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),player.getLocation().getX()+","+player.getLocation().getZ());
	    					this.saveConfig();
	    					Location myloc = new Location(player.getWorld(),player.getLocation().getX(),64,player.getLocation().getZ());
	    					user.setCompassTarget(myloc);
	    					msg(player,"&7Compass set to:&a "+player.getLocation().getX()+", "+player.getLocation().getZ()+" for "+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("CURRENT")) {
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),user.getLocation().getX()+","+user.getLocation().getZ());
	    					this.saveConfig();
	    					Location myloc = new Location(user.getWorld(),user.getLocation().getX(),64,user.getLocation().getZ());
	    					user.setCompassTarget(myloc);
	    					msg(player,"&7Compass set to:&a "+user.getLocation().getX()+", "+user.getLocation().getZ()+" for "+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("RANDOM")) {
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),"RANDOM");
	    					this.saveConfig();
	    					msg(player,"&7Compass set to:&a RANDOM for "+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("DEATH")) {
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),"DEATH");
	    					this.saveConfig();
	    	        		try {
	    	    				String[] last =  deathpoints.get(user.getName()).split(",");
	    	    				Location myloc = new Location(Bukkit.getWorld(last[2]),Double.valueOf(last[0]).intValue(),64,Double.valueOf(last[1]).intValue());
	    	    				if (Bukkit.getWorld(last[2]) == user.getWorld()) {
	    	    					user.setCompassTarget(myloc);
	    	    				}
	    	    				msg(player,"&7Compass set to:&a DEATH for "+args[1]);
	    	    			}
	    	    			catch (Exception e) {
	    	    				msg(player,args[1]+" does not have a death point.");
	    	    			}
	    				}
	    				else if ((args[0].toLowerCase().contains("home:"))&&(checkperm(player,"compassmodes.home"))){
	    					if (ess!=null) {
	    						Location myloc = ess.getHome(user, args[0].substring(5));
	    						if (myloc==null) {
	    							msg(player,args[1]+" does not have any homes by that name");
	    						}
	    						else {
	    							this.reloadConfig();
	    							user.setCompassTarget(myloc);
	    							this.getConfig().set("Players."+args[1]+"."+player.getWorld().getName(),args[0]);
	    							this.saveConfig();
	    							msg(player,"&7Compass set to:&a "+args[0]+" for "+args[1]);
	    						}
	    					}
	    					else {
	    						msg(player,args[1]+" does not have any homes");
	    					}
	    				}
	    				else if (args[0].equalsIgnoreCase("NEAR")) {
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),"NEAR");
	    					this.saveConfig();
	    					msg(player,"&7Compass set to:&a NEAR for "+args[1]);
	    				}
	    				else if ((StringUtils.countMatches(String.valueOf(args[0]), ",") == 2)) {
	    					try {
	    						this.reloadConfig();
	    						String[] parts = args[0].split(",");
	    						Location myloc = new Location(user.getWorld(),Double.valueOf(parts[0]).intValue(),Double.valueOf(parts[1]).intValue(),Double.valueOf(parts[2]).intValue());
	    						user.setCompassTarget(myloc);
	    						this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),args[0]);
	    						this.saveConfig();
	    						msg(player,"&7Compass set to:&a "+args[0]+ "for "+args[1]);
	    					}
	    					catch (Exception e) {
	    						msg(player,"&7Invalid syntax, please use&c /compass X,Y,Z <playername>");
	    			        }
	    				}
	    				else if ((StringUtils.countMatches(String.valueOf(args[0]), ",") == 1)) {
	    					try {
	    						this.reloadConfig();
	    						String[] parts = args[0].split(",");
	    						Location myloc = new Location(user.getWorld(),Double.valueOf(parts[0]).intValue(),64,Double.valueOf(parts[1]).intValue());
	    						user.setCompassTarget(myloc);
	    						this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),args[0]);
	    						this.saveConfig();
	    						msg(player,"&7Compass set to:&a "+args[0]+ "for "+args[1]);
	    					}
	    					catch (Exception e) {
	    						msg(player,"&7Invalid syntax, please use&c /compass X,Z <playername>");
	    			        }
	    				}
	    				else if (args[0].equalsIgnoreCase("BED")) {
	    					if (user.getBedSpawnLocation() != null) {
	    						this.getConfig().set("Players."+user.getName(),"BED");
	    						user.setCompassTarget(user.getBedSpawnLocation());
	    						this.saveConfig();
	    						msg(player,"&7Compass set for:&a"+args[1]);
	    					}
	    					else {
	    						msg(player,"&7Player did not have a:&c bed");
	    					}
	    				}
	    				else if (args[0].equalsIgnoreCase("NORTH")) {
	    					//TODO
	    					Location myloc = new Location(user.getWorld(),0,64,-999999999);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+user.getName(),"0,"+(-999999999));
	    					user.setCompassTarget(myloc);
	    					this.saveConfig();
	    					msg(player,"&7Compass set for:&a"+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("EAST")) {
	    					Location myloc = new Location(user.getWorld(),999999999,64,0);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+user.getName(),999999999+",0");
	    					user.setCompassTarget(myloc);
	    					this.saveConfig();
	    					msg(player,"&7Compass set for:&a"+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("SOUTH")) {
	    					Location myloc = new Location(user.getWorld(),0,64,999999999);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+user.getName(),"0,"+999999999);
	    					user.setCompassTarget(myloc);
	    					this.saveConfig();
	    					msg(player,"&7Compass set for:&a"+args[1]);
	    				}
	    				else if (args[0].equalsIgnoreCase("WEST")) {
	    					Location myloc = new Location(user.getWorld(),-999999999,64,0);
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+user.getName(),-999999999+",0");
	    					user.setCompassTarget(myloc);
	    					this.saveConfig();
	    					msg(player,"&7Compass set for:&a"+args[1]);
	    				}
	    				else if ((Bukkit.getPlayer(args[0])!=null)){
	    					this.reloadConfig();
	    					this.getConfig().set("Players."+args[1]+"."+user.getWorld().getName(),args[0]);
	    					this.saveConfig();
	    					msg(player,args[1] + " is now tracking " + args[0]);
	    				}
	    				else {
	    					msg(player,"&7Unknown mode: &c" + args[0]);
	    				}
	    			}
	    			else {
	    				msg(player,"&7Cannot find PLAYER: &c"+args[1]);
	    			}
	    			}
	    			else {
	    				msg(player,"&7Too many parameters. For &4help&7 use &1/compass help");
	    			}
    			}
    		}
    		else {
    			msg(player,ChatColor.GOLD+"Commands:");
    			msg(player,ChatColor.GREEN+" - /compass <mode> - sets your compass mode");
    			msg(player,ChatColor.GREEN+" - /compass <mode> <player> - sets a player's mode");
    			msg(player,ChatColor.GREEN+" - /compass list - a list of all the modes");
    			msg(player,ChatColor.GREEN+" - /compass reload - reloads the config file");
    			msg(player,ChatColor.GREEN+" - /compass help - shows this page");
    		}
    	}
    	return false; 
    }
    @EventHandler
	public void onMove(PlayerMoveEvent event){
    	Player player = event.getPlayer();
    	try {
    		if (getConfig().getString("force-compass-slot").equalsIgnoreCase("false")==false) {
    			int index = (getConfig().getInt("force-compass-slot")-1);
    			ItemStack item = player.getInventory().getItem(index);
    			if (item==null) {
    				player.getInventory().setItem(getConfig().getInt("force-compass-slot"), new ItemStack(Material.COMPASS));
    			}
    			else if (item.getType().equals(Material.COMPASS)==false) {
    				player.getInventory().setItem(getConfig().getInt("force-compass-slot"), new ItemStack(Material.COMPASS));
    			}
    		}
		}
		catch (Exception e) {
			
		}
    	if (player.getItemInHand().getType().equals(Material.COMPASS)) {
    		if (checkperm(player,"compassmodes.info")) {
    			StringBuilder text = new StringBuilder(getConfig().getString("multiworld."+player.getWorld().getName()+".hotbar-message")); 
    			if (!text.equals("false")) {
    				int q = 0;
    				List<Integer> indicies = new ArrayList<Integer>();
    	            for(int i = 0; i < text.length(); i++) {
    	                char current = text.charAt(i);
    	                if (current == '{') {
    	                    indicies.add(i);
    	                    q++;
    	                }
	    	            else if (current == '}') {
	    	                if (q>0) {
		                        q--;
		                        int lastindx = indicies.size()-1;
		                        int start = indicies.get(lastindx);
		                        String replace = text.substring(start+1,i);
		                        String result;
		                        if (replace.equals("player")) {
		                        	result = player.getName();
		                        }
		                        else if (replace.equals("x")) {
		                        	result = player.getLocation().getBlockX()+"";
		                        }
		                        else if (replace.equals("y")) {
		                        	result = player.getLocation().getBlockY()+"";
		                        }
		                        else if (replace.equals("z")) {
		                        	result = player.getLocation().getBlockZ()+"";
		                        }
		                        else if (replace.equals("pitch")) {
		                        	result = ""+Math.round(player.getLocation().getPitch());
		                        }
		                        else if (replace.equals("yaw")) {
		                        	result = ""+Math.round(player.getLocation().getYaw());
		                        }
		                        else if (replace.equals("dist")) {
		                        	result = Math.round(player.getLocation().distance(player.getCompassTarget()))+"";
		                        }
		                        else if (replace.equals("dir")) {
		                        	Location loc = player.getCompassTarget().subtract(player.getLocation());
		                        	double angle = Math.toDegrees(Math.atan2(loc.getX(), loc.getZ()));
		                            if(angle < 0){
		                                angle += 360;
		                            }
		                            result = ""+Math.round(angle);
		                        }
		                        else if (replace.equals("angle")) {
		                        	Location loc = player.getCompassTarget().subtract(player.getLocation());
		                        	double angle = Math.toDegrees(Math.atan2(loc.getX(), loc.getZ()));
		                        	angle += player.getLocation().getYaw();
		                        	if (angle>360) {
		                        		angle -= 360;
		                        	}
		                        	else if(angle < -360){
		                                angle += 720;
		                            }
		                        	else if(angle < 0){
		                                angle += 360;
		                            }
		                            result = ""+Math.round(angle);
		                        }
		                        else if (replace.equals("distx")) {
		                        	result = ""+Math.round(player.getLocation().getX()-player.getCompassTarget().getX());
		                        }
		                        else if (replace.equals("disty")) {
		                        	result = ""+Math.round(player.getLocation().getY()-player.getCompassTarget().getY());
		                        }
		                        else if (replace.equals("distz")) {
		                        	result = ""+Math.round(player.getLocation().getZ()-player.getCompassTarget().getZ());
		                        }
		                        else if (replace.equals("targx")) {
		                        	result = player.getCompassTarget().getBlockX()+"";
		                        }
		                        else if (replace.equals("targy")) {
		                        	result = player.getCompassTarget().getBlockY()+"";
		                        }
		                        else if (replace.equals("targz")) {
		                        	result = player.getCompassTarget().getBlockZ()+"";
		                        }
		                        else if (replace.equals("cardinal")) {
		                			int degrees = (Math.round(player.getLocation().getYaw()) + 270) % 360;
		                            if (degrees <= 22)  {result="WEST";}
		                            else if (degrees <= 67) {result=getMessage("NORTHWEST");}
		                            else if (degrees <= 112) {result=getMessage("NORTH");}
		                            else if (degrees <= 157) {result=getMessage("NORTHEAST");}
		                            else if (degrees <= 202) {result=getMessage("EAST");}
		                            else if (degrees <= 247) {result=getMessage("SOUTHEAST");}
		                            else if (degrees <= 292) {result=getMessage("SOUTH");}
		                            else if (degrees <= 337) {result=getMessage("SOUTHWEST");}
		                            else {result=getMessage("WEST");}
		                        }
		                        else if (replace.equals("targyaw")) {
		                        	result = Math.round(player.getCompassTarget().getYaw())+"";
		                        }
		                        else if (replace.equals("targpitch")) {
		                        	result = Math.round(player.getCompassTarget().getPitch())+"";
		                        }
		                        else if (replace.equals("distyaw")) {
		                        	result = ""+Math.round(player.getLocation().getYaw()-player.getCompassTarget().getYaw());
		                        }
		                        else if (replace.equals("distpitch")) {
		                        	result = ""+Math.round(player.getLocation().getPitch()-player.getCompassTarget().getPitch());
		                        }
		                        else {
		                        	result = "["+replace+"=ERROR]";
		                        }
		                        // EVALUATE
		                        text.replace(start, i+1, result);
		                        indicies.remove(lastindx);
		                        i = start;
	    	                }
	    	                
	    	            }
	        			ItemMeta meta = player.getItemInHand().getItemMeta();
	        			meta.setDisplayName(colorise(text.toString()));
	        			player.getItemInHand().setItemMeta(meta);
    	            }
    			}
    		}
    	}
    }
    public String getMessage(String key) {
		try {
			return colorise(langYAML.getString(key));
		}
		catch (Exception e){
			return "";
		}
	}
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		final Player myplayer = event.getPlayer();
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				update(myplayer);				
			}
        }, 20L);
    }
    
    public void update(Player player) {
    	boolean override = false;
		String current = "";
        try {
    		try {
			if (getConfig().getString("multiworld."+player.getWorld().getName()+".override").equalsIgnoreCase("true")) {
				current = getConfig().getString("multiworld."+player.getWorld().getName()+".mode");
				override = true;
			}
			else if (this.getConfig().contains("Players."+player.getName()+"."+player.getWorld().getName())) {
				current = getConfig().getString("Players."+player.getName()+"."+player.getWorld().getName());
				
			}
			else {
				current = getConfig().getString("multiworld."+player.getWorld().getName()+".mode");
			}
    		}
    		catch (Exception e) {
    		}
    		if (((StringUtils.countMatches(String.valueOf(current), ",") == 2)&&(override||checkperm(player,"compassmodes.location")))) {
        		String[] parts = current.split(",");
        		Location myloc = new Location(player.getWorld(),Double.valueOf(parts[0]).intValue(),Double.valueOf(parts[1]).intValue(),Double.valueOf(parts[2]).intValue());
        		player.setCompassTarget(myloc);
        	}
    		else if (((StringUtils.countMatches(String.valueOf(current), ",") == 1)&&(override||checkperm(player,"compassmodes.location")))) {
        		String[] parts = current.split(",");
        		Location myloc = new Location(player.getWorld(),Double.valueOf(parts[0]).intValue(),64,Double.valueOf(parts[1]).intValue());
        		player.setCompassTarget(myloc);
        	}
    		else if ((current.equalsIgnoreCase("east"))&&(override||checkperm(player,"compassmodes.location"))) {
    			Location myloc = new Location(player.getWorld(),999999999,64,0);
        		player.setCompassTarget(myloc);
        	}
    		else if ((current.equalsIgnoreCase("west"))&&(override||checkperm(player,"compassmodes.location"))) {
    			Location myloc = new Location(player.getWorld(),-999999999,64,0);
        		player.setCompassTarget(myloc);
        	}
    		else if ((current.equalsIgnoreCase("south"))&&(override||checkperm(player,"compassmodes.location"))) {
    			Location myloc = new Location(player.getWorld(),0,64,999999999);
        		player.setCompassTarget(myloc);
        	}
    		else if ((current.equalsIgnoreCase("north"))&&(override||checkperm(player,"compassmodes.location"))) {
    			Location myloc = new Location(player.getWorld(),0,64,-999999999);
        		player.setCompassTarget(myloc);
        	}
        	else if ((current.equalsIgnoreCase("DEATH"))&&(override||checkperm(player,"compassmodes.deathpoint"))) { 
        		try {
    				String[] last =  deathpoints.get(player.getName()).split(",");
    				Location myloc = new Location(Bukkit.getWorld(last[2]),Double.valueOf(last[0]).intValue(),64,Double.valueOf(last[1]).intValue());
    				if (Bukkit.getWorld(last[2]) == player.getWorld()) {
    					((Player) player).setCompassTarget(myloc);
    				}
    			}
    			catch (Exception e) {
    			}
        	}
			else if ((current.toLowerCase().contains("home:"))&&(override||checkperm(player,"compassmodes.home"))){
				if (ess!=null) {
					Location myloc = ess.getHome(player, current.substring(5));
					if (myloc!=null) {
						player.setCompassTarget(myloc);
					}
				}
			}
        	else if ((current.equalsIgnoreCase("BED"))&&(override||checkperm(player,"compassmodes.bed"))) { 
				this.reloadConfig();
				if (player.getBedSpawnLocation() != null) {
					player.setCompassTarget(player.getBedSpawnLocation());
				}       	
        	} 	
        }
        catch (Exception e) {
        	player.setCompassTarget(player.getWorld().getSpawnLocation());
        	e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {
    	try {
    	timer.cancel();
    	timer.purge();
    	}
    	catch (Exception e) {
    		
    	}
    	this.reloadConfig();
    	this.saveConfig();
        // TODO Insert logic to be performed when the plugin is disabled
    }
         
    // Leave event
}
