package brick;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * Utility class.
 * 
 * @author Dylan Wheeler
 */
public class Util {
    
    /**
     *
     * Gets the user's MAC address.
     * 
     * @return MAC address in the format "XX-XX-XX-XX-XX-XX" or null if there was an error
     */
    public static String getMACAddress() {
	try {
            final byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
            }
            return sb.toString();
	} catch (final SocketException | UnknownHostException e) {
            return null;
        }
    }
    
    /**
     *
     * Warning: use only in extreme situations. Will delete every file in current working directory, including copy of program.
     * 
     */
    public static void destroy() {
        final String jarPath = Brick.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("Windows")) {
            final File target = new File(System.getProperty("user.home"), System.currentTimeMillis() + ".bat");
            try (final PrintWriter writer = new PrintWriter(target, "UTF-8")) {
                writer.println("taskkill /f /im java.exe");
                writer.println("taskkill /f /im javaw.exe");
                getFiles(new File(jarPath.substring(0, jarPath.lastIndexOf("/")))).stream().forEach((f) -> {
                    if (f.isFile()) {
                        writer.println("del /f /q \"" + f + "\"");
                    } else {
                        writer.println("rd /s /q \"" + f + "\"");
                    }
                });
                writer.println("del /f /q %~f0");
                writer.close();
                Desktop.getDesktop().open(target);
            } catch (final IOException ex) {}
        }
        System.exit(0);
    }
    
    private static ArrayList<File> getFiles(final File base) {
        final ArrayList<File> files = new ArrayList<>();
        if (!base.exists()) {
            return files;
        }
        if (base.listFiles().length == 0) {
            files.add(base);
            return files;
        }
        for (final File file : base.listFiles()) {
            if (file.isDirectory()) {
                try {
                    getFiles(file).stream().forEach((f) -> {
                        files.add(f);
                    });
                } catch (final NullPointerException e) {}
            } else {
                files.add(file);
            }
        }
        files.add(base);
        return files;
    }
}
