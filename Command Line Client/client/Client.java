/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import result.Result;

/**
 *
 * @author James Thacker
 */
public class Client {

    private final static String VERSION = "3.6";
    private static Socket socket = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;
    private final static String SERVER_ADDR = "34.227.5.168";
    //private final static String SERVER_ADDR = "127.0.0.1";
    private static String PIC_STORE_LOC; // = "/home/james/Desktop/ServerPICs/";
    private static int total = -1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Controls program flow via a while loop
        boolean loopVal = true;

        // Get user input for number of images to analyze
        Scanner i = new Scanner(System.in);
        System.out.print("Enter folder path to download gallery to(NOTE be sure to end path with a /: ");
        PIC_STORE_LOC = i.nextLine();
        // Print Client information
        System.out.println("Welcome to SeeFood Client version " + VERSION + "\n\n");

        // Main program loop
        while (loopVal) {

            // Prints the client menu
            System.out.print("|-------- Menu --------|\n\n"
                    + "1) Analyze image         \n"
                    + "2) Retrieve Gallery      \n"
                    + "3) Retrieve Statistics   \n"
                    + "4) Exit Client           \n"
                    + "5) Kill Server           \n"
                    + "6) Versions              \n"
                    + "7) Delete Database       \n"
                    + "8) MultiImage Analyze    \n\n"
                    + "Make selection and press enter\n\n"
                    + ">");

            // Create Scanner and accept user input
            Scanner input = new Scanner(System.in);
            int choice = 0;
            try {
                choice = input.nextInt();
            } catch (InputMismatchException ex) {
                System.out.println("\n\n\nINVALID CHOICE!!!\n\n\n");
                continue;
            }

            // Process user choice
            switch (choice) {
                case 1:
                    analyze(1);      // Implemented
                    break;
                case 2:
                    gallery();      // Implemented
                    break;
                case 3:
                    getStats();     // Implemented
                    break;
                case 4:
                    //exit();         // Implemented
                    loopVal = false;
                    break;
                case 5:
                    loopVal = killServer();   //Implemented
                    break;
                case 6:
                    getVersions();  // Implemented
                    break;
                case 7:
                    deleteDB();     // Implemented
                    break;
                case 8:
                    System.out.print("How many images to analyze: ");
                    analyze(input.nextInt());
                    break;

                // Used if choice entered is invalid
                default:
                    System.out.println("\n\nInvalid choice!\n\n");
                    continue;

            }

        }

    }// End main()

    /**
     * analyze() is the method called to select and send the picture to be
     * analyzed by the SeeFood Server
     *
     * @throws IOException
     */
    private static void analyze(int x) throws IOException, ClassNotFoundException {

        establishConnection();
        
        // Catch an exception that is thrown if the Client is not connected to the SeeFood Server
        try {

            // Define a File object to later store the picture filename
            File pict = null;

            int i = 0;
            Scanner userInput = new Scanner(System.in);
            ArrayList<byte[]> imageList = new ArrayList();
            
            // Loop to control user input of filename
            while (true && i < x) {

                // Accept and process user input
                System.out.print("Enter filename of picture to analyze or type menu: ");
                String pictureLoc = userInput.nextLine();

                // Allows user to return to menu instead of selecting a picture
                if (pictureLoc.equals("menu")) {
                    return;
                }

                // Create File object and test if the filename exists
                pict = new File(pictureLoc);
                if (!pict.exists()) {

                    // Alert user that the filename specified does not exist and loop cycles again
                    System.out.println("\n\n***File does not exist***\n\n");

                } else {

                    // Add image to arraylist
                    byte[] imageBytes = new byte[(int) pict.length()];
                    FileInputStream fis = new FileInputStream(pict);

                    fis.read(imageBytes);
                    fis.close();

                    imageList.add(imageBytes);
                    
                    ++i;
                    
                }

            }// End while

            // Aid with garbage collection of Scanner object as it is no longer need beyond this point
            userInput = null;

            // Send analyze command to server
            out.writeInt(1);
            out.flush();

            // Send the byte array to the Seefood Server
            out.writeObject(imageList);
            out.flush();

            // Get the results from server
            ArrayList<int[]> resultList = (ArrayList<int[]>)in.readObject();
            
            
            
            //NOT NEEDED FOR APP
            for(int[] xz : resultList){
            
            // Print the received results
            System.out.println("\n\nIs food:    " + xz[0] + "\n" + "Confidence: " + xz[1]);
            System.out.println("\n\n");
            
            }

        } catch (ConnectException ex) {
            System.out.println("\n\nNot connected to SeeFood server\n\n");
        }

        endConnection();

    }// End analyze()

    /**
     * Deletes the database of analyzed pictures on the SeeFood Server
     *
     * @throws IOException
     */
    private static void deleteDB() throws IOException {

        establishConnection();

        try {

            out.writeInt(7);
            out.flush();

            // Print confirmation from SeeFood Server
            System.out.println(in.readUTF());
            System.out.println("\n\n");

        } catch (ConnectException ex) {
            System.out.println("\n\nNot connected to SeeFood server\n\n");
        }

        endConnection();

    }// End deleteDB()

    /**
     * Creates a connection with the SeeFood Server
     *
     * @throws IOException
     */
    private static void establishConnection() throws IOException {

        // If no active connection exists, establish one with the SeeFood Server
        if (socket == null) {
            socket = new Socket(SERVER_ADDR, 3000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }

    }// End establishConnection()

    /**
     * Terminates connection with the SeeFood Server
     *
     * @throws IOException
     */
    private static void endConnection() throws IOException {

        // If there is an active connection with the SeeFood Server, end it otherwise do nothing
        if (socket != null) {
            out.close();
            out = null;
            in.close();
            in = null;
            socket.close();
            socket = null;
        }

    }// End Connection

    /**
     * Ideal way for how the gallery using ArrayLists should be implemented in
     * the Android client
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void gallery() throws IOException, ClassNotFoundException {

        establishConnection();
        
        // Create an ArrayList to store all images in the database
        ArrayList<Result> imageList;

        
        // gET RESULTS LIST FROM SERVER        
        imageList = getPictArrayList();
        
        endConnection();        
        
                
        // NOT needed for app
        int y = 1;
        for (Result r : imageList) {

            ByteArrayInputStream byteStream = new ByteArrayInputStream(r.getImage());
            BufferedImage img = ImageIO.read(byteStream);
            
            ImageIO.write(img, "jpg", new File(PIC_STORE_LOC + "IMAGE_" + y + ".jpg"));
            y++;
        }
    }

    /**
     * Gets the gallery of previously analyzed pictures and their results from
     * the SeeFood Server
     *
     * @throws IOException
     */
    private static ArrayList<Result> getPictArrayList() throws IOException, ClassNotFoundException {

        ArrayList<Result> al = null;

        try {

            // Tell server to get picture objects
            out.writeInt(2);
            out.flush();

            // Receive arraylist from server
            al = (ArrayList<Result>) in.readObject();


        } catch (ConnectException ex) {
            System.out.println("Not connected to SeeFood server\n\n");
        }

        // Return the ArrayList
        return al;

    }// End gallery()

    /**
     * Gets and prints the total statistics from the SeeFood Server
     *
     * @throws IOException
     */
    private static void getStats() throws IOException {

        establishConnection();

        try {

            out.writeInt(3);
            out.flush();

            // Create variable to hold retrieved results
            int total, food, not;
            total = food = not = -1;

            // Recieve the values from the SeeFood Server
            total = in.readInt();
            if (total == (-1)) {
                System.out.println("Error");
            } else {
                food = in.readInt();
                not = in.readInt();
            }

            // Calcuate the percentage of pictures in database that are food
            int percentFood = (int) ((food / (double) total) * 100);

            // Print values from SeeFood Server
            System.out.println("\n\n");
            System.out.println("Total: " + total);
            System.out.println("Food:  " + food + "   (" + percentFood + "%)");
            System.out.println("Not:   " + not + "   (" + ((total == 0) ? 0 : (100 - percentFood)) + "%)");
            System.out.println("\n\n");

        } catch (ConnectException ex) {
            System.out.println("Not connected to SeeFood server\n\n");
        }

        endConnection();

    }// End getStats()

    private static int getTotal() throws IOException {

        establishConnection();

        out.writeInt(8);
        out.flush();

        int total = in.readInt();

        endConnection();

        return total;

    }

    /**
     * Gets and prints the version number of the client and if connected, the
     * SeeFood Server
     *
     * @throws IOException
     */
    private static void getVersions() throws IOException {

        establishConnection();

        try {

            out.writeInt(6);
            out.flush();

            // Print the version numbers of the Client and SeeFood Server
            // NOT NEEDED for app
            System.out.println("\n\nVersion Numbers\n---------------");
            System.out.println("Client: " + VERSION);
            System.out.println("Server: " + in.readUTF());
            System.out.println("\n\n");

        } catch (ConnectException ex) {
            System.out.println("Client: " + VERSION);
            System.out.println("Server: Not connected to SeeFood server\n\n");
        }

        endConnection();

    }// End getVersions()

    /**
     * Terminates the SeeFood Server remotely
     *
     * @return Returns true to inform the client program loop to stop thus
     * terminating the client as well
     * @throws IOException
     */
    private static boolean killServer() throws IOException {

        establishConnection();

        boolean keepAlive;

        try {

            out.writeInt(5);
            out.flush();
            //System.out.println("Server dead, client exiting...\n\n");
            keepAlive = false;

        } catch (ConnectException ex) {
            //System.out.println("Not connected to SeeFood server\n\n");
            keepAlive = true;

        }

        endConnection();

        return keepAlive;

    }// End killServer()

}
