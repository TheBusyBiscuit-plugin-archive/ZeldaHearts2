package io.github.thebusybiscuit.zeldahearts;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import io.github.thebusybiscuit.cscorelib2.config.Localization;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.updater.BukkitUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.Updater;

public class ZeldaHearts extends JavaPlugin {
	
	public final ItemStack heart = new CustomItem(Material.RED_DYE, "&4Heart", "", "&rHeals you a bit when being picked up ;)");
	public final ItemStack heartCanister = new CustomItem(Material.RED_DYE, "&4Heart Canister", "", "&rGives you 1 extra Heart", "", "&7&eRight Click&7 to consume");
	
	private Config cfg;
	private Localization local;
	
	@Override
	public void onEnable() {
		cfg = new Config(this);
		
		Updater updater = new BukkitUpdater(this, getFile(), 63643);
		if (cfg.getBoolean("options.auto-update")) updater.start();
		
		new Metrics(this);
		
		new ZeldaHeartsListener(this);
		
		local = new Localization(this);
		
		local.setPrefix("&7[&9Zelda&4Hearts&7] ");
		local.setDefaultMessage("messages.max-hearts", "&4&lYou have reached the maximum amount of Hearts");
		local.setDefaultMessage("messages.thief.stole", "&4&lYou have stolen 1 of %player%'s Hearts");
		local.setDefaultMessage("messages.thief.stolen", "&4&l%player% stole you 1 of your Hearts");
	}
	
	public Config getConfigFile() {
		return cfg;
	}
	
	public Localization getLocal() {
		return local;
	}

}
