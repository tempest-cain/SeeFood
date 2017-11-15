/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

/**
 * Main SeeFood Server class
 *
 * @author James Thacker
 */
public class Server {

    private final String VERSION = "See-0.9.8_Beta";
    private ServerSocket server = null;
    private ServerSocket server2 = null;
    private Socket socket = null;
    private Socket socket2 = null;
    private static final String FIND_FOOD = "/home/ec2-user/seefood-core-ai/find_food.py";
    //private static final String FIND_FOOD = "/home/james/Desktop/find_food.py";
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private DataOutputStream out2 = null;
    private DataInputStream in2 = null;
    

    /**
     * Receives a picture from the Client and begins the analysis process
     *
     * @param in Input stream
     * @param out Output stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void analyze(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {

        // Byte array to store picture sent from Client
        byte[] byteArray = (byte[]) in.readObject();

        // Create input stream and convert the bytes into a picture
        ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
        BufferedImage img = ImageIO.read(byteStream);

        // Create instance of class that analyzes picture and call its analyze method and store results
        ImageAnalysis analyze = new ImageAnalysis();
        int[] results = analyze.analyze(img, in2, out2);

        // Write each result, the first [0] is whether the picture contained food or not
        // The second [1] is the confidence rating from the AI
        out.writeInt(results[0]);
        out.flush();
        out.writeInt(results[1]);
        out.flush();
    }// End analyze()

    /**
     * Deletes the SeeFood Server database
     *
     * @param out Output stream
     * @throws IOException
     */
    public void deleteDB(ObjectOutputStream out) throws IOException {

        // Call delete method and send message to the Client
        ImageAnalysis.deleteDB();
        out.writeUTF("Database Deleted");
        out.flush();

    }// End deleteDB()

    /**
     * Sends the gallery to the Client
     *
     * @param out Output stream
     * @throws IOException
     */
    public void getGallery(ObjectOutputStream out) throws IOException {

        // UNFINISHED
        out.writeUTF("This is the unfinished gallery function");
        out.flush();

    }// End gallery()

    /**
     * Sends the total statistics of previously analyzed pictures to the Client
     *
     * @param out Output stream
     * @throws IOException
     */
    public void getStats(ObjectOutputStream out) throws IOException {

        // Get statistic from file
        int[] stats = GetStats.getTotalStats();

        // Send values to the Client
        // If TotalStats.bin is corrupt, alert Client
        if (stats[0] == (-1)) {
            out.writeInt(-1);
            out.flush();
        } else {
            out.writeInt(stats[0]); // Total pictures in DB
            out.flush();
            out.writeInt(stats[1]); // Total pictures containing food
            out.flush();
            out.writeInt(stats[2]); // Total pictures NOT containing food
            out.flush();
        }

    }// End getStats()

    /**
     * Sends the SeeFood Server version number to the Client
     *
     * @param out Output stream
     * @throws IOException
     */
    public void getVersion(ObjectOutputStream out) throws IOException {

        // Send SeeFood Server version number to the client
        out.writeUTF(VERSION);
        out.flush();

    }// End getVersion()

    /**
     * Listens for connections
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void listen() throws IOException, ClassNotFoundException {

        // Variable used to control main loop
        boolean run = true;

        // Main program loop
        while (run) {

            // Listen for and accept Client connection
            socket = server.accept();

            // Create input and output stream for this connection
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Begin interaction with Client
            try {
                run = options(out, in);
            } catch (EOFException ex) {
                ;//Dont crash server if client crashes
            }

        }

    }// End listen()

    /**
     * Accept and process chosen function/option
     *
     * @param out Output stream
     * @param in Input stream
     * @return value that is used to decide whether or not to terminate the
     * SeeFood Server
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean options(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {

        // Create control variables
        boolean keepServerAlive = true, loop = true;

        // Begin Server-Client Communication loop
        while (loop) {

            // Accept input from Client
            int x = -1;
            
            try{
                x = in.readInt();
            }catch(Exception ex){
                x = 4;
            }
            // Execute chosen function
            switch (x) {

                // Analyze picture sent from Client
                case 1:
                    analyze(in, out);
                    System.out.println("Picture Analyzed");
                    break;

                // Send gallery to Client
                case 2:
                    getGallery(out);
                    System.out.println("Gallery Sent");
                    break;

                // Send total statistics to Client
                case 3:
                    getStats(out);
                    System.out.println("Statistics Sent");
                    break;

                // Acknowledge Client terminating connection and wait for new connection
                case 4:
                    loop = false;
                    System.out.println("Goodbye");
                    break;

                // Terminate the connection to the Client and the SeeFood Server
                case 5:
                    keepServerAlive = false;
                    loop = false;
                    System.out.println("Goodbye");
                    break;

                // Send SeeFood Server version number to the Client
                case 6:
                    getVersion(out);
                    System.out.println("Version Info Sent");
                    break;

                // Delete the SeeFood Server database
                case 7:
                    deleteDB(out);
                    System.out.println("Database Erased");
                    break;

            }

        }

        return keepServerAlive;

    }// End options()

    /**
     * SeeFood Server constructor
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Server() throws IOException, ClassNotFoundException {

        // Create a ServerSocket and begin listening for connections
        server = new ServerSocket(3000);
        server2 = new ServerSocket(4000);

        Runtime runtime = Runtime.getRuntime();
        try {
            java.lang.Process temp = runtime.exec("python2.7 " + FIND_FOOD);
        } catch (IOException e) {
            e.printStackTrace();
        }

        socket2 = server2.accept();
        out2 = new DataOutputStream(socket2.getOutputStream());
        in2 = new DataInputStream(socket2.getInputStream());
        
        // Receive acknowledgement that Python Script is running
        boolean x = in2.readBoolean();

        System.out.println("Ready");

        // Listen for connections
        listen();
        
        // Close streams and socket relating to the Python Script
        out2.close();
        out2 = null;
        in2.close();
        in2 = null;
        socket2.close();
        socket2 = null;

        // Close input/output streams and socket relating to the Client
        out.close();
        out = null;
        in.close();
        in = null;
        socket.close();
        socket = null;
        
    }// End Server

}
