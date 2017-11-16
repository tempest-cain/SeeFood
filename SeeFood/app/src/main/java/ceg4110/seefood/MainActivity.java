package ceg4110.seefood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    /**
     * Uses android's built-in camera app to take a picture.
     * The taken photo's path will be stored in the member variable mCurrentPhotoPath
     * @param v
     */
    public void dispatchTakePictureIntent(View v) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ceg4110.seefood",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Sends the picture in mCurrentPhotoPath to the server.
     * @param v
     */
    public void sendPicture(View v) {

        new MyAsyncTask().execute(mCurrentPhotoPath);


    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private static final int READ_REQUEST_CODE = 42;

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(View v) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

}

/**
 * Creates an asynchronous thread to handle communication with the server
 */
class MyAsyncTask extends AsyncTask<String,Void,Void> {

    private final static String VERSION = "3.1.1";
    private static Socket socket = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;
    private final static String SERVER_ADDR = "34.227.5.168";//"34.227.5.168" : "127.0.0.1"

    /**
     * Communicates with the server
     * @param action -
     * @return
     */
    @Override
    protected Void doInBackground(String... img) {

//        try {
//            switch (action) {
//                case 1:
//                    //call function
//                    break;
//                case 2:
//                    //call function
//                    break;
//                case 1:
//                    //call function
//                    break;
//                case 2:
//                    //call function
//                    break;
//                case 1:
//                    //call function
//                    break;
//                case 2:
//                    //call function
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            analyze(img[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void analyze(String pictureLoc) throws IOException {

        // Catch an exception that is thrown if the Client is not connected to the SeeFood Server
        try {

            // Define a File object to later store the picture filename
            File pict = null;

            // Loop to control user input of filename
            //while (true) {

            // Accept and process user input
//                System.out.print("Enter filename of picture to analyze or type menu: ");
//                Scanner userInput = new Scanner(System.in);
//                String pictureLoc = userInput.nextLine();

            // Allows user to return to menu instead of selecting a picture
            if (pictureLoc.equals("menu")) {
                return;
            }

            // Create File object and test if the filename exists
            pict = new File(pictureLoc);
            if (pict.exists()) {

                // Aid with garbage collection of Scanner object as it is no longer need beyond this point
//                    userInput = null;
//                    break;
            }

            // Alert user that the filename specified does not exist and loop cycles again
            System.out.println("\n\n***File does not exist***\n\n");

            //}

            // Alert user that the server is analyzing the selected picture
            System.out.println("Analyzing");

            // Create connection to server if not already connected and send server the proper command
            establishConnection();
            out.writeInt(1);
            out.flush();

            // Create a FileInputStream object to convert picture into a byte array
            FileInputStream g = new FileInputStream(pict);

            // Create byte array to hold picture
            byte[] byteArray = new byte[(int) pict.length()];
            g.read(byteArray);

            // Close FileInputStream
            g.close();

            // Send the byte array to the Seefood Server
            out.writeObject(byteArray);
            out.flush();
            // End of sending data to the SeeFood Server

            // Wait for the SeeFood Server to send back results
            int isFood = in.readInt();
            int confidence = in.readInt();

            // Print the received results
            System.out.println("\n\nIs food:    " + isFood + "\n" + "Confidence: " + confidence);
            System.out.println("\n\n");
            endConnection();


        } catch (ConnectException ex) {
            System.out.println("\n\nNot connected to SeeFood server\n\n");
        }

    }// End analyze()


    /**
     * Establishes a connection to the SeeFood server.
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
     * Ends the connection created by establishConnection().
     * @throws IOException
     */
    private static void endConnection() throws IOException {

        // If there is an active connection with the SeeFood Server, end it otherwise do nothing
        if (socket != null) {
            out.close();
            in.close();
            socket.close();
            socket = null;
        }

    }// End endConnection()

    /**
     * Requests the current stats of the SeeFood server.
     * @throws IOException
     */
    private static void getStats() throws IOException {

        try {
            // Connect to SeeFood Server if not already connected and send the get statistics command
            establishConnection();


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
            endConnection();

        } catch (ConnectException ex) {
            System.out.println("Not connected to SeeFood server\n\n");
        }
    }// End getStats()
}

