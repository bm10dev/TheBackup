package bm10.ch.thebackup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TheBackup extends JavaPlugin {
    HashMap<CommandSender, Integer> restoreconf = new HashMap();
    HashMap<CommandSender, Integer> deleteconf = new HashMap();
    public static Plugin plugin;
    public static FileConfiguration config;
    public static FileConfiguration lang;
    public static File backupfolder;
    public static String prefix;

    public TheBackup() {
    }

    public void onEnable() {
        if (!this.getDataFolder().isDirectory()) {
            BackupUtils.setFilePerms(this.getDataFolder());
            this.getDataFolder().mkdir();
            BackupUtils.setFilePerms(this.getDataFolder());
        }

        File conffile = new File(this.getDataFolder(), "config.yml");
        if (!conffile.isFile()) {
            BackupUtils.setFilePerms(conffile);
            this.saveDefaultConfig();
            BackupUtils.setFilePerms(conffile);
        }

        File langfile = new File(this.getDataFolder(), "language.yml");
        if (!langfile.isFile()) {
            BackupUtils.setFilePerms(langfile);
            this.saveResource(langfile.getName(), false);
            BackupUtils.setFilePerms(langfile);
        }

        plugin = this;
        config = this.getConfig();
        lang = YamlConfiguration.loadConfiguration(langfile);
        prefix = ChatColor.translateAlternateColorCodes('&', lang.getString("prefix").replace("\\n", "\n"));
        BackupRestorer.log = config.getBoolean("debug");
        backupfolder = new File(config.getString("localpath"));
        if (!backupfolder.isDirectory()) {
            BackupUtils.setFilePerms(backupfolder);
            backupfolder.mkdirs();
            BackupUtils.setFilePerms(backupfolder);
        }

        try {
            (new MetricsLite(plugin)).start();
        } catch (IOException var5) {
            Bukkit.getLogger().info(ChatColor.stripColor(prefix) + "Error while loading MetricsLite:");
            var5.printStackTrace();
        }

        if (config.isString("interval") && config.getString("interval").matches("[0-9]+ [tsmhd]")) {
            long unit = Long.parseLong(config.getString("interval").split(" ")[0]);
            if (config.getString("interval").split(" ")[1].equals("s")) {
                unit *= 20L;
            } else if (config.getString("interval").split(" ")[1].equals("m")) {
                unit *= 1200L;
            } else if (config.getString("interval").split(" ")[1].equals("h")) {
                unit *= 72000L;
            } else if (config.getString("interval").split(" ")[1].equals("d")) {
                unit *= 1728000L;
            }

            (new BukkitRunnable() {
                public void run() {
                    if (TheBackup.plugin.isEnabled()) {
                        String message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("started").replace("\\n", "\n"));
                        TheBackup.this.getLogger().info(ChatColor.stripColor(message));
                        Iterator var3 = Bukkit.getOnlinePlayers().iterator();

                        while(true) {
                            Player player;
                            do {
                                if (!var3.hasNext()) {
                                    if (BackupUtils.createBackup() != null) {
                                        message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("ended").replace("\\n", "\n"));
                                        TheBackup.this.getLogger().info(ChatColor.stripColor(message));
                                        var3 = Bukkit.getOnlinePlayers().iterator();

                                        while(true) {
                                            do {
                                                if (!var3.hasNext()) {
                                                    return;
                                                }

                                                player = (Player)var3.next();
                                            } while(!TheBackup.config.getBoolean("broadcast") && !player.hasPermission("TheBackup.warning"));

                                            player.sendMessage(TheBackup.prefix + message);
                                        }
                                    } else {
                                        message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("failed").replace("\\n", "\n"));
                                        TheBackup.this.getLogger().warning(ChatColor.stripColor(message));
                                        var3 = Bukkit.getOnlinePlayers().iterator();

                                        while(true) {
                                            do {
                                                if (!var3.hasNext()) {
                                                    return;
                                                }

                                                player = (Player)var3.next();
                                            } while(!TheBackup.config.getBoolean("broadcast") && !player.hasPermission("TheBackup.warning"));

                                            player.sendMessage(TheBackup.prefix + message);
                                        }
                                    }
                                }

                                player = (Player)var3.next();
                            } while(!TheBackup.config.getBoolean("broadcast") && !player.hasPermission("TheBackup.warning"));

                            player.sendMessage(TheBackup.prefix + message);
                        }
                    } else {
                        this.cancel();
                    }
                }
            }).runTaskTimerAsynchronously(plugin, unit, unit);
        }

        if (config.isString("crontask")) {
            CronRunnable var10000 = new CronRunnable(config.getString("crontask")) {
                public void exec() {
                    if (TheBackup.plugin.isEnabled()) {
                        String message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("started").replace("\\n", "\n"));
                        TheBackup.this.getLogger().info(ChatColor.stripColor(message));
                        Iterator var3 = Bukkit.getOnlinePlayers().iterator();

                        while(true) {
                            Player player;
                            do {
                                if (!var3.hasNext()) {
                                    if (BackupUtils.createBackup() != null) {
                                        message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("ended").replace("\\n", "\n"));
                                        TheBackup.this.getLogger().info(ChatColor.stripColor(message));
                                        var3 = Bukkit.getOnlinePlayers().iterator();

                                        while(true) {
                                            do {
                                                if (!var3.hasNext()) {
                                                    return;
                                                }

                                                player = (Player)var3.next();
                                            } while(!TheBackup.config.getBoolean("broadcast") && !player.hasPermission("TheBackup.warning"));

                                            player.sendMessage(TheBackup.prefix + message);
                                        }
                                    } else {
                                        message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("failed").replace("\\n", "\n"));
                                        TheBackup.this.getLogger().warning(ChatColor.stripColor(message));
                                        var3 = Bukkit.getOnlinePlayers().iterator();

                                        while(true) {
                                            do {
                                                if (!var3.hasNext()) {
                                                    return;
                                                }

                                                player = (Player)var3.next();
                                            } while(!TheBackup.config.getBoolean("broadcast") && !player.hasPermission("TheBackup.warning"));

                                            player.sendMessage(TheBackup.prefix + message);
                                        }
                                    }
                                }

                                player = (Player)var3.next();
                            } while(!TheBackup.config.getBoolean("broadcast") && !player.hasPermission("TheBackup.warning"));

                            player.sendMessage(TheBackup.prefix + message);
                        }
                    } else {
                        this.cancel();
                    }
                }
            };
        }

    }

    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        int i;
        if (cmd.getLabel().equals("backups")) {
            if (sender.hasPermission("TheBackup.list")) {
                File[] files = BackupUtils.listBackups();
                if (files.length > 0) {
                    if (args.length != 0 && !args[0].matches("[0-9]+")) {
                        sender.sendMessage(prefix + ChatColor.RED + "Invalid page number, must be a positive number.");
                    } else {
                        long page = args.length > 0 ? Long.parseLong(args[0]) : 1L;
                        int total = (int)Math.ceil((double)files.length / 10.0D);
                        if (page <= (long)total) {
                            sender.sendMessage(prefix + files.length + " backups created (" + ChatColor.RED + "page " + page + "/" + total + ChatColor.RESET + "):");
                            i = 0;
                            File[] var13 = files;
                            int var12 = files.length;

                            for(int var11 = 0; var11 < var12; ++var11) {
                                File backup = var13[var11];
                                ++i;
                                if ((long)i > page * 10L) {
                                    break;
                                }

                                if ((long)i > (page - 1L) * 10L) {
                                    String size = BackupUtils.bytesToString(backup.length());
                                    sender.sendMessage(files.length - i + 1 + ". \"" + ChatColor.ITALIC + backup.getName() + ChatColor.RESET + "\" (" + size + ")");
                                }
                            }
                        } else {
                            sender.sendMessage(prefix + ChatColor.RED + "Invalid page number, it is out of range.");
                        }
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "No backup has been created yet.");
                }
            } else {
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', lang.getString("permission").replace("\\n", "\n")));
            }
        } else if (cmd.getLabel().equals("backup")) {
            if (sender.hasPermission("TheBackup.backup")) {
                String message = ChatColor.translateAlternateColorCodes('&', lang.getString("manualstart").replace("\\n", "\n"));
                this.getLogger().info(ChatColor.stripColor(message));
                Iterator var7 = Bukkit.getOnlinePlayers().iterator();

                while(true) {
                    Player player;
                    do {
                        if (!var7.hasNext()) {
                            (new Thread((ThreadGroup)null, new Runnable() {
                                public void run() {
                                    Iterator var2 = Bukkit.getWorlds().iterator();

                                    while(var2.hasNext()) {
                                        World world = (World)var2.next();
                                        world.save();
                                    }

                                    File backup = BackupUtils.createBackup();
                                    Player player;
                                    Iterator var4;
                                    String message;
                                    if (backup != null) {
                                        message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("manualend").replace("\\n", "\n"));
                                        TheBackup.this.getLogger().info(ChatColor.stripColor(message));
                                        var4 = Bukkit.getOnlinePlayers().iterator();

                                        while(true) {
                                            do {
                                                if (!var4.hasNext()) {
                                                    if (backup.exists() && backup.isDirectory() || backup.isFile()) {
                                                        sender.sendMessage(TheBackup.prefix + "\"" + backup.getName() + "\" (" + BackupUtils.bytesToString(backup.length()) + ")");
                                                    }

                                                    return;
                                                }

                                                player = (Player)var4.next();
                                            } while(!TheBackup.config.getBoolean("manualbroadcast") && !player.hasPermission("TheBackup.warning"));

                                            player.sendMessage(TheBackup.prefix + message);
                                        }
                                    } else {
                                        message = ChatColor.translateAlternateColorCodes('&', TheBackup.lang.getString("manualfail").replace("\\n", "\n"));
                                        TheBackup.this.getLogger().info(ChatColor.stripColor(message));
                                        var4 = Bukkit.getOnlinePlayers().iterator();

                                        while(true) {
                                            do {
                                                if (!var4.hasNext()) {
                                                    return;
                                                }

                                                player = (Player)var4.next();
                                            } while(!TheBackup.config.getBoolean("manualbroadcast") && !player.hasPermission("TheBackup.warning"));

                                            player.sendMessage(TheBackup.prefix + message);
                                        }
                                    }
                                }
                            }, "TheBackup")).start();
                            return false;
                        }

                        player = (Player)var7.next();
                    } while(!config.getBoolean("manualbroadcast") && !player.hasPermission("TheBackup.warning"));

                    player.sendMessage(prefix + message);
                }
            } else {
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', lang.getString("permission").replace("\\n", "\n")));
            }
        } else {
            int id;
            File zip;
            if (cmd.getLabel().equals("restore")) {
                if (sender.hasPermission("TheBackup.restore")) {
                    if (args.length != 0 && (!args[0].matches("[0-9]{0,9}") || Integer.parseInt(args[0]) <= 0)) {
                        sender.sendMessage(prefix + ChatColor.RED + "Invalid backup, id must be a positive integer.");
                    } else if (args.length != 0 && Integer.parseInt(args[0]) > BackupUtils.listBackups().length) {
                        sender.sendMessage(prefix + ChatColor.RED + "Invalid backup, id out of range.");
                    } else if (!this.restoreconf.containsKey(sender)) {
                        id = args.length > 0 ? Integer.parseInt(args[0]) : BackupUtils.listBackups().length;
                        if (id > 0) {
                            sender.sendMessage(prefix + "Do you REALLY want to restore backup #" + id + "?");
                            sender.sendMessage(prefix + "Your server will be stopped. This cannot be undone!");
                            sender.sendMessage(prefix + "If you want to continue type /restore within 10s.");
                            this.restoreconf.put(sender, id);
                            (new BukkitRunnable() {
                                public void run() {
                                    if (TheBackup.this.restoreconf.remove(sender) != null) {
                                        sender.sendMessage(TheBackup.prefix + "The backup restore has been cancelled.");
                                    }

                                }
                            }).runTaskLater(plugin, 200L);
                        } else {
                            sender.sendMessage(prefix + ChatColor.RED + "No backup has been created yet.");
                        }
                    } else {
                        id = (Integer)this.restoreconf.remove(sender);
                        sender.sendMessage(prefix + "Restoring the backup #" + id + ", please wait...");

                        try {
                            zip = BackupUtils.listBackups()[id - 1];
                            File tmp = new File("");
                            Iterator var25 = Bukkit.getOnlinePlayers().iterator();

                            while(var25.hasNext()) {
                                Player player = (Player)var25.next();
                                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', lang.getString("kick").replace("\\n", "\n")));
                            }

                            BackupRestorer.main(new String[]{zip.getAbsolutePath(), tmp.getAbsolutePath(), "true"});
                            if (config.getBoolean("restorerestart", false)) {
                                try {
                                    File[] var27;
                                    int var26 = (var27 = (new File(".")).listFiles()).length;

                                    for(i = 0; i < var26; ++i) {
                                        File file = var27[i];
                                        if (file.isFile() && file.getName().endsWith(".jar")) {
                                            Runtime.getRuntime().exec("cd " + args[1] + ";sleep 3;java -server -jar " + file.getName());
                                            this.getLogger().info("Restarting server with jarfile " + file.getName());
                                            break;
                                        }
                                    }
                                } catch (IOException var15) {
                                    this.getLogger().info("Error while re-starting server:");
                                    var15.printStackTrace();
                                    this.getLogger().info("Please restart the server by hand.");
                                }
                            }

                            Bukkit.shutdown();
                        } catch (Exception var16) {
                            sender.sendMessage(prefix + ChatColor.RED + "Error while restoring backup, please check the logs.");
                            var16.printStackTrace();
                        }
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', lang.getString("permission").replace("\\n", "\n")));
                }
            } else if (cmd.getLabel().equals("delbackup")) {
                if (sender.hasPermission("TheBackup.delbackup")) {
                    if (args.length == 0 || args[0].matches("[0-9]{0,9}") && Integer.parseInt(args[0]) > 0) {
                        if (args.length != 0 && Integer.parseInt(args[0]) > BackupUtils.listBackups().length) {
                            sender.sendMessage(prefix + ChatColor.RED + "Invalid backup, id out of range.");
                        } else if (!this.deleteconf.containsKey(sender)) {
                            id = args.length > 0 ? Integer.parseInt(args[0]) : BackupUtils.listBackups().length;
                            if (id > 0) {
                                sender.sendMessage(prefix + "Do you REALLY want to delete backup #" + id + "?");
                                sender.sendMessage(prefix + "If you want to continue type /delbackup within 10s.");
                                this.deleteconf.put(sender, id);
                                (new BukkitRunnable() {
                                    public void run() {
                                        if (TheBackup.this.deleteconf.remove(sender) != null) {
                                            sender.sendMessage(TheBackup.prefix + "The backup deletion has been cancelled.");
                                        }

                                    }
                                }).runTaskLater(plugin, 200L);
                            } else {
                                sender.sendMessage(prefix + ChatColor.RED + "No backup has been created yet.");
                            }
                        } else {
                            id = (Integer)this.deleteconf.remove(sender);
                            zip = BackupUtils.listBackups()[id - 1];
                            String size = BackupUtils.bytesToString(zip.length());
                            BackupUtils.deleteFile(zip);
                            sender.sendMessage(prefix + "Successfully deleted backup #" + id + " (" + size + ").");
                        }
                    } else {
                        sender.sendMessage(prefix + ChatColor.RED + "Invalid backup, id must be a positive integer.");
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', lang.getString("permission").replace("\\n", "\n")));
                }
            }
        }

        return false;
    }
}
