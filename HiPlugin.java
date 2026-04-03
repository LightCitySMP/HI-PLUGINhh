package net.hiplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HiPlugin extends JavaPlugin {

    private File logFile;
    private Queue<String> logQueue;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        logQueue = new ConcurrentLinkedQueue<>();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        logFile = new File(getDataFolder(), "history.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Failed to generate history file");
            }
        }

        HiCommand cmdClass = new HiCommand(this);
        getCommand("hi").setExecutor(cmdClass);
        getCommand("hi").setTabCompleter(cmdClass);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            flushLogs();
        }, 1200L, 1200L);
    }

    @Override
    public void onDisable() {
        flushLogs();
    }

    public void logAction(String pName, String cmdUsed) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logQueue.add("[" + time + "] " + pName + " executed " + cmdUsed);
    }

    private void flushLogs() {
        if (logQueue.isEmpty()) return;

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String line;
            while ((line = logQueue.poll()) != null) {
                pw.println(line);
            }
        } catch (IOException e) {
            getLogger().warning("Could not flush log queue to file");
        }
    }
}