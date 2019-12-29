package io.github.thebusybiscuit.zeldahearts;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.thebusybiscuit.cscorelib2.data.PersistentDataAPI;
import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.scheduling.TaskQueue;

public class ZeldaHeartsListener implements Listener {
	
	private final ZeldaHearts plugin;
	private final NamespacedKey key;
	private final float startingHealth;
	
	public ZeldaHeartsListener(ZeldaHearts plugin) {
		this.plugin = plugin;
		this.key = new NamespacedKey(plugin, "zelda_hearts");
		this.startingHealth = plugin.getConfigFile().getInt("hearts.starting-hearts") * 2;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if (plugin.getConfig().getBoolean("hearts.drop-on-kill") && !e.getDrops().contains(plugin.heart)) e.getDrops().add(plugin.heart);
		if (plugin.getConfig().getStringList("hearts.drop-canisters").contains(e.getEntityType().toString()) && !e.getDrops().contains(plugin.heartCanister)) e.getDrops().add(plugin.heartCanister); 
	
		if (e.getEntity() instanceof Player) {
			if (plugin.getConfig().getBoolean("thief-mode.enabled")) {
				if (e.getEntity().getMaxHealth() > (plugin.getConfig().getInt("thief-mode.min-hearts") * 2)) {
					if (ThreadLocalRandom.current().nextInt(100) <= plugin.getConfig().getInt("thief-mode.chance") && (e.getEntity().getKiller().getMaxHealth() / 2) < plugin.getConfig().getInt("hearts.max-hearts")) {
						
						e.getEntity().setMaxHealth(e.getEntity().getMaxHealth() - 2);
						e.getEntity().getKiller().setMaxHealth(e.getEntity().getKiller().getMaxHealth() + 2);
						
						plugin.getLocal().sendMessage(e.getEntity().getKiller(), "messages.thief.stole", true, msg -> msg.replace("%player%", ((Player) e.getEntity()).getName()));
						plugin.getLocal().sendMessage((Player) e.getEntity(), "messages.thief.stolen", true, msg -> msg.replace("%player%", ((Player) e.getEntity()).getKiller().getName()));
						
						playSound(e.getEntity().getKiller());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (!PersistentDataAPI.hasFloat(e.getPlayer(), key)) {
			PersistentDataAPI.setFloat(e.getPlayer(), key, startingHealth);
			e.getPlayer().setMaxHealth(startingHealth);
		}
		else {
			float health = PersistentDataAPI.getFloat(e.getPlayer(), key);
			if (health < startingHealth) {
				PersistentDataAPI.setFloat(e.getPlayer(), key, startingHealth);
				e.getPlayer().setMaxHealth(startingHealth);
			}
		}
	}
	
	@EventHandler
	public void onPickup(EntityPickupItemEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		
		if (new CustomItem(e.getItem().getItemStack(), 1).isSimilar(plugin.heart)) {
			
			Player p = (Player) e.getEntity();
			
			p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1));
			e.setCancelled(true);
			e.getItem().remove();
			
			
		}
	}
	
	@EventHandler
	public void onUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		ItemStack item = e.getItem();
		
		if (item != null && new CustomItem(item, 1).isSimilar(plugin.heartCanister)) {
			double max = p.getMaxHealth() + 2;
			if (max / 2 > plugin.getConfig().getInt("hearts.max-hearts")) plugin.getLocal().sendMessage(p, "messages.max-hearts");
			else {
				p.setMaxHealth(max);
				ItemUtils.consumeItem(item, false);
				
				playSound(p);
			}
		}
	}
	
	private void playSound(Player p) {
		TaskQueue queue = new TaskQueue();
		queue.thenRun(4, () -> p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.5F));
		queue.thenRepeatEvery(4, 3, () -> p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F));
		
		queue.execute(plugin);
	}
}
