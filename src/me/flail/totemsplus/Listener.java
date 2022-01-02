package me.flail.totemsplus;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class Listener implements org.bukkit.event.Listener {

	private TotemsPlus plugin;

	private FileConfiguration config;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDamage(EntityDamageEvent event) {

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			double pHealth = player.getHealth();
			double pDamage = event.getFinalDamage();

			plugin = JavaPlugin.getPlugin(TotemsPlus.class);
			config = plugin.config;

			if (event.getCause().equals(DamageCause.VOID) && config.getBoolean("IgnoreVoidDamage", true))
				return;

			if ((pDamage >= pHealth)) {

				if (player.hasPermission("totemsplus.use")) {

					String prefix = plugin.PREFIX + " ";

					String resurrectMessage = plugin.RESURRECT;
					String onCooldown = plugin
							.chat(config.getString("TotemOnCooldown", "&cTotems have been put on cooldown for {0} seconds!"));

					UUID uuid = player.getUniqueId();

					if (plugin.cooldowns.containsKey(uuid) && !player.hasPermission("totemsplus.bypasscooldown")) {
						player.sendMessage(prefix + onCooldown.replace("{0}", plugin.cooldowns.get(uuid).toString()));

						return;
					}

					boolean showEffect = config.getBoolean("ShowTotemEffect");

					PlayerInventory pInv = player.getInventory();
					ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);

					boolean hasTotem = pInv.containsAtLeast(totem, 1);
					boolean hasShulkerTotem = false;

					if (!hasTotem) {
						for (ItemStack item : pInv) {
							if ((item != null) && item.getType().toString().endsWith("SHULKER_BOX")) {
								hasShulkerTotem = plugin.removeTotemFromShulker(item);

								resurrectMessage = resurrectMessage.replace("inventory", "shulker box");
								break;
							}

						}

					}

					if (hasTotem || hasShulkerTotem) {
						plugin.cooldowns.put(uuid, Integer.valueOf(plugin.getConfig().getInt("Cooldown", 300)));

						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
							plugin.cooldowns.remove(uuid);
						}, plugin.cooldowns.get(uuid).intValue() * 20L);

						try {
							player.getInventory().removeItem(totem);
						} finally {
							event.setDamage(0);
						}

						for (PotionEffect effect : plugin.getTotemEffects()) {
							player.addPotionEffect(effect);
						}

						if (showEffect) {
							player.playEffect(EntityEffect.TOTEM_RESURRECT);
						}

						player.sendMessage(prefix + resurrectMessage);

						return;
					}

				}

			}

		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onResurrect(EntityResurrectEvent event) {
		if (!event.isCancelled()) {
			plugin = JavaPlugin.getPlugin(TotemsPlus.class);

			config = plugin.getConfig();

			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();

				String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("Prefix")) + " ";

				String resurrectMessage = config.getString("ResurrectMessage").replace("&", "�");

				boolean cooldownOnDefault = config.getBoolean("CooldownOnVanillaBehavior", false);
				if (cooldownOnDefault && !player.hasPermission("totemsplus.bypasscooldown")) {

					if (plugin.cooldowns.containsKey(player.getUniqueId())) {

						event.setCancelled(true);
						return;
					}

				}

				plugin.cooldowns.put(player.getUniqueId(), Integer.valueOf(config.getInt("Cooldown", 300)));

				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
					plugin.cooldowns.remove(player.getUniqueId());
				}, plugin.cooldowns.get(player.getUniqueId()).intValue() * 20L);

				player.sendMessage(prefix + resurrectMessage);
				return;

			}

		}

	}

}
