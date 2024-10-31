package org.parkourpluginsaveiteminworld.tonkungz.afksystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AFKCommandTabCompleter implements TabCompleter {

    private final FileConfiguration dataConfig;

    public AFKCommandTabCompleter(FileConfiguration dataConfig) {
        this.dataConfig = dataConfig;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // ถ้าผู้เล่นพิมพ์ "/afks" แล้วกด Tab
        if (command.getName().equalsIgnoreCase("afks")) {
            // ตรวจสอบว่าเป็นการพิมพ์คำสั่งหลังจาก "/afks "
            if (args.length == 1) {
                // คืนค่าคำสั่งที่จะแสดงให้เลือก
                List<String> subCommands = Arrays.asList("set", "delete", "reload", "setautoafk");
                List<String> completions = new ArrayList<>();
                // เพิ่มการแสดงผลที่ตรงกับข้อความที่พิมพ์
                for (String subCommand : subCommands) {
                    if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(subCommand);
                    }
                }
                return completions;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
                // แสดง key ที่อยู่ใน points จาก dataConfig
                if (dataConfig.contains("points")) {
                    Set<String> keys = dataConfig.getConfigurationSection("points").getKeys(false);
                    List<String> completions = new ArrayList<>(keys);
                    return completions; // คืนค่าคีย์จาก points section ใน dataConfig
                }
            }
        }
        return null; // ถ้าไม่มีการเติมคำสั่งที่ตรง
    }
}
