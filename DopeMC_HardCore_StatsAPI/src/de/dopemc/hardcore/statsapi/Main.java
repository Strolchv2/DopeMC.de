package de.dopemc.hardcore.statsapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements CommandExecutor, Listener {

    public String Prefix = ChatColor.YELLOW + "DopeMC.de " + ChatColor.GRAY + "✘ " + ChatColor.WHITE;
    private FileConfiguration statsConfig;
    private File statsFile;
    private final Map<UUID, Map<String, Integer>> playerStats = new HashMap<>();

    private final List<String> availableStats = Arrays.asList(
            "init_leben", // Umbenannt von "leben"
            "kills",
            "tode",
            "getoetete_tiere",
            "getoetete_mobs",
            "bloecke_abgebaut",
            "bloecke_plaziert"
    );

    private final int INITIAL_LIVES = 3; // Standardmäßige Anzahl an Leben

    @Override
    public void onEnable() {
        getCommand("stats").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        loadConfig();
        loadStats();
    }

    @Override
    public void onDisable() {
        saveStats();
    }

    private void loadConfig() {
        statsFile = new File(getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            statsFile.getParentFile().mkdirs();
            saveDefaultConfig();
            copy(getResource("config.yml"), statsFile);
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        if (statsConfig == null) {
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void saveConfig() {
        if (statsConfig != null) {
            try {
                statsConfig.save(statsFile);
            } catch (IOException e) {
            }
        } else {
        }
    }

    private void loadStats() {
        if (statsConfig != null && statsConfig.contains("players")) {
            for (String uuidString : statsConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    Map<String, Integer> stats = new HashMap<>();
                    if (statsConfig.contains("players." + uuidString)) {
                        for (String statKey : statsConfig.getConfigurationSection("players." + uuidString).getKeys(false)) {
                            stats.put(statKey.toLowerCase(), statsConfig.getInt("players." + uuidString + "." + statKey, 0));
                        }
                    }
                    playerStats.put(uuid, stats);
                } catch (IllegalArgumentException e) {
                }
            }
        } else if (statsConfig == null) {
        }
    }

    private void saveStats() {
        if (statsConfig != null) {
            statsConfig.set("players", null);
            for (Map.Entry<UUID, Map<String, Integer>> playerEntry : playerStats.entrySet()) {
                for (Map.Entry<String, Integer> statEntry : playerEntry.getValue().entrySet()) {
                    statsConfig.set("players." + playerEntry.getKey().toString() + "." + statEntry.getKey(), statEntry.getValue());
                }
            }
            saveConfig();
        } else {
        }
    }

    private int getStat(Player player, String stat) {
        return playerStats.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(stat.toLowerCase(), 0);
    }

    private void incrementStat(Player player, String stat) {
        Map<String, Integer> stats = playerStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        stats.put(stat.toLowerCase(), stats.getOrDefault(stat.toLowerCase(), 0) + 1);
        saveStats();
    }

    private void setStat(Player player, String stat, int value) {
        Map<String, Integer> stats = playerStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        stats.put(stat.toLowerCase(), value);
        saveStats();
    }

    private void addStat(Player player, String stat, int amount) {
        Map<String, Integer> stats = playerStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        stats.put(stat.toLowerCase(), stats.getOrDefault(stat.toLowerCase(), 0) + amount);
        saveStats();
    }

    private void removeStat(Player player, String stat, int amount) {
        Map<String, Integer> stats = playerStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        stats.put(stat.toLowerCase(), Math.max(0, stats.getOrDefault(stat.toLowerCase(), 0) - amount));
        saveStats();
    }

    private boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("statsapi.admin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stats")) {
            if (sender instanceof Player p) {
                if (args.length == 0) {
                    p.sendMessage("");
                    p.sendMessage(Prefix + ChatColor.AQUA + "Deine Statistiken (kommen bald im GUI!)");
                    p.sendMessage("");
                    return true;
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("list") && isAdmin(sender)) {
                        sender.sendMessage("");
                        sender.sendMessage(Prefix + ChatColor.AQUA + "Verfügbare Statistiken:");
                        for (String stat : availableStats) {
                            sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + stat);
                        }
                        sender.sendMessage("");
                        return true;
                    } else if (isAdmin(sender)) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target != null) {
                            sender.sendMessage("");
                            sender.sendMessage(Prefix + ChatColor.AQUA + "Statistiken von " + ChatColor.YELLOW + target.getName() + ":");
                            sender.sendMessage(ChatColor.GRAY + "  Leben: " + ChatColor.GREEN + getStat(target, "init_leben")); // Geänderter Stat-Name
                            sender.sendMessage(ChatColor.GRAY + "  Kills: " + ChatColor.GREEN + getStat(target, "kills"));
                            sender.sendMessage(ChatColor.GRAY + "  Tode: " + ChatColor.GREEN + getStat(target, "tode"));
                            sender.sendMessage(ChatColor.GRAY + "  Getötete Tiere: " + ChatColor.GREEN + getStat(target, "getoetete_tiere"));
                            sender.sendMessage(ChatColor.GRAY + "  Getötete Mobs: " + ChatColor.GREEN + getStat(target, "getoetete_mobs"));
                            sender.sendMessage(ChatColor.GRAY + "  Abgebaute Blöcke: " + ChatColor.GREEN + getStat(target, "bloecke_abgebaut"));
                            sender.sendMessage(ChatColor.GRAY + "  Platzierte Blöcke: " + ChatColor.GREEN + getStat(target, "bloecke_plaziert"));
                            sender.sendMessage("");
                            return true;
                        } else {
                            p.sendMessage("");
                            p.sendMessage(Prefix + ChatColor.RED + "Spieler '" + args[0] + "' nicht gefunden!");
                            p.sendMessage("");
                            return false;
                        }
                    } else {
                        p.sendMessage("");
                        p.sendMessage(Prefix + ChatColor.RED + "Hey das darfst du nicht!");
                        p.sendMessage("");
                        return true;
                    }
                } else if (args.length >= 3 && isAdmin(sender)) {
                    String subCommand = args[0].toLowerCase();
                    String targetName = args[1];
                    Player target = Bukkit.getPlayer(targetName);
                    if (target != null) {
                        String statName = args[2].toLowerCase();
                        if (!availableStats.contains(statName)) {
                            sender.sendMessage("");
                            sender.sendMessage(Prefix + ChatColor.RED + "Ungültige Statistik: '" + statName + "'. Nutze /stats list für eine Liste.");
                            sender.sendMessage("");
                            return false;
                        }
                        if (subCommand.equals("add")) {
                            if (args.length == 4) {
                                try {
                                    int amount = Integer.parseInt(args[3]);
                                    addStat(target, statName, amount);
                                    sender.sendMessage("");
                                    sender.sendMessage(Prefix + ChatColor.GREEN + amount + " zu Statistik '" + statName + "' von " + ChatColor.YELLOW + target.getName() + " hinzugefügt.");
                                    sender.sendMessage("");
                                    return true;
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("");
                                    sender.sendMessage(Prefix + ChatColor.RED + "Ungültige Menge!");
                                    sender.sendMessage("");
                                    return false;
                                }
                            } else {
                                sender.sendMessage("");
                                sender.sendMessage(Prefix + ChatColor.RED + "Verwendung: /stats add <Spieler> <Statistik> <Menge>");
                                sender.sendMessage("");
                                return false;
                            }
                        } else if (subCommand.equals("remove")) {
                            if (args.length == 4) {
                                try {
                                    int amount = Integer.parseInt(args[3]);
                                    removeStat(target, statName, amount);
                                    sender.sendMessage("");
                                    sender.sendMessage(Prefix + ChatColor.RED + amount + " von Statistik '" + statName + "' von " + ChatColor.YELLOW + target.getName() + " entfernt.");
                                    sender.sendMessage("");
                                    return true;
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("");
                                    sender.sendMessage(Prefix + ChatColor.RED + "Ungültige Menge!");
                                    sender.sendMessage("");
                                    return false;
                                }
                            } else {
                                sender.sendMessage("");
                                sender.sendMessage(Prefix + ChatColor.RED + "Verwendung: /stats remove <Spieler> <Statistik> <Menge>");
                                sender.sendMessage("");
                                return false;
                            }
                        } else if (subCommand.equals("set")) {
                            if (args.length == 4) {
                                try {
                                    int amount = Integer.parseInt(args[3]);
                                    setStat(target, statName, amount);
                                    sender.sendMessage("");
                                    sender.sendMessage(Prefix + "Statistik '" + statName + "' von " + ChatColor.YELLOW + target.getName() + " auf " + ChatColor.YELLOW + amount + " gesetzt.");
                                    sender.sendMessage("");
                                    return true;
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("");
                                    sender.sendMessage(Prefix + ChatColor.RED + "Ungültige Menge!");
                                    sender.sendMessage("");
                                    return false;
                                }
                            } else {
                                sender.sendMessage("");
                                sender.sendMessage(Prefix + ChatColor.RED + "Verwendung: /stats set <Spieler> <Statistik> <Menge>");
                                sender.sendMessage("");
                                return false;
                            }
                        } else {
                            sender.sendMessage("");
                            sender.sendMessage(Prefix + ChatColor.RED + "Verwendung: /stats [add/remove/set] <Spieler> <Statistik> <Menge>");
                            sender.sendMessage("");
                            return false;
                        }
                    } else {
                        sender.sendMessage("");
                        sender.sendMessage(Prefix + ChatColor.RED + "Spieler '" + targetName + "' nicht gefunden!");
                        sender.sendMessage("");
                        return false;
                    }
                } else {
                    p.sendMessage("");
                    p.sendMessage(Prefix + ChatColor.RED + "Verwendung: /stats [Spieler] oder /stats [list] oder /stats [add/remove/set] <Spieler> <Statistik> <Menge>");
                    p.sendMessage("");
                    return false;
                }
            } else {
                sender.sendMessage("");
                sender.sendMessage(Prefix + ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden!");
                sender.sendMessage("");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerStats.computeIfAbsent(player.getUniqueId(), k -> {
            Map<String, Integer> initialStats = new HashMap<>();
            initialStats.put("init_leben", INITIAL_LIVES); // Initialisiere Leben beim Join
            return initialStats;
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player killer) {
            if (event.getEntity() instanceof org.bukkit.entity.Animals) {
                incrementStat(killer, "getoetete_tiere");
            } else if (!(event.getEntity() instanceof Player)) {
                incrementStat(killer, "getoetete_mobs");
            } else {
                incrementStat(killer, "kills");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        incrementStat(player, "tode");
        // Hier könntest du Logik einfügen, um das "init_leben" des Spielers zu reduzieren
        int currentLives = getStat(player, "init_leben");
        if (currentLives > 0) {
            setStat(player, "init_leben", currentLives - 1);
            if (currentLives - 1 <= 0) {
                Bukkit.broadcastMessage(Prefix + ChatColor.RED + player.getName() + " ist gestorben (keine Leben mehr)!");
                // Hier könntest du weitere Aktionen bei endgültigem Tod auslösen
            } else {
                player.sendMessage(Prefix + ChatColor.YELLOW + "Du bist gestorben! Verbleibende Leben: " + ChatColor.RED + (currentLives - 1));
            }
        }
        if (player.getKiller() != null) {
            incrementStat(player.getKiller(), "kills");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        incrementStat(player, "bloecke_abgebaut");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        incrementStat(player, "bloecke_plaziert");
    }

    private void copy(java.io.InputStream in, File file) {
        try {
            java.nio.file.Files.copy(in, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}