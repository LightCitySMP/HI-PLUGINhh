package net.hiplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HiCommand implements CommandExecutor, TabCompleter {

    private HiPlugin plugin;
    private HashMap<UUID, Long> cooldowns;

    public HiCommand(HiPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("hiplugin.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("hiplugin.hi")) {
            p.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        List<String> disabledWorlds = plugin.getConfig().getStringList("disabled-worlds");
        if (disabledWorlds.contains(p.getWorld().getName())) {
            p.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
            return true;
        }

        int cdTime = plugin.getConfig().getInt("cooldown-seconds");
        if (cooldowns.containsKey(p.getUniqueId())) {
            long timePassed = (System.currentTimeMillis() - cooldowns.get(p.getUniqueId())) / 1000;
            if (timePassed < cdTime) {
                long timeLeft = cdTime - timePassed;
                p.sendMessage(ChatColor.RED + "Please wait " + timeLeft + "s before doing this again.");
                return true;
            }
        }

        String msg = plugin.getConfig().getString("message");
        if (msg != null) {
            msg = msg.replace("%player%", p.getName());
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
        plugin.logAction(p.getName(), "/hi");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                p.sendMessage(ChatColor.GREEN + "Your cooldown is over! You can use /hi again.");
            }
        }, cdTime * 20L);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("hiplugin.reload")) {
            String sub = args[0].toLowerCase();
            if ("reload".startsWith(sub)) {
                completions.add("reload");
            }
        }
        return completions;
    }
}