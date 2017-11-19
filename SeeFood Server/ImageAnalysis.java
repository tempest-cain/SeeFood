/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Class for analyzing pictures
 *
 * @author James Thacker
 */
public final class ImageAnalysis {

    //private final static String TOTAL_STATS_FILE_LOC = "/home/james/Desktop/Stats/";
    //private final static String DATABASE_LOC =       "/home/james/Desktop/Stats/";
    private final static String TOTAL_STATS_FILE_LOC = "/home/ec2-user/stats/";
    private final static String DATABASE_LOC = "/home/ec2-user/stats/";

    /**
     * Analyzes a picture and returns the results
     *
     * @param img Picture to be analyzed
     * @return Array containing the results
     * @throws IOException
     */
    public void analyze(BufferedImage img, ObjectInputStream in, ObjectOutputStream out, DataInputStream in2, DataOutputStream out2) throws IOException {

        // Create TotalStats.bin if it doesnt already exist
        checkTotalStatsFile(TOTAL_STATS_FILE_LOC);

        // Remove the alpha channel from the picture
        img = removeAlpha(img);

        // Create the folder to store the converted picture and its statistics file
        // Create the converted picture to be used for analysis
        long timestamp = createName();
        String directory = createPictFolder(DATABASE_LOC, timestamp);
        String filename = writeImage(img, directory, timestamp);

        // Call the AI to analyze the picture
        int[] result = callFindFood(filename, in2, out2);
        
        // Write each result, the first [0] is whether the picture contained food or not
        // The second [1] is the confidence rating from the AI
        out.writeInt(result[0]);
        out.flush();
        out.writeInt(result[1]);
        out.flush();

        // Update the overall statistics with the values from the AI
        updateTotalStatsFile(TOTAL_STATS_FILE_LOC, result[0]);

    }// End analyze()

    /**
     * Invokes the AI to analyze picture
     *
     * @param filename the location of the picture to be analyzed
     */
    public int[] callFindFood(String filename, DataInputStream in2, DataOutputStream out2) throws IOException {

        // Create a string of the filename of the image to be analyzed
        String pictureLoc = filename;
        byte[] byteArray = pictureLoc.getBytes("UTF-8");

        // Send filename to find_food.py
        out2.write(byteArray);
        out2.flush();

        int[] result = new int[2];
        result[0] = in2.readInt();
        result[1] = in2.readInt();

        // Receive confirmation of complete operation from find_food.py
        in2.readBoolean();
        
        return result;

    }// End callFindFood()

    /**
     * // Verify the TotalStats.bin file exists and isnt corrupt
     *
     * @param folder Folder containing the TotalStats.bin file
     */
    public static void checkTotalStatsFile(String folder) {

        // If the folder that is supposed to contain TotalStats.bin doesnt exist, create it
        if (!(new File(folder).exists())) {
            (new File(folder)).mkdirs();
        }

        // If TotalStats.bin doesnt exist, create a default TotalStats.bin
        // If TotalStats.bin does exist, verify it is not corrupted
        File file = new File(folder + "TotalStats.bin");
        if (!file.exists() || file.length() != 12) {
            try {

                // Create default file
                DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
                os.writeInt(0);
                os.flush();
                os.writeInt(0);
                os.flush();
                os.writeInt(0);
                os.flush();

                os.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }// End checkTotalStatsFile()

    /**
     * Creates the folder for the picture being analyzed and its results file
     *
     * @param databasePath Root folder of the database
     * @param timestamp Name of the folder
     * @return The path to the newly created folder
     */
    public String createPictFolder(String databasePath, long timestamp) {

        File folder = new File(databasePath + timestamp + "/");

        folder.mkdirs();

        return folder.getAbsolutePath() + "/";

    }// End createFolder

    /**
     * Create the name to be used for the analyzed picture, results file, and
     * their containing folder
     *
     * @return The name created
     */
    public long createName() {

        // Generate unique name
        Date dateObj = new Date();
        return dateObj.getTime();

    }// End createName()

    /**
     * This, along with its helper method, deletes all of the folders, pictures,
     * results/stats files and creates a new deafult TotalStats.bin file
     */
    public static void deleteDB() {

        // Specify location of the database
        File dir = new File(DATABASE_LOC);

        // Delete files and folders recursively
        if (dir.exists()) {

            for (File file : dir.listFiles()) {

                if (file.isDirectory()) {
                    deleteDB(file);
                    file.delete();
                } else {
                    file.delete();
                }

            }

        }

        // Create new TotalStats.bin file
        checkTotalStatsFile(TOTAL_STATS_FILE_LOC);

    }// End deleteDB()

    /**
     * Helper of deleteDB() above
     *
     * @param dir Folder to process/delete files from
     */
    public static void deleteDB(File dir) {

        if (dir.exists() && dir.isDirectory()) {

            for (File file : dir.listFiles()) {

                if (file.isDirectory()) {
                    deleteDB(file);
                } else {
                    file.delete();
                }

            }

        }

    }// End deleteDB()

    /**
     * Removes the alpha channel from a picture
     *
     * @param img Picture to remove alpha channel from
     * @return BufferedImage of picture without an alpha channel
     */
    public BufferedImage removeAlpha(BufferedImage img) {

        // Create BufferedImage to store newly created picture
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        // Copy picture without alpha channel
        Graphics2D data = copy.createGraphics();
        data.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
        data.dispose();

        return copy;

    }// End removeAlpha()

    /**
     * Update the file containing the total statistics, TotalStats.bin
     *
     * @param totalStatsLoc Location of TotalStats.bin
     * @param imageFolderLoc Location of the newly analyzed picture and results
     * file
     * @param imageID Name, excluding extension, of the results file
     */
    public void updateTotalStatsFile(String totalStatsLoc, int result) {

        // Create references to needed file
        File totalStats = new File(totalStatsLoc + "TotalStats.bin");

        // If file exist, update the TotalStats.bin file, else print an error message
        if (totalStats.exists()) {

            // Update TotalStats.bin
            try {
                DataInputStream is = new DataInputStream(new FileInputStream(totalStats));

                // Read current values from TotalStats.bin
                int total = is.readInt();
                int food = is.readInt();
                int Nfood = is.readInt();

                // Update totals
                total++;
                if (result == 1) {
                    food++;
                } else {
                    Nfood++;
                }

                is.close();

                DataOutputStream os = new DataOutputStream(new FileOutputStream(totalStats));

                // Write new values to TotalStats.bin
                os.writeInt(total);
                os.writeInt(food);
                os.writeInt(Nfood);
                os.flush();
                os.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                //System.out.println("TotalStats.bin is CORRUPTED");
            }

        } else {

            // Print error message
            //System.out.println("File or Folder does NOT exist, terminating...");
        }

    }// End updateTotalStatsFile()

    /**
     * Writes a picture to the database for analysis
     *
     * @param image Picture to write
     * @param folderPath Folder to write picture to
     * @param timestamp Name of newly created picture excluding extension as
     * this method converts/stores picture as jpg
     * @return Location of the newly created picture
     * @throws IOException
     */
    public String writeImage(BufferedImage image, String folderPath, long timestamp) throws IOException {

        // Create the filename and write picture to the disk
        String fileLocation = folderPath + timestamp + ".jpg";
        ImageIO.write(image, "jpg", new File(fileLocation));

        return fileLocation;

    }// End writeImage()

}
