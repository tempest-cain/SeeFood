/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import result.Result;

/**
 * Class for analyzing pictures
 *
 * @author James Thacker
 */
public final class ImageAnalysis {

    //private final static String TOTAL_STATS_FILE_LOC = "/home/james/Desktop/stats/";
    //private final static String DATABASE_LOC = "/home/james/Desktop/stats/";
    private final static String TOTAL_STATS_FILE_LOC = "/home/ec2-user/stats/";
    private final static String DATABASE_LOC = "/home/ec2-user/stats/";

    /**
     * Analyzes a picture and returns the results
     *
     * @param byteArray, picture to analyze in a byte array
     * @param in2 Input stream to the AI
     * @param out Output stream to the client
     * @param out2 Output stream to the AI
     * @throws IOException
     */
    public int[] analyze(byte[] byteArray, ObjectOutputStream out, DataInputStream in2, DataOutputStream out2) throws IOException, ClassNotFoundException {
        
        // Create TotalStats.bin if it doesnt already exist
        checkTotalStatsFile(TOTAL_STATS_FILE_LOC);

        // Create a name and folder for picture
        int name = createName();
        String directory = createPictFolder(DATABASE_LOC, name);

        // Write picture to disk
        String filename = writeImage(byteArray, directory, name);        
        
        // Call the AI to analyze the picture
        int[] result = callFindFood(filename, in2, out2);
        
        // Update the overall statistics with the values from the AI
        updateTotalStatsFile(TOTAL_STATS_FILE_LOC, result[0]);
        
        
        File jpg = new File(filename + ".jpg");
        
        FileInputStream fis = new FileInputStream(jpg);
        
        // Writes Result object to folder for faster access when returning the gallery
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filename+".image")));
        
        byte[] imageBytes = new byte[(int)jpg.length()];
        fis.read(imageBytes);
        fis.close();
        
        // Create Result object
        Result imageObject = new Result(result[0], result[1], imageBytes);

        oos.writeObject(imageObject);
        oos.flush();
        oos.close();
        
        // Return int[] containing results of analysis
        return result;
        
    }// End analyze()

    /**
     * Invokes the AI to analyze picture
     *
     * @param filename the location of the picture to be analyzed
     * @param in2 Input stream to the AI
     * @param out2 Output stream to the AI
     * @return Array containing the is food value and confidence value
     * @throws java.io.IOException
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
     * @param name Name of the folder
     * @return The path to the newly created folder
     */
    public String createPictFolder(String databasePath, long name) {

        File folder = new File(databasePath + name + "/");

        folder.mkdirs();

        return folder.getAbsolutePath() + "/";

    }// End createFolder

    /**
     * Create the name to be used for the analyzed picture, results file, and
     * their containing folder
     *
     * @return The name created
     * @throws java.io.IOException
     */
    public int createName() throws IOException {

        // Generate unique name
        int name = GetStats.getTotalStats()[0];
        return ++name;

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
     * Update the file containing the total statistics, TotalStats.bin
     *
     * @param totalStatsLoc Location of TotalStats.bin
     * @param result Whether picture contained food or not
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
                    ;
            }

        }

    }// End updateTotalStatsFile()

    /**
     * Writes a picture to the database for analysis
     *
     * @param image Picture to write
     * @param folderPath Folder to write picture to
     * @param name Name of newly created picture excluding extension as this
     * method converts/stores picture as jpg
     * @return Location of the newly created picture
     * @throws IOException
     */
    public String writeImage(byte[] byteArray, String folderPath, long name) throws IOException {

        // Create the filename and write picture to the disk
        String fileLocation = folderPath + name;
                
        FileOutputStream fo = new FileOutputStream(new File(fileLocation));
        fo.write(byteArray);
        fo.flush();
        fo.close();

        return fileLocation;

    }// End writeImage()


}
