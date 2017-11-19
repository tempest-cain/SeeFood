package ceg4110.seefood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_DOWN;
import static ceg4110.seefood.R.id.image;
import static ceg4110.seefood.R.id.parent;


public class MainActivity extends AppCompatActivity {

    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int READ_REQUEST_CODE = 42;


    ImageView cameraButton;
    ImageView browseButton;
    ImageView galleryButton;
    ImageView statsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = (ImageView)findViewById(R.id.cameraButton);
        browseButton = (ImageView)findViewById(R.id.browseButton);
        galleryButton = (ImageView)findViewById(R.id.galleryButton);
        statsButton = (ImageView)findViewById(R.id.statsButton);

        /*
        OnTouch & OnClick Listeners for buttons
         */
        cameraButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP: cameraButton.setImageResource(R.drawable.camera);
                        break;
                    case ACTION_DOWN:
                        cameraButton.setImageResource(R.drawable.camera2);
                        dispatchTakePictureIntent();
                        break;
                }
                return true;
            }
        });

        browseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP: browseButton.setImageResource(R.drawable.browse);
                        break;
                    case ACTION_DOWN:
                        browseButton.setImageResource(R.drawable.browse2);
                        performFileSearch();
                        break;
                }
                return true;
            }
        });


        galleryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP: galleryButton.setImageResource(R.drawable.gallery);
                        break;
                    case ACTION_DOWN:
                        galleryButton.setImageResource(R.drawable.gallery2);
                        new MyAsyncTask().execute("2");
                        break;
                }
                return true;
            }
        });

        statsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP: statsButton.setImageResource(R.drawable.stats);
                        break;
                    case ACTION_DOWN:
                        statsButton.setImageResource(R.drawable.stats2);
                        new MyAsyncTask().execute("3");
                        break;
                }
                return true;
            }
        });
    }


    /**
     * Uses android's built-in camera app to take a picture.
     * The taken photo's path will be stored in the member variable mCurrentPhotoPath
     * @param v
     */
    public void dispatchTakePictureIntent() {

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



    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
//                Log.i(TAG, "Uri: " + uri.toString());
//                showImage(uri);
//                Context context = getApplicationContext();
//                CharSequence text = "Not food! Confidence: 75%";
//                int duration = Toast.LENGTH_LONG;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();
                File image = new File(uri.getPath());
                mCurrentPhotoPath = image.getAbsolutePath();
                mCurrentPhotoPath = mCurrentPhotoPath.replaceAll("/document/primary:","/storage/emulated/0/");
                new MyAsyncTask().execute("1",mCurrentPhotoPath);

//                Bundle extras = resultData.getExtras();

            }
        } else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            new MyAsyncTask().execute("1",mCurrentPhotoPath);
        }
    }

    /**
     * Creates an asynchronous thread to handle communication with the server
     */
    class MyAsyncTask extends AsyncTask<String,Void,Integer> {

        private final static String VERSION = "3.1.1";
        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private final static String SERVER_ADDR = "34.227.5.168";//"34.227.5.168" : "127.0.0.1"
        int isFood, confidence;
        int total, food, not;

        /**
         * Communicates with the server
         * @param action -
         * @return
         */
        @Override
        protected Integer doInBackground(String... args) {

        try {
            switch (args[0]) {
                case "1": //analyze image
                    analyze(args[1]);
                    return 1;
                case "2": //gallery
                    //call function
                    return 2;
                case "3": //statistics
                    getStats();
                    return 3;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Context context = getApplicationContext();
            CharSequence message;
            Toast toast;
            switch (result) {
                case 1:
                    message = isFood==1?"I see food! ":"Not food. " + "Confidence level: "+confidence+"%";
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                case 2:
                    message = "Feature not implemented yet";
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                case 3:
                    message = "Food:\t"+food+"\nNot food:\t"+not+"\nPercentage of food pictures: "+(int) ((food / (double) total) * 100);
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                case -1:
                    message = "Error";
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
            }
        }

        private void analyze(String pictureLoc) throws IOException {

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
                isFood = in.readInt();
                confidence = in.readInt();

                // Print the received results
                System.out.println("\n\nIs food:    " + isFood + "\n" + "Confidence: " + confidence);
                System.out.println("\n\n");



            } catch (ConnectException ex) {
                System.out.println("\n\nNot connected to SeeFood server\n\n");
            }

        }// End analyze()


        /**
         * Establishes a connection to the SeeFood server.
         * @throws IOException
         */
        private void establishConnection() throws IOException {

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
        private void endConnection() throws IOException {

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
        private void getStats() throws IOException {

            try {
                // Connect to SeeFood Server if not already connected and send the get statistics command
                establishConnection();


                out.writeInt(3);
                out.flush();

                // Create variable to hold retrieved results

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
        }// End getStats()


    }


}
