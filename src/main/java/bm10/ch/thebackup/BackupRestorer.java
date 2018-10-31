package bm10.ch.thebackup;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class BackupRestorer {
    public static boolean log = false;
    public static String prefix = "[TheBackup] ";

    public BackupRestorer() {
    }

    public static void main(String[] args) {
        log = args.length < 3 || !args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("yes") && !args[2].equalsIgnoreCase("1");
        if (args.length >= 2) {
            if ((new File(args[0])).exists() && (new File(args[1])).exists()) {
                if (log) {
                    System.out.println(prefix + "Starting backup restore...");
                }

                File from = new File(args[0]);
                File to = new File(args[1]);
                if (from.isFile() && from.getName().endsWith(".zip")) {
                    if (log) {
                        System.out.println(prefix + "Decompressing backup to target...");
                    }

                    BackupUtils.unzip(from, to);
                } else if (from.isDirectory()) {
                    if (log) {
                        System.out.println(prefix + "Copying backup to target...");
                    }

                    try {
                        Files.copy(from.toPath(), to.toPath());
                    } catch (IOException var4) {
                        System.out.println(prefix + "Error while copying file:");
                        var4.printStackTrace();
                    }
                } else if (from.exists()) {
                    System.out.println(prefix + "The specified backup hasn't a valid format.");
                    System.exit(1);
                } else {
                    System.out.println(prefix + "The specified backup couldn't be found.");
                    System.exit(1);
                }

                if (log) {
                    System.out.println(prefix + "Finished restoring backup!");
                }

                System.exit(0);
            } else if (!(new File(args[0])).exists()) {
                System.out.println(prefix + "Error, the specified backup directory does not exist.");
                System.exit(1);
            } else if ((new File(args[1])).exists()) {
                System.out.println(prefix + "Error, the specified server directory does not exist.");
                System.exit(1);
            }
        } else {
            System.out.println(prefix + "Usage: TheBackup.jar <backup_path> <server_path> [log]");
            System.exit(1);
        }

        System.out.println(prefix + "An unknown error occured while restoring backup!");
    }
}
