package brick;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 *
 * Codes data.
 * 
 * @author Dylan Wheeler
 */
public class Coder {
    
    /**
     *
     * Creates an coded Brick from input data while also saving key to disk.
     * 
     * @param data the data to encrypt
     * @param keyOut file location to save the key
     * @return the Brick as a BufferedImage
     * @see java.awt.image.BufferedImage
     */
    public static BufferedImage write(final String data, final File keyOut) {
        final String[] LETTERS = {"a", "b", "c", "d", "e", "f"};
        final byte[] out = data.getBytes();
        final char[] key = new char[out.length];
        for (int i = 0; i < out.length; i++) {
            key[i] = (char) (33 + (int) (Math.random() * 94));
            out[i] = (byte) (out[i] ^ key[i]);
        }
        String ascii = "";
        for (final byte c : out) {
            ascii += (byte) c + LETTERS[(int) (Math.random() * 6)];
        }
        ascii = ascii.substring(0, ascii.length() - 1);
        final int iterations = 6 - ascii.length() % 6;
        for (int i = 0; i < iterations; i++) {
            ascii = "0" + ascii;
        }
        final int colorLen = ascii.length() / 6;
        Point middle = new Point(colorLen, 1);
        for (int i = 2; i <= (int) Math.ceil(Math.sqrt(colorLen)); i++) {
            if (colorLen % i == 0) {
                middle = new Point(colorLen / i, i);
            }
        }
        final BufferedImage img = new BufferedImage(middle.x, middle.y, BufferedImage.TYPE_INT_RGB);
        final String[] colors = ascii.split("(?<=\\G.{6})");
        int index = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                img.setRGB(x, y, new Color(Integer.valueOf(colors[index].substring(0, 2), 16), Integer.valueOf(colors[index].substring(2, 4), 16), Integer.valueOf(colors[index].substring(4, 6), 16)).getRGB());
                index++;
            }
        }
        try (final FileWriter fw = new FileWriter(keyOut)) {
            for (final char k : key) {
                fw.append(k);
            }
            fw.close();
        } catch (final IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to encrypt data! Please check for invalid characters in the input and try again.", "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return img;
    }
    
    /**
     *
     * Reads a Brick using key information.
     * 
     * @param img the Brick to read
     * @param keyIn the key to use
     * @return the encrypted data as a String
     */
    public static String read(final BufferedImage img, final File keyIn) {
        try {
            char[] key;
            try (final Scanner input = new Scanner(keyIn)) {
                String temp = "";
                while (input.hasNext()) {
                    temp += input.next();
                }
                input.close();
                key = temp.toCharArray();
            }
            String asciiCode = "";
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    final Color c = new Color(img.getRGB(x, y));
                    asciiCode += String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                }
            }
            final String[] asciiSplit = asciiCode.split("[a-z]");
            String decrypted = "";
            for (int i = 0; i < asciiSplit.length; i++) {
                decrypted += (char) ((Integer.parseInt(asciiSplit[i])) ^ key[i]);
            }
            return decrypted;
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to decrypt data! Please make sure you are the intended party.", "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

}
