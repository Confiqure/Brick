package brick;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 *
 * Main class.
 * 
 * @author Dylan Wheeler
 */
public class Brick {
    
    private static final Scanner IN = new Scanner(System.in);
    
    private static String MAC;
    
    public static void main(final String[] args) {
        if ((MAC = Util.getMACAddress()) == null) {
            System.err.println("Initialization error");
            System.exit(0);
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "r":
                case "read":
                    read(args);
                    return;
                case "w":
                case "write":
                    write(args);
                    return;
            }
        }
        System.out.println("What would you like to do? (READ/WRITE)");
        switch (IN.nextLine().toLowerCase().trim()) {
            case "r":
            case "read":
                read(args);
                break;
            case "w":
            case "write":
                write(args);
                break;
            default:
                System.err.println("Command not recognized: please use input READ (shortcut R) or WRITE (shortcut W)");
                System.exit(0);
        }
    }
    
    private static void read(final String[] args) {
        final File fIn = new File(getInput(args, 1, "Target brick with extension (example message.png)"));
        if (!fIn.exists()) {
            System.err.println("File not found on disk");
            System.exit(0);
        }
        final File keyIn = new File(getInput(args, 2, "Key file path (will read from for decryption)"));
        if (!keyIn.exists()) {
            System.err.println("Key not found on disk");
            System.exit(0);
        }
        final long start = System.nanoTime() / 1000000;
        try {
            String out = Coder.read(ImageIO.read(fIn), keyIn);
            if (out.startsWith("args[")) {
                final String[] params = out.substring(5, out.indexOf("]")).split(",");
                for (final String parameter : params) {
                    final String[] parts = parameter.split("=");
                    if (parts[0].equals("to") && !parts[1].equals(MAC)) {
                        System.err.println("You are not authorized to view this brick");
                    } else if (parts[0].equals("expire") && System.currentTimeMillis() > Long.parseLong(parts[1])) {
                        System.err.println("This brick has expired");
                    } else {
                        continue;
                    }
                    if (!fIn.delete()) {
                        Util.destroy();
                    }
                    System.out.println("The brick has been destroyed");
                    System.exit(0);
                }
                out = out.substring(out.indexOf("]") + 1);
            }
            System.out.println(out + "\n\nDecrypted in " + (System.nanoTime() / 1000000 - start) + "ms");
        } catch (final IOException ex) {
            System.err.println("Error while decrypting: " + ex.getMessage());
        }
    }
    
    private static void write(final String[] args) {
        final File fIn = new File(getInput(args, 1, "Source file name with extension (example src.txt)"));
        if (!fIn.exists()) {
            System.err.println("File not found on disk");
            System.exit(0);
        }
        final File keyOut = new File(getInput(args, 2, "Key file path (will write to after encryption)"));
        final Object[] eParams = new Object[3];
        eParams[0] = new File(getInput(args, 3, "Target file name (ex. message)") + ".png");
        eParams[1] = getInput(args, 4, "Specify the MAC address of receipient (press enter for no receipient)");
        String date = getInput(args, 5, "Specify the expiration date in format MM/dd/yyyy HH:mm (press enter for no expiration)");
        if (!date.isEmpty()) {
            try {
                eParams[2] = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(date).getTime();
            } catch (final ParseException ex) {
                System.err.println("Invalid expiration date, packaging without one");
                eParams[2] = 0L;
            }
        } else {
            eParams[2] = 0L;
        }
        final long start = System.nanoTime() / 1000000;
        try {
            String out = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(fIn.getName())))).toString();
            if (!((String) eParams[1]).isEmpty() || (Long) eParams[2] != 0) {
                final StringBuilder sb = new StringBuilder();
                sb.append("args[");
                if (!((String) eParams[1]).isEmpty()) {
                    sb.append("to=").append((String) eParams[1]);
                }
                if ((Long) eParams[2] != 0) {
                    if (sb.toString().length() > 5) {
                        sb.append(",");
                    }
                    sb.append("expire=").append((Long) eParams[2]);
                }
                sb.append("]");
                out = sb.toString() + out;
            }
            ImageIO.write(Coder.write(out, keyOut), "png", ((File) eParams[0]));
            System.out.println("Encrypted in " + (System.nanoTime() / 1000000 - start) + "ms");
        } catch (final IOException ex) {
            System.err.println("Error while encrypting: " + ex.getMessage());
        }
    }
    
    private static String getInput(final String[] args, final int index, final String prompt) {
        if (args.length > index) return args[index];
        System.out.print(prompt + ": ");
        return IN.nextLine();
    }
    
}
