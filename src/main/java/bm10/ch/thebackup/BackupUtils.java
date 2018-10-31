package bm10.ch.thebackup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class BackupUtils {
    public BackupUtils() {
    }

    public static void saveToFTP(File file) throws Exception {
        FTPClient ftp = new FTPClient();
        if (BackupRestorer.log) {
            System.out.println(BackupRestorer.prefix + "Connecting to FTP server...");
        }

        ftp.connect(TheBackup.config.getString("ftp.host"), TheBackup.config.getInt("ftp.port"));
        if (BackupRestorer.log) {
            System.out.println(BackupRestorer.prefix + "Authentificating...");
        }

        ftp.login(TheBackup.config.getString("ftp.user"), TheBackup.config.getString("ftp.pass"));
        ftp.enterLocalPassiveMode();
        ftp.setFileType(2);
        boolean done = false;
        if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            String path = TheBackup.config.getString("ftp.path");
            if (!path.endsWith("/")) {
                path = path + "/";
            }

            ftp.mkd(path);
            if (BackupRestorer.log) {
                System.out.println(BackupRestorer.prefix + "Uploading file to server...");
            }

            InputStream is = new FileInputStream(file);
            if (ftp.storeFile(path + file.getName(), is)) {
                done = true;
            }

            is.close();
        }

        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }

        if (!done) {
            throw new IOException(ftp.getReplyString());
        }
    }

    public static File createBackup() {
        File backup = null;

        try {
            String name = TheBackup.config.getString("backupformat").replace("{DATE}", dateToString(System.currentTimeMillis()));
            if (TheBackup.config.getBoolean("zipbackups", false)) {
                backup = Files.createTempDirectory("TheBackup").toFile();
            } else {
                backup = new File(TheBackup.backupfolder, name);

                for(int i = 1; backup.exists(); ++i) {
                    backup = new File(TheBackup.backupfolder, name + " (" + i + ")");
                }
            }

            setFilePerms(backup);
            backup.mkdirs();
            setFilePerms(backup);
            if (BackupRestorer.log) {
                System.out.println(BackupRestorer.prefix + "Copying files to be backed up...");
            }

            int i;
            int var4;
            File[] var5;
            File to;
            if (TheBackup.config.getBoolean("backup.jarfile", false)) {
                var4 = (var5 = (new File(".")).listFiles()).length;

                for(i = 0; i < var4; ++i) {
                    to = var5[i];
                    if (to.isFile() && to.getName().endsWith(".jar")) {
                        copyFile(to, backup, true);
                    }
                }
            }

            if (TheBackup.config.getBoolean("backup.properties", false)) {
                copyFile(new File("server.properties"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.ops", false)) {
                copyFile(new File("ops.json"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.whitelist", false)) {
                copyFile(new File("whitelist.json"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.spigotyml", false)) {
                copyFile(new File("spigot.yml"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.bukkityml", false)) {
                copyFile(new File("bukkit.yml"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.aliases", false)) {
                copyFile(new File("commands.yml"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.eula", false)) {
                copyFile(new File("eula.txt"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.logs", false)) {
                copyFile(new File("logs"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.metrics", false)) {
                copyFile(new File("plugins" + File.separator + "PluginMetrics"), backup, true);
            }

            if (TheBackup.config.getBoolean("backup.pluginjars", false)) {
                (new File(backup, "plugins")).mkdir();
                var4 = (var5 = (new File("plugins")).listFiles()).length;

                for(i = 0; i < var4; ++i) {
                    to = var5[i];
                    if (to.isFile() && to.getName().endsWith(".jar")) {
                        copyFile(to, new File(backup, "plugins"), true);
                    }
                }
            }

            if (TheBackup.config.getBoolean("backup.pluginconfs", false)) {
                (new File(backup, "plugins")).mkdir();
                var4 = (var5 = (new File("plugins")).listFiles()).length;

                for(i = 0; i < var4; ++i) {
                    to = var5[i];
                    if (to.isDirectory() && Bukkit.getPluginManager().getPlugin(to.getName()) != null) {
                        copyFile(to, new File(backup, "plugins"), true);
                    }
                }
            }

            Iterator var11 = TheBackup.config.getStringList("backup.worlds").iterator();

            String other;
            label157:
            while(var11.hasNext()) {
                other = (String)var11.next();
                if (other.equals("*")) {
                    Iterator var15 = Bukkit.getWorlds().iterator();

                    while(true) {
                        if (!var15.hasNext()) {
                            break label157;
                        }

                        World w = (World)var15.next();
                        copyFile(new File(w.getName()), backup, true);
                    }
                }

                if ((new File(other)).isDirectory() && Bukkit.getWorld(other) != null) {
                    copyFile(new File(other), backup, true);
                }
            }

            var11 = TheBackup.config.getStringList("backup.other").iterator();

            while(var11.hasNext()) {
                other = (String)var11.next();
                other = other.replace("/", File.separator);
                if (!other.startsWith("!")) {
                    if ((new File(other)).isDirectory()) {
                        copyFile(new File(other), backup, true);
                    } else if ((new File(other)).isFile()) {
                        copyFile(new File(other), backup, true);
                    }
                } else {
                    deleteFile(new File(backup, other.substring(1)));
                }
            }

            if (TheBackup.config.getBoolean("zipbackups", false)) {
                to = new File(TheBackup.backupfolder, name + ".zip");

                for(i = 1; to.exists(); ++i) {
                    to = new File(TheBackup.backupfolder, name + " (" + i + ").zip");
                }

                setFilePerms(to);
                zip((ZipOutputStream)null, backup, to.getAbsolutePath(), true);
                setFilePerms(to);
                deleteFile(backup);
                backup = to;
            }

            boolean both = TheBackup.config.getString("ftp.mode").equalsIgnoreCase("both");
            boolean first = TheBackup.config.getString("ftp.mode").equalsIgnoreCase("first");
            boolean always = TheBackup.config.getString("ftp.mode").equalsIgnoreCase("always");
            if (both || first || always) {
                try {
                    saveToFTP(backup);
                    if (first || always) {
                        deleteFile(backup);
                    }
                } catch (IOException var7) {
                    if (always) {
                        deleteFile(backup);
                        backup = null;
                    }

                    System.out.println(BackupRestorer.prefix + "Error while sending file to FTP server:");
                    var7.printStackTrace();
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            if (TheBackup.config.getString("ftp.mode").equalsIgnoreCase("fallback")) {
                try {
                    saveToFTP(backup);
                    deleteFile(backup);
                } catch (Exception var6) {
                    System.out.println(BackupRestorer.prefix + "Error while falling back to FTP server:");
                    var6.printStackTrace();
                    backup = null;
                }
            }
        }

        clean();
        return backup;
    }

    public static void setFilePerms(File file) {
        try {
            file.setExecutable(true);
            file.setReadable(true);
            file.setWritable(true);
        } catch (SecurityException var2) {
            var2.printStackTrace();
        }

    }

    public static void copyFile(File from, File to, boolean check) {
        to = new File(to, from.getName());
        if (!TheBackup.config.getStringList("backup.other").contains("!" + from.getPath().replace(File.separator, "/"))) {
            if (!TheBackup.config.getStringList("backup.other").contains("!" + from.getPath().replace(File.separator, "/") + "/")) {
                if (from.isDirectory()) {
                    if (!to.isDirectory()) {
                        to.mkdirs();
                    }

                    File[] var6;
                    int var5 = (var6 = from.listFiles()).length;

                    for(int var4 = 0; var4 < var5; ++var4) {
                        File file = var6[var4];
                        copyFile(file, to, check);
                    }
                } else if (from.isFile()) {
                    if (to.exists()) {
                        to.delete();
                    }

                    try {
                        Files.copy(from.toPath(), to.toPath());
                    } catch (IOException var7) {
                        var7.printStackTrace();
                    }
                }

            }
        }
    }

    public static void clean() {
        File[] files;
        File oldest;
        for(int max = TheBackup.config.getInt("maxbackups"); max > 0 && (files = listBackups()).length > max; deleteFile(oldest)) {
            if (BackupRestorer.log) {
                System.out.println(BackupRestorer.prefix + "Deleting oldest backup...");
            }

            oldest = files[0];
            File[] var6 = files;
            int var5 = files.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                File file = var6[var4];
                if (file.lastModified() < oldest.lastModified()) {
                    oldest = file;
                }
            }

            try {
                if (TheBackup.config.getString("ftp.mode").equalsIgnoreCase("limit")) {
                    saveToFTP(oldest);
                }
            } catch (Exception var7) {
                System.out.println(BackupRestorer.prefix + "Error while moving file to FTP server:");
                var7.printStackTrace();
            }
        }

    }

    public static File[] listBackups() {
        if (!TheBackup.backupfolder.isDirectory()) {
            return new File[0];
        } else {
            ArrayList<File> backupList = new ArrayList();
            File[] var4;
            int var3 = (var4 = TheBackup.backupfolder.listFiles()).length;

            for(int var2 = 0; var2 < var3; ++var2) {
                File backup = var4[var2];
                if (backup.isDirectory() && !backup.isHidden()) {
                    backupList.add(backup);
                } else if (backup.isFile() && backup.getName().endsWith(".zip") && !backup.isHidden()) {
                    backupList.add(backup);
                }
            }

            File[] backups = (File[])backupList.toArray(new File[backupList.size()]);
            Arrays.sort(backups, new Comparator<File>() {
                public int compare(File a, File b) {
                    if (b.lastModified() - a.lastModified() < 2147483647L) {
                        return b.lastModified() - a.lastModified() > -2147483648L ? (int)(b.lastModified() - a.lastModified()) : -2147483648;
                    } else {
                        return 2147483647;
                    }
                }
            });
            return backups;
        }
    }

    public static void deleteFile(File file) {
        if (file.isDirectory() && file.listFiles().length > 0) {
            File[] var4;
            int var3 = (var4 = file.listFiles()).length;

            for(int var2 = 0; var2 < var3; ++var2) {
                File subfile = var4[var2];
                deleteFile(subfile);
            }

            deleteFile(file);
        } else if (file.exists()) {
            file.delete();
        }

    }

    public static String dateToString(long unix) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(unix);
        String string = TheBackup.config.getString("dateformat");
        if (string == null || string.isEmpty()) {
            string = "MM/DD/YYYY hh:mm:ss";
        }

        string = string.replace("MM", (c.get(2) < 9 ? "0" : "") + (c.get(2) + 1));
        string = string.replace("DD", (c.get(5) < 10 ? "0" : "") + c.get(5));
        string = string.replace("YYYY", "" + c.get(1));
        string = string.replace("hh", (c.get(11) < 10 ? "0" : "") + c.get(11));
        string = string.replace("HH", (c.get(10) < 10 ? "0" : "") + c.get(10));
        string = string.replace("mm", (c.get(12) < 10 ? "0" : "") + c.get(12));
        string = string.replace("ss", (c.get(13) < 10 ? "0" : "") + c.get(13));
        string = string.replace("ms", "" + c.get(14));
        string = string.replace("AM", c.get(9) == 0 ? "AM" : "PM");
        return string;
    }

    public static void unzip(File zip, File folder) {
        byte[] buffer = new byte[1024];

        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
            int i = 0;

            while(true) {
                String fileName;
                do {
                    ZipEntry ze;
                    if ((ze = zis.getNextEntry()) == null) {
                        if (BackupRestorer.log) {
                            System.out.println(BackupRestorer.prefix + "Finished decompressing " + i + " file(s).");
                        }

                        zis.closeEntry();
                        zis.close();
                        return;
                    }

                    fileName = ze.getName();
                    if (ze.getName().contains("/")) {
                        fileName = ze.getName().substring(ze.getName().lastIndexOf("/"));
                    }
                } while(fileName.equals("/"));

                if (BackupRestorer.log) {
                    System.out.println(BackupRestorer.prefix + "Decompressing file " + fileName + "...");
                }

                File newFile = new File(folder, fileName);
                if (newFile.exists()) {
                    deleteFile(newFile);
                }

                (new File(newFile.getParent())).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ++i;
            }
        } catch (IOException var10) {
            System.out.println(BackupRestorer.prefix + "Error while decompressing file:");
            var10.printStackTrace();
        }
    }

    public static ZipOutputStream zip(ZipOutputStream zip, File tozip, String dir, boolean close) {
        byte[] buffer = new byte[1024];

        try {
            if (zip == null) {
                if (!(new File(dir)).isFile()) {
                    (new File(dir)).getParentFile().mkdirs();
                    (new File(dir)).createNewFile();
                }

                zip = new ZipOutputStream(new FileOutputStream(new File(dir)));
                dir = null;
            }

            int len;
            if (tozip.isFile()) {
                ZipEntry ze = new ZipEntry((dir != null ? dir : "") + tozip.getName());
                zip.putNextEntry(ze);
                if (BackupRestorer.log) {
                    System.out.println(BackupRestorer.prefix + "Compressing file " + ze.getName() + "...");
                }

                FileInputStream in = new FileInputStream(tozip);

                while((len = in.read(buffer)) > 0) {
                    zip.write(buffer, 0, len);
                }

                in.close();
            } else if (tozip.isDirectory()) {
                dir = dir != null ? dir + tozip.getName() + File.separator : "";
                File[] var8;
                len = (var8 = tozip.listFiles()).length;

                for(int var12 = 0; var12 < len; ++var12) {
                    File subfile = var8[var12];
                    if (subfile.exists()) {
                        zip(zip, subfile, dir, false);
                    }
                }
            }
        } catch (IOException var10) {
            System.out.println(BackupRestorer.prefix + "Error while compressing file:");
            var10.printStackTrace();
        }

        if (close) {
            try {
                zip.closeEntry();
                zip.close();
            } catch (IOException var9) {
                System.out.println(BackupRestorer.prefix + "Error while closing compressed file:");
                var9.printStackTrace();
            }

            if (BackupRestorer.log) {
                System.out.println(BackupRestorer.prefix + "Finished decompressing file(s).");
            }
        }

        return zip;
    }

    public static String bytesToString(long bytes) {
        int u = 1024;
        if ((double)bytes / Math.pow((double)u, 3.0D) > 0.0D) {
            return (double)bytes / Math.pow((double)u, 3.0D) + " GB";
        } else if ((double)bytes / Math.pow((double)u, 2.0D) > 0.0D) {
            return (double)bytes / Math.pow((double)u, 2.0D) + " MB";
        } else {
            return (double)bytes / Math.pow((double)u, 1.0D) > 0.0D ? (double)bytes / Math.pow((double)u, 1.0D) + " KB" : bytes + " B";
        }
    }
}
