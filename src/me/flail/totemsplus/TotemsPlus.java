package me.flail.totemsplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TotemsPlus extends JavaPlugin {

	private Server server;

	public Map<UUID, Integer> cooldowns = new HashMap<>();
	public String PREFIX;
	public String RELOAD;
	public String NO_PERMISSION;
	public String TOTEM_GIVE_SELF;
	public String TOTEM_GIVE_OTHER;
	public String TOTEM_RECIEVE;
	public String RESURRECT;

	public FileConfiguration config;

	@Override
	public void onEnable() {
		server = getServer();

		saveDefaultConfig();
		config = getConfig();
		PREFIX = chat(config.getString("Prefix"));
		RELOAD = chat(config.getString("ReloadMessage"));
		NO_PERMISSION = chat(config.getString("NoPermissionMessage"));
		RESURRECT = chat(config.getString("ResurrectMessage"));
		TOTEM_GIVE_SELF = chat(config.getString("TotemGiveSelfMessage"));
		TOTEM_GIVE_OTHER = chat(config.getString("TotemGivenMessage"));
		TOTEM_RECIEVE = chat(config.getString("TotemRecievedMessage"));

		server.getScheduler().scheduleSyncDelayedTask(this, () -> {
			for (String cmd : getDescription().getCommands().keySet()) {
				getCommand(cmd).setExecutor(this);
				getCommand(cmd).setTabCompleter(this);
			}

			getServer().getPluginManager().registerEvents(new Listener(), this);

		}, 1L);
	}

	@Override
	public void onDisable() {
		cooldowns.clear();

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return new Commands().run(sender, command, label, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> tab = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		if (command.getName().equalsIgnoreCase("totemsplus")) {
			switch (args.length) {
			case 0:
				tempList.add("totemsplus");

				for (String s : tempList) {
					if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(s);
					}
				}
				break;
			case 1:
				tempList.add("give");
				tempList.add("get");
				tempList.add("about");
				tempList.add("reload");

				for (String s : tempList) {
					if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(s);
					}
				}
				break;
			case 2:
				if (args[0].equalsIgnoreCase("give")) {
					for (Player p : getServer().getOnlinePlayers()) {
						tempList.add(p.getName());
					}
				}

				for (String s : tempList) {
					if (s.toLowerCase().startsWith(args[1].toLowerCase())) {
						tab.add(s);
					}
				}
				break;
			}

		}

		return tab;
	}

	public List<PotionEffect> getTotemEffects() {
		List<PotionEffect> effects = new ArrayList<>();

		int duration = getConfig().getInt("PotionEffectDuration") * 20;
		int power = getConfig().getInt("PotionEffectStrength") - 1;

		boolean particles = getConfig().getBoolean("HideEffectParticles");

		List<String> effectList = getConfig().getStringList("TotemEffects");
		for (String s : effectList) {
			PotionEffectType type = PotionEffectType.getByName(s);

			if (type != null) {
				PotionEffect potion = new PotionEffect(type, duration, power, false, !particles);

				effects.add(potion);
				continue;
			}

			getLogger().warning(chat("&cERROR! Invalid Potion effect: &f" + s + "&c for TotemsPlus."));
		}

		return effects;
	}

	public boolean removeTotemFromShulker(ItemStack item) {
		BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();

		if (blockMeta.getBlockState() instanceof ShulkerBox) {
			ShulkerBox box = (ShulkerBox) blockMeta.getBlockState();
			Inventory inv = box.getSnapshotInventory();
			ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);

			if (inv.containsAtLeast(totem, 1)) {
				inv.removeItem(totem);

				box.update(true, false);

				blockMeta.setBlockState(box);
				item.setItemMeta(blockMeta);
				return true;
			}

		}

		return false;
	}

	public String chat(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

}
