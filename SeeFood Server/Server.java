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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import result.Result;

/**
 * Main SeeFood Server class
 *
 * @author James Thacker
 */
public class Server {

    private final String VERSION = "See-0.1.4_Beta";
    private ServerSocket server = null;
    private ServerSocket server2 = null;
    private Socket socket = null;
    private Socket socket2 = null;
    private static final String FIND_FOOD = "/home/ec2-user/seefood-core-ai/find_food.py";
    //private static final String FIND_FOOD = "/home/james/Desktop/find_food.py";
    private static final String IMAGE_FOLDER = "/home/ec2-user/stats/";
    //private static final String IMAGE_FOLDER = "/home/james/Desktop/Stats/";
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

    /**
     * Receives a picture from the Client and begins the analysis process
     *
     * @param in Input stream
     * @param out Output stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void analyze(ObjectInputStream in, ObjectOutputStream out, DataInputStream in2, DataOutputStream out2) throws IOException, ClassNotFoundException {

        // Byte array to store picture sent from Client
        byte[] byteArray = (byte[]) in.readObject();

        // Create instance of class that analyzes picture and call its analyze method and store results
        ImageAnalysis analyze = new ImageAnalysis();
        analyze.analyze(byteArray, out, in2, out2);

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
     * @throws IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void getPictArray() throws IOException, ClassNotFoundException {

        // Arraylist to hold image objects containing the image and its stats
        ArrayList<Result> al = new ArrayList();

        // Get image number/name to start with, HIGHER number is more recent image
        int sendImageNo = in.readInt();

        // Read 20 images/stats into the Arraylist
        for (int i = 0; i < 20 && sendImageNo > 0; ++i, --sendImageNo) {

            File fi = new File(IMAGE_FOLDER + sendImageNo + "/" + sendImageNo + ".image");

            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(fi));

            al.add((Result) oin.readObject());

        }

        // Send arraylist containing part of the Gallery to the client
        out.writeObject(al);
        out.flush();

        // Send the size of the arraylist to the client
        out.writeInt(al.size());
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
     * Sends the total files in DB to client
     * @param out Output channel to client
     * @throws IOException 
     */
    public void getTotal(ObjectOutputStream out) throws IOException {

        // Send the total value from TotalStats.bin
        out.writeInt(GetStats.getTotalStats()[0]);
        out.flush();

    }

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
    private void listen(DataInputStream in2, DataOutputStream out2) throws IOException, ClassNotFoundException {

        // Variable used to control main loop
        boolean run = true;

        // Main program loop
        while (run) {

            // Listen for and accept Client connection
            socket = server.accept();

            // Begin interaction with Client
            try {
                // Create input and output stream for this connection
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                run = options(in2, out2);
            } catch (Exception ex) {
                //Dont crash server if client crashes
                continue;
            }

            out.close();
            out = null;
            in.close();
            in = null;
            socket.close();
            socket = null;

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
    public boolean options(DataInputStream in2, DataOutputStream out2) throws IOException, ClassNotFoundException {

        // Create control variables
        boolean keepServerAlive = true;

        // Accept input from Client
        int x = in.readInt();

        // Execute chosen function
        switch (x) {

            // Analyze picture sent from Client
            case 1:
                analyze(in, out, in2, out2);
                break;

            // Send gallery to Client
            case 2:
                getPictArray();
                break;

            // Send total statistics to Client
            case 3:
                getStats(out);
                break;

            // Client left, start waiting for new connection
            case 4:
                break;

            // Terminate the connection to the Client and the SeeFood Server
            case 5:
                keepServerAlive = false;
                break;

            // Send SeeFood Server version number to the Client
            case 6:
                getVersion(out);
                break;

            // Delete the SeeFood Server database
            case 7:
                deleteDB(out);
                break;
                
            case 8:
                getTotal(out);
                break;

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
        DataOutputStream out2 = new DataOutputStream(socket2.getOutputStream());
        DataInputStream in2 = new DataInputStream(socket2.getInputStream());

        // Receive acknowledgement that Python Script is running
        boolean x = in2.readBoolean();

        //System.out.println("Ready");
        // Listen for connections
        listen(in2, out2);

        // Close streams and socket relating to the Python Script
        out2.close();
        out2 = null;
        in2.close();
        in2 = null;
        socket2.close();
        socket2 = null;

    }// End Server

}
