package me.jdon.kill.streak;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class killstreak extends JavaPlugin implements Listener, CommandExecutor{
	
	// Stops player in a row saves 2 players involved
	Map<Player, Player> killers = new HashMap<Player, Player>();
	// Total kills
	Map<Player, Integer> kills = new HashMap<Player, Integer>();
	// Current Streak
	Map<Player, Integer> streak= new HashMap<Player, Integer>();
	// Total Deaths
	Map<Player, Integer> deaths= new HashMap<Player, Integer>();
	//logger
	Logger log = Logger.getLogger("Minecraft");
	
	//enable
	@Override
	public void onEnable () {
		getServer().getPluginManager().registerEvents(this, this);
		//loads and saves config
	    this.saveConfig();
	}
	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
	    deaths.remove(event.getPlayer());
	    streak.remove(event.getPlayer());
	    kills.remove(event.getPlayer());
	    killers.remove(event.getPlayer());
	}
	//sets up commands
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
		if(!(sender instanceof Player)){
			// Sent from the console
			this.getLogger().log(Level.INFO,"'kd' can only be sent by a player");
			return false;
		}
		// /kd command
		if (command.getName().equalsIgnoreCase("kd")) {
			Player p = (Player)sender;
			// checks if = to null
			if(!(deaths.containsKey(sender))){
				deaths.put(p, 1);
			}
			if(!(kills.containsKey(sender))){
				kills.put(p, 1);
			}
			// when not null works out your kd ratio
			  p.sendMessage(ChatColor.GREEN+"Your total kills are "+kills.get(p));
			  p.sendMessage(ChatColor.GREEN+"Your total deaths are "+deaths.get(p));
			  p.sendMessage(ChatColor.GREEN+"Your kill death ratio is "+kills.get(p)/deaths.get(p)+":1 !");
			  return true;
		}
	    return false;
	}
	
	//player death event
	@EventHandler
	public void event(PlayerDeathEvent event) {
		// puts streak to 0
		streak.put(event.getEntity(), 0);
		//checks if null
		if(!(deaths.containsKey(event.getEntity()))){
			deaths.put(event.getEntity(), 0);
		}else{
			// plus 1 to killstreak
			int death = deaths.get(event.getEntity());
			int deathp1 = death+1;
			deaths.put(event.getEntity(), deathp1);
		}
		// checks config for "pvp"
		if(this.getConfig().getBoolean("PVP") == true){
			if(this.getConfig().getBoolean("pvparena"))
			// checks if the killer is a player
		if(event.getEntity().getKiller() instanceof Player){
			//variables
			Player killed = event.getEntity();
			Player killer = event.getEntity().getKiller();
			//checks config to stop farming killstreaks
			if(this.getConfig().getBoolean("Stop player from killing same player twice in a row") == true){
				//only do it if true
				killers.put(killer, killed);
				if(killers.get(killer) == event.getEntity()){
					return;
				}
			}
			//runs the streakchecker method
			streakchecker(killer);
		}
		}else{
			return;
		}
	}
	
	
	// for "PVE"
	@EventHandler
	public void event(EntityDeathEvent ev){
		// checks config and if they are a player and if a monster is killed
		if(ev.getEntity() instanceof Monster && this.getConfig().getBoolean("PVE") == true && ev.getEntity().getKiller() instanceof Player){
			Player p = ev.getEntity().getKiller();
			//runs the killstreak method
			streakchecker(p);
		}
	}
	
	public void streakchecker(Player p){
		// checks if null
		if(!(streak.containsKey(p))){
			streak.put(p, 0);
			kills.put(p, 0);
		}
		// plus one to the streaks and deaths for kd
		Integer kill = streak.get(p);
		Integer plus = kill+1;
		Integer kill1 = kills.get(p);
		Integer plus1 = kill1+1;
		streak.put(p, plus);
		kills.put(p, plus1);
		// puts the int "plus" to a string
		String plus2 = String.valueOf(plus);
		// checks config for the killstreak message and puts the number in it
		p.sendMessage(ChatColor.GREEN+this.getConfig().getString("KillStreak-Message").replaceAll("%killstreaknumber%", plus2));
		CommandSender cs = Bukkit.getServer().getConsoleSender();
		//checks to see if the config is null
		if(this.getConfig().getString(Integer.toString(streak.get(p))) == null){
			return;
		}else{
			// for loop to run the commands
			int number = this.getConfig().getInt("1.NumberOfCommands");
			for (int i = 1; i <= number; i = i + 1) {
				// changes "i" to a string
				String istr = Integer.toString(i);
				// check to see if the commands run is the same as defined in the config
				if(this.getConfig().getString(Integer.toString(streak.get(p))+"."+istr) == null){
					// tells the user that the set number is too high
					this.getLogger().log(Level.INFO,"Warning You have set the number of commands too high");
					return;
				}
				// runs the commands for the kill streak
				Bukkit.getServer().dispatchCommand(cs, this.getConfig().getString(Integer.toString(streak.get(p))+"."+istr).replaceAll("%name%", p.getName()));
	         }
			
		}
		
		return;
		}
}
