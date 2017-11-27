/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private final static String VERSION = "3.2.2.2";
    private static Socket socket = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;
    private final static String SERVER_ADDR = "34.227.5.168";
    //private final static String SERVER_ADDR = "127.0.0.1";
    private final static String PIC_STORE_LOC = "/home/james/Desktop/ServerPICs/";
    private static int total = -1;
    private static int imageWidth = 500;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Controls program flow via a while loop
        boolean loopVal = true;

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
                    + "7) Delete Database       \n\n"
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
                    analyze();      // Implemented
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
    private static void analyze() throws IOException {

        establishConnection();

        // Catch an exception that is thrown if the Client is not connected to the SeeFood Server
        try {

            // Define a File object to later store the picture filename
            File pict = null;

            // Loop to control user input of filename
            while (true) {

                // Accept and process user input
                System.out.print("Enter filename of picture to analyze or type menu: ");
                Scanner userInput = new Scanner(System.in);
                String pictureLoc = userInput.nextLine();

                // Allows user to return to menu instead of selecting a picture
                if (pictureLoc.equals("menu")) {
                    return;
                }

                // Create File object and test if the filename exists
                pict = new File(pictureLoc);
                if (pict.exists()) {

                    // Aid with garbage collection of Scanner object as it is no longer need beyond this point
                    userInput = null;
                    break;
                }

                // Alert user that the filename specified does not exist and loop cycles again
                System.out.println("\n\n***File does not exist***\n\n");

            }

            // Alert user that the server is analyzing the selected picture
            System.out.println("Analyzing");

            out.writeInt(1);
            out.flush();

            /////////////////////////
            //
            // Resize logic below
            //
            /////////////////////////
            BufferedImage img = ImageIO.read(pict);

            Image img2 = img.getScaledInstance(imageWidth, -1, Image.SCALE_SMOOTH);

            BufferedImage copy = new BufferedImage(img2.getWidth(null), img2.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics data = copy.createGraphics();
            data.drawImage(img2, 0, 0, null);
            data.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(copy, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            // Send the byte array to the Seefood Server
            out.writeObject(imageBytes);
            out.flush();

            // Wait for the SeeFood Server to send back results
            int isFood = in.readInt();
            int confidence = in.readInt();

            // Print the received results
            System.out.println("\n\nIs food:    " + isFood + "\n" + "Confidence: " + confidence);
            System.out.println("\n\n");

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
     * Ideal way for how the gallery using ArrayLists should be implemented in the Android client
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private static void gallery() throws IOException, ClassNotFoundException {
        
        // Get the total amount of pictures in the server database
        total = getTotal();
        
        // Create an ArrayList to store all images in the database
        ArrayList<Result> imageList = new ArrayList();

        /* Keep adding images to ArrayList until all have been received
        *  NOTE this is where there will be a big difference between this and the Android app
        *  On the app when the user selects the gallery in the app get the total first, then as you scroll
        *  it will load the first 20 images and then when the scroll part tells it to load more it
        *  will load 20 more, See getPictArrayList(), you may want to implement this differently, if so
        *  we need to work on it so that nothing breaks
        */
        while (total > 0) {
            imageList.addAll(getPictArrayList());
        }

        // NOT needed for app
        int y = 1;
        for (Result r : imageList) {
            ImageIO.write(r.getImage(), "jpg", new File(PIC_STORE_LOC + "IMAGE_" + y + ".jpg"));
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

        establishConnection();
        ArrayList<Result> al = null;

        try {

            // Tell server to get picture objects
            out.writeInt(2);
            out.flush();

            // Tell server to begin sending picture objects starting with the most recent
            out.writeInt(total);
            out.flush();

            // Read each picture object into an ArrayList
            // The ArrayList returned will either have 20 elements or if there are
            // less than 20 needing to be sent the ArrayList will have the same number
            // of elements as there are picture objects still needing to be downloaded
            // on the server, ex. if there are 6 pictures on the server, then the ArrayList
            // returned from the server will contain 6 elements
            al = (ArrayList<Result>) in.readObject();
            int alistSize = in.readInt();

            // Update total variable to tell client how many picture objects still need to be downloaded
            // total is a member(class variable)
            total -= alistSize;

        } catch (ConnectException ex) {
            System.out.println("Not connected to SeeFood server\n\n");
        }

        endConnection();

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
            System.out.println("Server dead, client exiting...\n\n");
            keepAlive = false;

        } catch (ConnectException ex) {
            System.out.println("Not connected to SeeFood server\n\n");
            keepAlive = true;

        }

        endConnection();

        return keepAlive;

    }// End killServer()

}
