package org.parkourpluginsaveiteminworld.tonkungz.afksystem;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.net.InetAddress;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.net.UnknownHostException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public final class AFKSystem extends JavaPlugin implements Listener {
    private FileConfiguration dataConfig;
    private File dataFile;

    private Location point1 = null;
    private Location point2 = null;
    private final Map<String, Location[]> points = new HashMap<>();
    private final Map<UUID, Location> playerLocations = new HashMap<>();
    private final File dataFiles = new File(getDataFolder(), "playerLocations.json");
    private LuckPerms luckPerms;
    private final Map<Player, Boolean> playerInRegion = new HashMap<>();
    private final Map<UUID, Long> afkTimers = new HashMap<>();
    private final Map<Player, BukkitRunnable> countdownTasks = new HashMap<>();
    private Map<Player, Boolean> hasEnteredAFKZone = new HashMap<>();
    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("afks").setExecutor(new AFKCommand());

        createDataFile();
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig == null) {
            getLogger().severe("Failed to load data configuration.");
            return;
        }

        getCommand("afks").setTabCompleter(new AFKCommandTabCompleter(dataConfig));
        luckPerms = LuckPermsProvider.get();

        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Create JSON file if it doesn't exist
        if (!dataFiles.exists()) {
            try {
                dataFiles.createNewFile();
                // Initialize the JSON file with an empty JSON object
                try (FileWriter writer = new FileWriter(dataFiles)) {
                    writer.write(new JSONObject().toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        startAfkCheckTask();
        loadPlayerLocations();
        loadPoints();
        getLogger().info("");
        getLogger().info("");
        getLogger().info("        _____              _   _                       _   _  _     _        ");
        getLogger().info("       (_   _)            ( ) ( )                     ( ) ( )( )   ( )       ");
        getLogger().info("         | |   _     ___  | |/'/' _   _   ___     __  | `\\| |`\\`\\_/'/'       ");
        getLogger().info("         | | /'_`\\ /' _ `\\| , <  ( ) ( )/' _ `\\ /'_ `\\| , ` |  `\\ /'         ");
        getLogger().info("         | |( (_) )| ( ) || |\\`\\ | (_) || ( ) |( (_) || |`\\ |   | |          ");
        getLogger().info("         (_)`\\___/'(_) (_)(_) (_)`\\___/'(_) (_)`\\__  |(_) (_)   (_)          ");
        getLogger().info(" ______                                        ( )_) |                ______ ");
        getLogger().info("(______)                                        \\___/'               (______)");
        getLogger().info(" ___                         _                                           _   ");
        getLogger().info("(  _`\\                      (_ )                                        ( )_ ");
        getLogger().info("| | ) |   __   _   _    __   | |    _    _ _     ___ ___     __    ___  | ,_)");
        getLogger().info("| | | ) /'__`\\( ) ( ) /'__`\\ | |  /'_`\\ ( '_`\\ /' _ ` _ `\\ /'__`\\/' _ `\\| |  ");
        getLogger().info("| |_) |(  ___/| \\_/ |(  ___/ | | ( (_) )| (_) )| ( ) ( ) |(  ___/| ( ) || |_ ");
        getLogger().info("(____/'`\\____)`\\___/'`\\____)(___)`\\___/'| ,__/'(_) (_) (_)`\\____)(_) (_)`\\__)");
        getLogger().info("                                        | |                                  ");
        getLogger().info("                                        (_)                                  ");
        getLogger().info(" ___                                                                          ");
        getLogger().info("(  _`\\                                                                        ");
        getLogger().info("| (_(_)_    _ __   ___ ___                                                    ");
        getLogger().info("|  _)/'_`\\ ( '__)/' _ ` _ `\\                                                  ");
        getLogger().info("| | ( (_) )| |   | ( ) ( ) |                                                  ");
        getLogger().info("(_) `\\___/'(_)   (_) (_) (_)                                                  ");
        getLogger().info("                                                                              ");
        getLogger().info("                                                                              ");
        getLogger().info(" _   _    __    _   _  _____  ___    ___    _   _                             ");
        getLogger().info("( ) ( ) /'__`\\ ( ) ( )(_   _)(  _`\\ (  _`\\ ( ) ( )                            ");
        getLogger().info("| |/'/'(_)  ) )| `\\| |  | |  | (_(_)| ( (_)| |_| |                            ");
        getLogger().info("| , <     /' / | , ` |  | |  |  _)_ | |  _ |  _  |                            ");
        getLogger().info("| |\\`\\  /' /( )| |`\\ |  | |  | (_( )| (_( )| | | |                            ");
        getLogger().info("(_) (_) (____/'(_) (_)  (_)  (____/'(____/'(_) (_)                            ");
        getLogger().info("                                                                              ");
        getLogger().info("        _____  ___    ___    _____  _   _  ___    ___    _____  _____        ");
        getLogger().info("/'\\_/`\\(  _  )(  _`\\ |  _`\\ (  _  )( ) ( )(  _`\\ (  _`\\ (  _  )(_   _)       ");
        getLogger().info("|     || (_) || |_) )| (_) )| (_) || `\\| || ( (_)| (_) )| ( ) |  | |         ");
        getLogger().info("| (_) ||  _  || ,__/'| ,  / |  _  || , ` || |___ |  _ <'| | | |  | |         ");
        getLogger().info("| | | || | | || |    | |\\ \\ | | | || |`\\ || (_, )| (_) )| (_) |  | |         ");
        getLogger().info("(_) (_)(_) (_)(_)    (_) (_)(_) (_)(_) (_)(____/'(____/'(_____)  (_)          ");
        getLogger().info("");
        getLogger().info("");
        getLogger().info("AFKSystem 1.0 Is started! TonKungNY(K2NTECH)");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveDataConfig();
        savePlayerLocations();
        getLogger().info("");
        getLogger().info("");
        getLogger().info("        _____              _   _                       _   _  _     _        ");
        getLogger().info("       (_   _)            ( ) ( )                     ( ) ( )( )   ( )       ");
        getLogger().info("         | |   _     ___  | |/'/' _   _   ___     __  | `\\| |`\\`\\_/'/'       ");
        getLogger().info("         | | /'_`\\ /' _ `\\| , <  ( ) ( )/' _ `\\ /'_ `\\| , ` |  `\\ /'         ");
        getLogger().info("         | |( (_) )| ( ) || |\\`\\ | (_) || ( ) |( (_) || |`\\ |   | |          ");
        getLogger().info("         (_)`\\___/'(_) (_)(_) (_)`\\___/'(_) (_)`\\__  |(_) (_)   (_)          ");
        getLogger().info(" ______                                        ( )_) |                ______ ");
        getLogger().info("(______)                                        \\___/'               (______)");
        getLogger().info(" ___                         _                                           _   ");
        getLogger().info("(  _`\\                      (_ )                                        ( )_ ");
        getLogger().info("| | ) |   __   _   _    __   | |    _    _ _     ___ ___     __    ___  | ,_)");
        getLogger().info("| | | ) /'__`\\( ) ( ) /'__`\\ | |  /'_`\\ ( '_`\\ /' _ ` _ `\\ /'__`\\/' _ `\\| |  ");
        getLogger().info("| |_) |(  ___/| \\_/ |(  ___/ | | ( (_) )| (_) )| ( ) ( ) |(  ___/| ( ) || |_ ");
        getLogger().info("(____/'`\\____)`\\___/'`\\____)(___)`\\___/'| ,__/'(_) (_) (_)`\\____)(_) (_)`\\__)");
        getLogger().info("                                        | |                                  ");
        getLogger().info("                                        (_)                                  ");
        getLogger().info(" ___                                                                          ");
        getLogger().info("(  _`\\                                                                        ");
        getLogger().info("| (_(_)_    _ __   ___ ___                                                    ");
        getLogger().info("|  _)/'_`\\ ( '__)/' _ ` _ `\\                                                  ");
        getLogger().info("| | ( (_) )| |   | ( ) ( ) |                                                  ");
        getLogger().info("(_) `\\___/'(_)   (_) (_) (_)                                                  ");
        getLogger().info("                                                                              ");
        getLogger().info("                                                                              ");
        getLogger().info(" _   _    __    _   _  _____  ___    ___    _   _                             ");
        getLogger().info("( ) ( ) /'__`\\ ( ) ( )(_   _)(  _`\\ (  _`\\ ( ) ( )                            ");
        getLogger().info("| |/'/'(_)  ) )| `\\| |  | |  | (_(_)| ( (_)| |_| |                            ");
        getLogger().info("| , <     /' / | , ` |  | |  |  _)_ | |  _ |  _  |                            ");
        getLogger().info("| |\\`\\  /' /( )| |`\\ |  | |  | (_( )| (_( )| | | |                            ");
        getLogger().info("(_) (_) (____/'(_) (_)  (_)  (____/'(____/'(_) (_)                            ");
        getLogger().info("                                                                              ");
        getLogger().info("        _____  ___    ___    _____  _   _  ___    ___    _____  _____        ");
        getLogger().info("/'\\_/`\\(  _  )(  _`\\ |  _`\\ (  _  )( ) ( )(  _`\\ (  _`\\ (  _  )(_   _)       ");
        getLogger().info("|     || (_) || |_) )| (_) )| (_) || `\\| || ( (_)| (_) )| ( ) |  | |         ");
        getLogger().info("| (_) ||  _  || ,__/'| ,  / |  _  || , ` || |___ |  _ <'| | | |  | |         ");
        getLogger().info("| | | || | | || |    | |\\ \\ | | | || |`\\ || (_, )| (_) )| (_) |  | |         ");
        getLogger().info("(_) (_)(_) (_)(_)    (_) (_)(_) (_)(_) (_)(____/'(____/'(_____)  (_)          ");
        getLogger().info("");
        getLogger().info("");
        getLogger().info("AFKSystem 1.0 Is stoped! TonKungNY(K2NTECH)");
    }

    public void reloadConfigData() {
        if (dataFile != null) {
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            loadPoints(); // รีโหลด points หลังจากโหลด config ใหม่
            getLogger().info("data.yml has been reloaded.");
        } else {

            getLogger().severe("Failed to reload data.yml, dataFile is null.");
        }
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            saveResource("data.yml", false);
            try {

                dataConfig = YamlConfiguration.loadConfiguration(dataFile);
                if (!dataConfig.contains("points")) {
                    dataConfig.createSection("points");
                    saveDataConfig();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveDataConfig() {
        if (dataConfig != null) {
            try {
                dataConfig.save(dataFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPoints() {
        points.clear();

        // หากไม่มีค่าใน "points" ให้หยุดการทำงาน
        if (dataConfig.contains("points")) {
            for (String key : dataConfig.getConfigurationSection("points").getKeys(false)) {
                Location[] locs = new Location[2];
                locs[0] = stringToLocation(dataConfig.getString("points." + key + ".point1"));
                locs[1] = stringToLocation(dataConfig.getString("points." + key + ".point2"));
                points.put(key, locs);
            }
        }

    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        if (parts.length != 4) {
            return null;
        }
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    public class AFKCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§8(§3§lTONKUNG§8) §7This command can only be executed by a player.");
                return true;
            }

            Player player = (Player) sender;

            // Check if the player is OP
            if (!player.hasPermission("tonkung.afk.admin")) {
                player.sendMessage("§8(§3§lTONKUNG§8) §cคุณไม่ได้อนุญาตให้ใช้คำสั่งนี้");
                return true;
            }
            if (args.length == 0) {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.BONE));
                player.sendMessage("§8(§3§lTONKUNG§8) §7คุณได้รับแท่งไฟ คลิ๊กซ้าย/ขวา เพื่อมาร์กจุด");
                return true;

            }else if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
                reloadConfigData();
                sender.sendMessage("§8(§3§lTONKUNG§8) §7รีโหลดแล้วจ้าาา");
                return true;

            }else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
                String pointName = args[1];

                if (point1 == null || point2 == null) {
                    player.sendMessage("§8(§3§lTONKUNG§8) §7ต้องทำเครื่องหมายทั้งสองจุดก่อนบันทึก");
                    return true;
                }

                dataConfig.set("points." + pointName + ".point1", locationToString(point1));
                dataConfig.set("points." + pointName + ".point2", locationToString(point2));
                dataConfig.set("points." + pointName + ".cooldownafk", 10); // Example cooldownflag value

                List<String> commands = Arrays.asList(
                        "say Hello <playerName>",
                        "gamemode survival <playerName>"
                );
                dataConfig.set("points." + pointName + ".command", commands);

                saveDataConfig();
                loadPoints();

                player.sendMessage("§8(§3§lTONKUNG§8) §7คุณได้มาร์คจุดเป็นชื่อ " + pointName);

                point1 = null;
                point2 = null;

                return true;
            }else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
                String pointName = args[1];

                if (!dataConfig.contains("points." + pointName)) {
                    player.sendMessage("§8(§3§lTONKUNG§8) §cชื่อมาร์คนี้ไม่พบในระบบ");
                    return true;
                }

                // Remove the specified point from the configuration
                dataConfig.set("points." + pointName, null);
                saveDataConfig();
                loadPoints();

                player.sendMessage("§8(§3§lTONKUNG§8) §7ชื่อมาร์ค " + pointName + " ถูกลบเรียบร้อยแล้ว");
                return true;
            }else if (args.length == 1 && args[0].equalsIgnoreCase("setautoafk")) {
                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();

                dataConfig.set("mainautoafkzone.world", world);
                dataConfig.set("mainautoafkzone.x", x);
                dataConfig.set("mainautoafkzone.y", y);
                dataConfig.set("mainautoafkzone.z", z);

                saveDataConfig();
                loadPoints();

                player.sendMessage("§8(§3§lTONKUNG§8) §7จุด Auto AFK ได้ถูกบันทึกแล้ว ณ ตำแหน่ง: " +
                        "World: " + world + ", X: " + x + ", Y: " + y + ", Z: " + z);
                return true;
            }



            player.sendMessage("§8(§3§lTONKUNG§8) §7ใช้: /afks [set <ชื่อมาร์ค หรือ delete <ชื่อมาร์ค>]");
            return false;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.BONE && event.getClickedBlock() != null) {
            Player player = event.getPlayer();
            Location clickedLocation = event.getClickedBlock().getLocation();
            if(player.isOp()){
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (point1 == null) {
                        point1 = clickedLocation;
                        player.sendMessage("§8(§3§lTONKUNG§8) §7จุดแรกที่มาร์ค คือ " + locationToString(point1));
                        event.setCancelled(true);
                    } else if (point1.equals(clickedLocation)) {
                        player.sendMessage("§8(§3§lTONKUNG§8) §7คุณกำลังมาร์คที่จุดเดิม");
                        event.setCancelled(true);
                    } else {
                        point1 = clickedLocation;
                        player.sendMessage("§8(§3§lTONKUNG§8) §7คุณเปลี่ยนจุดมาร์คแรก เป็น " + locationToString(point1));
                        event.setCancelled(true);
                    }
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (point1 == null) {
                        player.sendMessage("§8(§3§lTONKUNG§8) §7คุณต้องมาร์คจุดแรกก่อน");
                        event.setCancelled(true);
                    } else if (point2 == null) {
                        point2 = clickedLocation;
                        player.sendMessage("§8(§3§lTONKUNG§8) §7จุดสองที่มาร์ค คือ " + locationToString(point2));
                        event.setCancelled(true);
                    } else if (point2.equals(clickedLocation)) {
                        player.sendMessage("§8(§3§lTONKUNG§8) §7คุณกำลังมาร์คที่จุดเดิม");
                        event.setCancelled(true);
                    } else {
                        point2 = clickedLocation;
                        player.sendMessage("§8(§3§lTONKUNG§8) §7คุณเปลี่ยนจุดมาร์คสอง เป็น " + locationToString(point2));
                        event.setCancelled(true);
                    }
                }
            }else{
                event.setCancelled(true);
            }

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startafk")) {
            if (args.length == 0) {
                // AFK the player who issued the command
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    // Check if player has permission to use this command
                    String world = dataConfig.getString("mainautoafkzone.world");

                    String xStr = dataConfig.getString("mainautoafkzone.x");
                    String yStr = dataConfig.getString("mainautoafkzone.y");
                    String zStr = dataConfig.getString("mainautoafkzone.z");

                    if (world == null || world.isEmpty() || Bukkit.getWorld(world) == null) {
                        sender.sendMessage("§8(§3§lTONKUNG§8) §cไม่พบข้อมูล world หรือ world นี้ไม่ถูกต้อง");
                        return true;
                    }

                    if (xStr == null || xStr.isEmpty() || yStr == null || yStr.isEmpty() || zStr == null || zStr.isEmpty()) {
                        sender.sendMessage("§8(§3§lTONKUNG§8) §cพิกัด x, y, z ไม่ถูกต้องหรือยังไม่ได้ตั้งค่า");
                        return true;
                    }
                    double x = dataConfig.getDouble("mainautoafkzone.x");
                    double y = dataConfig.getDouble("mainautoafkzone.y");
                    double z = dataConfig.getDouble("mainautoafkzone.z");


                    if (player.hasPermission("ton.antiafkplayer.startafk")) {
                        player.teleport(new Location(Bukkit.getWorld(world), x, y, z));
                        player.sendMessage(ChatColor.GREEN + "คุณเข้าสู่สถานะ AFK แล้ว.");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "คุณไม่มีสิทธิ์ใช้คำสั่งนี้.");
                        return true;
                    }
                } else {
                    sender.sendMessage("คำสั่งนี้ใช้ได้เฉพาะผู้เล่นเท่านั้น.");
                    return true;
                }
            } else if (args.length == 1) {

                Player targetPlayer = Bukkit.getPlayer(args[0]);
                String world = dataConfig.getString("mainautoafkzone.world");
                String xStr = dataConfig.getString("mainautoafkzone.x");
                String yStr = dataConfig.getString("mainautoafkzone.y");
                String zStr = dataConfig.getString("mainautoafkzone.z");

                // ตรวจสอบ world และ xyz ว่ามีค่าและเป็นค่าที่ถูกต้องหรือไม่
                if (world == null || world.isEmpty() || Bukkit.getWorld(world) == null) {
                    sender.sendMessage("§8(§3§lTONKUNG§8) §cไม่พบข้อมูล world หรือ world นี้ไม่ถูกต้อง");
                    return true;
                }

                if (xStr == null || xStr.isEmpty() || yStr == null || yStr.isEmpty() || zStr == null || zStr.isEmpty()) {
                    sender.sendMessage("§8(§3§lTONKUNG§8) §cพิกัด x, y, z ไม่ถูกต้องหรือยังไม่ได้ตั้งค่า");
                    return true;
                }
                double x = dataConfig.getDouble("mainautoafkzone.x");
                double y = dataConfig.getDouble("mainautoafkzone.y");
                double z = dataConfig.getDouble("mainautoafkzone.z");


                if (targetPlayer != null) {
                    if (sender.hasPermission("ton.antiafkplayer.startafk.others")) {
                        targetPlayer.teleport(new Location(Bukkit.getWorld(world), x, y, z));
                        sender.sendMessage(ChatColor.GREEN + "คุณได้ตั้งผู้เล่น " + targetPlayer.getName() + " ให้เข้าสู่สถานะ AFK แล้ว.");
                        targetPlayer.sendMessage(ChatColor.RED + "คุณถูกตั้งค่าให้เข้าสู่สถานะ AFK โดย " + sender.getName() + ".");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "คุณไม่มีสิทธิ์ใช้คำสั่งนี้กับผู้เล่นคนอื่น.");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "ไม่พบผู้เล่นที่มีชื่อ " + args[0] + ".");
                    return true;
                }
            }
        }
        return false;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity(); // รับผู้เล่นที่ตาย

        // ตรวจสอบว่าผู้เล่นตายที่จุดแรกหรือจุดที่สองของพื้นที่
        for (Location[] locs : points.values()) {
            Location point1 = locs[0];
            Location point2 = locs[1];

            // ตรวจสอบว่าผู้เล่นตายที่จุดแรกหรือจุดที่สอง
            if (isInRegion(player.getLocation(), point1, point2)) {
                // ลบข้อมูลของผู้เล่น
                BukkitRunnable countdownTask = countdownTasks.get(player);
                if (countdownTask != null) {
                    countdownTask.cancel();
                    countdownTasks.remove(player);
                }

                playerInRegion.remove(player); // ลบผู้เล่นออกจากการบันทึก
//                player.sendMessage("§8(§3§lTONKUNG§8) §7คุณตายที่จุดที่กำหนดและข้อมูลของคุณถูกลบออกแล้ว!");
                break; // ออกจากลูปเมื่อเจอพื้นที่
            }
        }
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer(); // ดึงข้อมูลของผู้เล่นที่ออกจากเซิร์ฟเวอร์
        for (Location[] locs : points.values()) { // วนลูปเช็คทุกพื้นที่ที่มีการกำหนดไว้ในระบบ
            Location point1 = locs[0]; // จุดแรกของพื้นที่
            Location point2 = locs[1]; // จุดที่สองของพื้นที่
            if (isInRegion(player.getLocation(), point1, point2)) { // ตรวจสอบว่าผู้เล่นอยู่ในพื้นที่หรือไม่
                BukkitRunnable countdownTask = countdownTasks.get(player); // ดึง task นับเวลาที่เกี่ยวข้องกับผู้เล่น
                if (countdownTask != null) { // ตรวจสอบว่ามี task นับเวลาหรือไม่
                    countdownTask.cancel(); // ยกเลิก task นับเวลา
                    countdownTasks.remove(player); // ลบ task ออกจากรายการ
                }
                playerInRegion.remove(player); // ลบข้อมูลผู้เล่นที่อยู่ในพื้นที่ออกจากการบันทึก
                break; // ออกจากลูปเพราะเจอพื้นที่แล้ว ไม่ต้องเช็คพื้นที่อื่นต่อ
            }
        }
    }

    private void afkPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerLocations.containsKey(playerId)) {

            String world = dataConfig.getString("mainautoafkzone.world");


            String xStr = dataConfig.getString("mainautoafkzone.x");
            String yStr = dataConfig.getString("mainautoafkzone.y");
            String zStr = dataConfig.getString("mainautoafkzone.z");

            if (world == null || world.isEmpty() || Bukkit.getWorld(world) == null) {
                player.sendMessage("§8(§3§lTONKUNG§8) §cไม่พบข้อมูล world หรือ world นี้ไม่ถูกต้อง");
                return;
            }

            if (xStr == null || xStr.isEmpty() || yStr == null || yStr.isEmpty() || zStr == null || zStr.isEmpty()) {
                player.sendMessage("§8(§3§lTONKUNG§8) §cพิกัด x, y, z ไม่ถูกต้องหรือยังไม่ได้ตั้งค่า");
                return;
            }
            double x = dataConfig.getDouble("mainautoafkzone.x");
            double y = dataConfig.getDouble("mainautoafkzone.y");
            double z = dataConfig.getDouble("mainautoafkzone.z");

            playerLocations.put(playerId, player.getLocation());
            player.teleport(new Location(Bukkit.getWorld(world), x, y, z));
            String titleautoafk1 = dataConfig.getString("title_autoafk_1");
            String titleautoafk2 = dataConfig.getString("title_autoafk_2");
            String alreadyafkmessage = dataConfig.getString("already_afk_message");
            if (titleautoafk1 == null) {
                titleautoafk1 = "§eคุณกำลัง AFK อยู่!";
            }
            if (titleautoafk2 == null) {
                titleautoafk2 = "§aขยับตัวเพื่อออกจากสถานะ AFK";
            }
            if (alreadyafkmessage == null) {
                alreadyafkmessage = "§cคุณติดสถานะ AFK";
            }

            player.sendTitle(titleautoafk1, titleautoafk2, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            player.sendMessage(alreadyafkmessage);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
        }
    }

    private void startAfkCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();

                    // Check if the player has the bypass permission using LuckPerms
                    User user = luckPerms.getUserManager().getUser(playerId);
                    if (user != null && user.getCachedData().getPermissionData().checkPermission("bypass.tonkungny.antiafk").asBoolean()) {
                        continue;
                    }

                    if (!afkTimers.containsKey(playerId)) {
                        afkTimers.put(playerId, System.currentTimeMillis());
                    } else {
                        long lastActive = afkTimers.get(playerId);
                        String autoafktime_config = dataConfig.getString("auto_afk_time");

                        if (autoafktime_config == null) {
                            autoafktime_config = "900";
                        }
                        int autoafktime = Integer.parseInt(autoafktime_config) * 1000;

                        if (System.currentTimeMillis() - lastActive > autoafktime) {
                            for (Location[] locs : points.values()) {
                                Location point1 = locs[0];
                                Location point2 = locs[1];

                                if (isInRegion(player.getLocation(), point1, point2)) {
                                    return;
                                }
                            }
                            if (!playerLocations.containsKey(playerId)) {
                                String world = dataConfig.getString("mainautoafkzone.world");

                                String xStr = dataConfig.getString("mainautoafkzone.x");
                                String yStr = dataConfig.getString("mainautoafkzone.y");
                                String zStr = dataConfig.getString("mainautoafkzone.z");

                                if (world == null || world.isEmpty() || Bukkit.getWorld(world) == null) {
                                    player.sendMessage("§8(§3§lTONKUNG§8) §cไม่พบข้อมูล world หรือ world นี้ไม่ถูกต้อง");
                                    return;
                                }

                                if (xStr == null || xStr.isEmpty() || yStr == null || yStr.isEmpty() || zStr == null || zStr.isEmpty()) {
                                    player.sendMessage("§8(§3§lTONKUNG§8) §cพิกัด x, y, z ไม่ถูกต้องหรือยังไม่ได้ตั้งค่า");
                                    return;
                                }
                                double x = dataConfig.getDouble("mainautoafkzone.x");
                                double y = dataConfig.getDouble("mainautoafkzone.y");
                                double z = dataConfig.getDouble("mainautoafkzone.z");
                                playerLocations.put(playerId, player.getLocation());
                                player.teleport(new Location(Bukkit.getWorld(world), x, y, z));
                                String titleautoafk1 = dataConfig.getString("title_autoafk_1");
                                String titleautoafk2 = dataConfig.getString("title_autoafk_2");
                                String alreadyafkmessage = dataConfig.getString("already_afk_message");
                                if (titleautoafk1 == null) {
                                    titleautoafk1 = "§eคุณกำลัง AFK อยู่!";
                                }
                                if (titleautoafk2 == null) {
                                    titleautoafk2 = "§aขยับตัวเพื่อออกจากสถานะ AFK";
                                }
                                if (alreadyafkmessage == null) {
                                    alreadyafkmessage = "§cคุณติดสถานะ AFK";
                                }

                                player.sendTitle(titleautoafk1, titleautoafk2, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
                                player.sendMessage(alreadyafkmessage);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    private void loadPlayerLocations() {
        if (!dataFiles.exists()) return;

        try (FileInputStream fis = new FileInputStream(dataFiles)) {
            byte[] data = new byte[(int) dataFiles.length()];
            fis.read(data);
            String json = new String(data, "UTF-8");

            // ตรวจสอบว่า JSON ถูกต้องหรือไม่
            if (json.isEmpty() || json.equals("{}")) {
                getLogger().warning("playerLocations.json is empty or invalid, initializing with an empty JSONObject.");
                return; // หากว่างเปล่าหรือไม่ถูกต้อง, ไม่ต้องทำอะไร
            }

            JSONObject jsonObject = new JSONObject(json);

            for (String key : jsonObject.keySet()) {
                JSONObject loc = jsonObject.getJSONObject(key);
                Location location = new Location(
                        Bukkit.getWorld(loc.getString("world")),
                        loc.getDouble("x"),
                        loc.getDouble("y"),
                        loc.getDouble("z"),
                        (float) loc.getDouble("yaw"),
                        (float) loc.getDouble("pitch")
                );
                playerLocations.put(UUID.fromString(key), location);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            getLogger().severe("Failed to load player locations: " + e.getMessage());
        }
    }


    private void savePlayerLocations() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<UUID, Location> entry : playerLocations.entrySet()) {
            UUID playerId = entry.getKey();
            Location loc = entry.getValue();
            JSONObject locJson = new JSONObject();
            locJson.put("world", loc.getWorld().getName());
            locJson.put("x", loc.getX());
            locJson.put("y", loc.getY());
            locJson.put("z", loc.getZ());
            locJson.put("yaw", loc.getYaw());
            locJson.put("pitch", loc.getPitch());
            jsonObject.put(playerId.toString(), locJson);
        }

        try (FileWriter writer = new FileWriter(dataFiles)) {
            writer.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class PlayerListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            afkTimers.put(playerId, System.currentTimeMillis());

            if (playerLocations.containsKey(playerId)) {
                Location originalLocation = playerLocations.remove(playerId);
                player.teleport(originalLocation);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.sendTitle("", "", 10, 70, 20); // Clear title when returning
                String autoafkbackmessage = dataConfig.getString("afk_auto_back_location_message");

                if (autoafkbackmessage == null) {
                    autoafkbackmessage = "§aคุณได้ออกจากสถานะ AFK แล้ว ระบบได้นำคุณกลับมายังจุดเดิม";
                }
                player.sendMessage(autoafkbackmessage);
            }
        }

        @org.bukkit.event.EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            UUID playerId = event.getPlayer().getUniqueId();
            Player player = event.getPlayer();
            if (playerLocations.containsKey(playerId)) {
                Location originalLocation = playerLocations.remove(playerId);
                player.teleport(originalLocation);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.sendTitle("", "", 10, 70, 20); // Clear title when returning
                String autoafkbackmessage = dataConfig.getString("afk_auto_back_location_message");

                if (autoafkbackmessage == null) {
                    autoafkbackmessage = "§aคุณได้ออกจากสถานะ AFK แล้ว ระบบได้นำคุณกลับมายังจุดเดิม";
                }
                player.sendMessage(autoafkbackmessage);
            }
            afkTimers.remove(playerId);
            playerLocations.remove(playerId);
        }
    }

    private void removePlayerFromRegion(Player player) {
        for (Location[] locs : points.values()) {
            Location point1 = locs[0];
            Location point2 = locs[1];

            if (isInRegion(player.getLocation(), point1, point2)) {
                return; // ถ้าผู้เล่นยังอยู่ในพื้นที่ ไม่ทำอะไร
            }
        }

        // ยกเลิกการทำงานของ task นับเวลา
        BukkitRunnable countdownTask = countdownTasks.get(player);
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTasks.remove(player);
        }


        playerInRegion.remove(player); // ลบผู้เล่นออกจากการบันทึก
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        boolean isInRegion = false;

        for (Map.Entry<String, Location[]> entry : points.entrySet()) {
            Location[] locs = entry.getValue();
            Location point1 = locs[0];
            Location point2 = locs[1];

            if (isInRegion(playerLocation, point1, point2)) {
                isInRegion = true;
                if (!playerInRegion.getOrDefault(player, false)) {
                    startCooldown(player, point1, point2); // ส่ง point1 และ point2 เข้าไปในฟังก์ชัน

                    playerInRegion.put(player, true);
                }
                break;
            }
        }

        if (!isInRegion) {
            removePlayerFromRegion(player);
            BukkitRunnable countdownTask = countdownTasks.get(player);
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTasks.remove(player);
                String actionbaroutregion = dataConfig.getString("out_region_actionbar");

                if (actionbaroutregion == null) {
                    actionbaroutregion = "§cคุณออกนอกบริเวณ!";
                }

                player.sendTitle(actionbaroutregion, "", 10, 20, 10);
            }
            playerInRegion.put(player, false);
        }
    }
    private String findPointName(Location point1, Location point2) {
        for (Map.Entry<String, Location[]> entry : points.entrySet()) {
            Location[] locs = entry.getValue();
            if (locs[0].equals(point1) && locs[1].equals(point2)) {
                return entry.getKey(); // คืนค่า pointName
            }
        }
        return null; // ถ้าไม่เจอ pointName
    }

    private String formatTimeLeftWithVariables(int timeLeftInSeconds, String messageTemplate) {
        int hours = timeLeftInSeconds / 3600;
        int minutes = (timeLeftInSeconds % 3600) / 60;
        int seconds = timeLeftInSeconds % 60;

        return messageTemplate
                .replace("<h>", String.valueOf(hours))
                .replace("<m>", String.valueOf(minutes))
                .replace("<s>", String.valueOf(seconds));
    }


    private void startCooldown(Player player, Location point1, Location point2) {
//        int cooldownTime = 20; // ตั้งค่าเวลา cooldown เป็น 20 วินาที

        hasEnteredAFKZone.put(player, true);
        String pointName = findPointName(point1, point2);
        if (pointName == null) {
            player.sendMessage(ChatColor.RED + "ไม่พบโซน AFK ที่คุณกำหนด!");
            return;
        }
        int cooldownTime = dataConfig.getInt("points." + pointName + ".cooldownafk");
        BukkitRunnable cooldownTask = new BukkitRunnable() {
            int timeLeft = cooldownTime;

            @Override
            public void run() {
                if (!isInRegion(player.getLocation(), point1, point2)) {
                    String actionbaroutregion = dataConfig.getString("out_region_actionbar");

                    if (actionbaroutregion == null) {
                        actionbaroutregion = "§cคุณออกนอกบริเวณ!";
                    }

                    player.sendTitle(actionbaroutregion, "", 10, 20, 10);
                    cancel();
                    playerInRegion.put(player, false);
                    return;
                }

                if (timeLeft > 0) {
                    int totalBars = 6;
                    int filledBars = (int) Math.ceil((timeLeft / (double) cooldownTime) * totalBars);
                    StringBuilder progressBar = new StringBuilder();
                    String barColor;

                    double timeLeftPercentage = (timeLeft / (double) cooldownTime) * 100;

                    if (timeLeftPercentage <= 10) {
                        String tenpersen_color = dataConfig.getString("10persen_color");
                        if (tenpersen_color == null) {
                            tenpersen_color = "§a";
                        }
                        barColor = tenpersen_color;
                    } else if (timeLeftPercentage <= 50) {
                        String fivetenpersen_color = dataConfig.getString("50persen_color");
                        if (fivetenpersen_color == null) {
                            fivetenpersen_color = "§e";
                        }
                        barColor = fivetenpersen_color;
                    } else {
                        String morefivetenpersen = dataConfig.getString("more50persen_color");
                        if (morefivetenpersen == null) {
                            morefivetenpersen = "§c";
                        }
                        barColor = morefivetenpersen;
                    }
                    String forward_icon = dataConfig.getString("forward_icon");
                    if (forward_icon == null) {
                        forward_icon = "[";
                    }
                    progressBar.append(ChatColor.GRAY).append(forward_icon);
                    for (int i = 0; i < totalBars; i++) {
                        if (i < filledBars) {
                            String icon = dataConfig.getString("icon");
                            if (icon == null) {
                                icon = ":";
                            }
                            progressBar.append(barColor).append(icon);
                        } else {
                            String icon2 = dataConfig.getString("icon");
                            if (icon2 == null) {
                                icon2 = ":";
                            }
                            String oldiconcolor = dataConfig.getString("old_icon_color");
                            if (icon2 == null) {
                                icon2 = "§7";
                            }
                            progressBar.append(oldiconcolor).append(icon2);
                        }
                    }
                    String backward_icon = dataConfig.getString("backward_icon");
                    if (backward_icon == null) {
                        backward_icon = "]";
                    }
                    progressBar.append(ChatColor.GRAY).append(backward_icon);
                    String actionbaroutregion = dataConfig.getString("afk_title");
                    if (actionbaroutregion == null) {
                        actionbaroutregion = "§fเหลืออีก <h> ชั่วโมง <m> นาที <s> วินาที คุณจะได้รางวัล";
                    }
                    actionbaroutregion = formatTimeLeftWithVariables(timeLeft, actionbaroutregion);
                    playerInRegion.put(player, true);
                    player.sendTitle(progressBar.toString(), actionbaroutregion, 10, 20, 10);
                    timeLeft--;
                } else {
                    executeCommands(player, pointName);
                    String suc1 = dataConfig.getString("success_title_1");
                    String suc2 = dataConfig.getString("success_title_2");

                    if (suc1 == null) {
                        suc1 = "§aคุณได้รับรางวัล AFK แล้ว!";
                    }
                    if (suc2 == null) {
                        suc2 = "§aโคตรสุดยอดเลยเว้ย";
                    }
                    player.sendTitle(suc1,suc2,10, 20, 10);

                    if (isInRegion(player.getLocation(), point1, point2)) {

                        timeLeft = cooldownTime;
                        String actionbaroutregion2 = dataConfig.getString("out_region_in_actionbar");

                        if (actionbaroutregion2 == null) {
                            actionbaroutregion2 = "§aคุณยังอยู่ในโซน AFK ระบบจะเริ่มคูลดาวน์รอบใหม่!";
                        }
                        player.sendMessage(actionbaroutregion2);
                    } else {

                        cancel();
                        playerInRegion.put(player, false);
                    }
                }
            }
        };

        countdownTasks.put(player, cooldownTask);
        cooldownTask.runTaskTimer(this, 0L, 20L); // เรียกใช้ทุก ๆ 20 ticks (1 วินาที)
    }

    private void executeCommands(Player player, String pointName) {
        List<String> commands = dataConfig.getStringList("points." + pointName + ".command");
        String playerName = player.getName();

        for (String command : commands) {
            String finalCommand = command.replace("<playerName>", playerName);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }
    }
    private boolean isInRegion(Location playerLocation, Location point1, Location point2) {
        double minX = Math.min(point1.getX(), point2.getX());
        double maxX = Math.max(point1.getX(), point2.getX());
        double minY = Math.min(point1.getY(), point2.getY());
        double maxY = Math.max(point1.getY(), point2.getY());
        double minZ = Math.min(point1.getZ(), point2.getZ());
        double maxZ = Math.max(point1.getZ(), point2.getZ());

        return playerLocation.getWorld().equals(point1.getWorld()) &&
                playerLocation.getX() >= minX && playerLocation.getX() <= maxX &&
                playerLocation.getY() >= minY && playerLocation.getY() <= maxY &&
                playerLocation.getZ() >= minZ && playerLocation.getZ() <= maxZ;
    }



}
