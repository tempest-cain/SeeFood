/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author James Thacker
 */
public final class GetStats {

    private final static String TOTAL_STATS_FILE = "/home/ec2-user/stats/TotalStats.bin";
    //private final static String TOTAL_STATS_FILE = "/home/james/Desktop/stats/TotalStats.bin";

    /**
     * Constructor
     */
    private GetStats() {
    }

    /**
     * Reads the statistics from the TotalStats.bin file and returns an array
     * containing its values
     *
     * @return array containing the statistics values
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static int[] getTotalStats() throws FileNotFoundException, IOException {

        // Create File object
        File file = new File(TOTAL_STATS_FILE);

        // Test if TotalStats.bin exists and write its data to an array and return that array
        if (!file.exists()) {
            return new int[]{-1};
        } else {
            try (DataInputStream is = new DataInputStream(new FileInputStream(file))) {
                return new int[]{is.readInt(), is.readInt(), is.readInt()};
            }
        }

    }// End getTotalStats()

}
