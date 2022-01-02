package me.flail.totemsplus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands {

	private TotemsPlus plugin;

	public boolean run(CommandSender sender, Command command, String label, String[] args) {
		plugin = TotemsPlus.getPlugin(TotemsPlus.class);

		FileConfiguration config = plugin.config;

		String cmd = command.getName().toLowerCase();
		String version = plugin.getDescription().getVersion();
		String prefix = plugin.PREFIX + " ";
		String noPermissionMessage = plugin.NO_PERMISSION;
		String reloadMessage = plugin.RELOAD;
		String totemGet = plugin.TOTEM_GIVE_SELF;
		String totemReceived = plugin.TOTEM_RECIEVE;
		String totemGiven = plugin.TOTEM_GIVE_OTHER;
		String about = plugin
				.chat("&6TotemsPlus &7v&f" + version + " &6by FlailoftheLord. &7Running on&8: &7" + Bukkit.getVersion());
		String usage = prefix + " &6Usage&8: &7 totemsplus [give:get:about:reload] [get:<player-name>]";

		ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);

		if (cmd.equals("totemsplus")) {

			if (sender instanceof Player) {
				Player operator = (Player) sender;

				if (operator.hasPermission("totemsplus.command")) {
					if (args.length == 1) {

						if (args[0].equals("reload")) {
							if (operator.hasPermission("totemsplus.command.reload")) {
								plugin.reloadConfig();
								plugin.onDisable();
								plugin.onEnable();
								operator.sendMessage(prefix + reloadMessage);
							} else {
								operator.sendMessage(prefix + noPermissionMessage);
							}

						} else if (args[0].equals("get")) {
							if (operator.hasPermission("totemsplus.command.get")) {
								operator.getInventory().addItem(totem);
								operator.sendMessage(prefix + totemGet);
							} else {
								operator.sendMessage(prefix + noPermissionMessage);
							}

						} else if (args[0].equalsIgnoreCase("about")) {

							operator.sendMessage(about);

						} else {
							operator.sendMessage(usage);
						}

					} else if (args.length == 2) {

						if (args[0].equals("give")) {

							Player player = plugin.getServer().getPlayer(args[1]);

							if (operator.hasPermission("totemsplus.command.give")) {
								if ((player != null) && args[1].equalsIgnoreCase(player.getName())) {

									player.getInventory().addItem(totem);

									if (!player.getName().equals(operator.getName())) {
										player.sendMessage(prefix + totemReceived);

									}
									String totemGivenMessage = totemGiven.replace("%player%", args[1]);

									operator.sendMessage(prefix + totemGivenMessage);

								} else {
									operator.sendMessage(plugin.chat(prefix + " &c" + args[1] + " is not a valid player!"));
								}
							} else {
								operator.sendMessage(prefix + noPermissionMessage);
							}

						} else {
							operator.sendMessage(usage);
						}

					} else {
						operator.sendMessage(usage);
					}
				} else {
					operator.sendMessage(prefix + noPermissionMessage);
				}

			} else if (!(sender instanceof Player)) {
				sender.sendMessage(plugin.chat("&cThat Command can only be used in game!"));
			}

		}

		return true;
	}

}