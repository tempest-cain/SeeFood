package ceg4110.seefood;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_DOWN;


public class MainActivity extends AppCompatActivity {

    private String[] mCurrentPhotoPath = new String[5];
    private String tempPath;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RETURN_FROM_SELECTION_MENU = 4;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int READ_REQUEST_CODE = 42;
    private static final int BACK_FROM_RESULT = 2;

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    Intent intent;
    RelativeLayout main;
    boolean alphaModified = false;
    boolean readyToSend = false;
    int selectionCounter = 0;

    ImageView cameraButton;
    ImageView browseButton;
    ImageView galleryButton;
    ImageView statsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main = (RelativeLayout)findViewById(R.id.mainActivityLayout);
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
                    case ACTION_UP:
                        cameraButton.setImageResource(R.drawable.camera);
                        dispatchTakePictureIntent();
                        break;
                    case ACTION_DOWN:
                        cameraButton.setImageResource(R.drawable.camera2);
                        break;
                }
                return true;
            }
        });

        browseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP:
                        browseButton.setImageResource(R.drawable.browse);
                        performFileSearch();
                        break;
                    case ACTION_DOWN:
                        browseButton.setImageResource(R.drawable.browse2);
                        break;
                }
                return true;
            }
        });

        galleryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP:
                        galleryButton.setImageResource(R.drawable.gallery);
                        //new MyAsyncTask().execute("2");
                        intent = new Intent(MainActivity.this, GalleryActivity.class);
                        new MyAsyncTask().execute("2");
                        break;
                    case ACTION_DOWN:
                        galleryButton.setImageResource(R.drawable.gallery2);

                        break;
                }
                return true;
            }
        });

        statsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP:
                        statsButton.setImageResource(R.drawable.stats);
                        intent = new Intent(MainActivity.this, StatisticsActivity.class);
                        new MyAsyncTask().execute("3");
                        break;
                    case ACTION_DOWN:
                        statsButton.setImageResource(R.drawable.stats2);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if(alphaModified) {
            main.setAlpha(1);
            alphaModified = false;
        }


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
     * Creates a file that will hold the image
     * @return the file where the image will be stored
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
        tempPath = image.getAbsolutePath();


        return image;
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) { //browse
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                //get file name
                String fileName = "no-path-found";
                String[] filePathColumn = {MediaStore.Images.Media.SIZE};
                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                if(cursor.moveToFirst()){
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    fileName = cursor.getString(columnIndex);
                }
                cursor.close();

                //parse file path
                String absolutePath = "";

                try {
                    InputStream inStr = getContentResolver().openInputStream(uri);

                    byte[]  buffer = new byte[Integer.parseInt(fileName)];
                    inStr.read(buffer);
                    inStr.close();

                    File myFile = createImageFile();
                    FileOutputStream out = new FileOutputStream(myFile);
                    out.write(buffer);
                    out.close();

                    absolutePath = myFile.getAbsolutePath();


                } catch (FileNotFoundException e) {
                } catch (IOException e) {}


                switch (selectionCounter) {
                    case 0:
                        mCurrentPhotoPath[selectionCounter++] = absolutePath;
                        break;
                    case 1:
                        mCurrentPhotoPath[selectionCounter++] = absolutePath;
                        break;
                    case 2:
                        mCurrentPhotoPath[selectionCounter++] = absolutePath;
                        break;
                    case 3:
                        mCurrentPhotoPath[selectionCounter++] = absolutePath;
                        break;
                    case 4:
                        mCurrentPhotoPath[selectionCounter++] = absolutePath;
                        readyToSend = true;
                }

                if(readyToSend) {

                    verifyStoragePermissions(this);
                    triggerAsyncTask();

                } else {
                    //displayselection
                    Intent openSelection = new Intent(MainActivity.this,DisplaySelection.class);
                    openSelection.putExtra("image1Path",mCurrentPhotoPath[0]);
                    openSelection.putExtra("image2Path",mCurrentPhotoPath[1]);
                    openSelection.putExtra("image3Path",mCurrentPhotoPath[2]);
                    openSelection.putExtra("image4Path",mCurrentPhotoPath[3]);
                    openSelection.putExtra("image5Path",mCurrentPhotoPath[4]);
                    openSelection.putExtra("selectionCount",selectionCounter);
                    main.setAlpha((float).5);
                    alphaModified = true;
                    startActivityForResult(openSelection, RETURN_FROM_SELECTION_MENU);
                }

            }
        } else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) { //from camera

            switch (selectionCounter) {
                case 0:
                    mCurrentPhotoPath[selectionCounter++] = tempPath;
                    break;
                case 1:
                    mCurrentPhotoPath[selectionCounter++] = tempPath;
                    break;
                case 2:
                    mCurrentPhotoPath[selectionCounter++] = tempPath;
                    break;
                case 3:
                    mCurrentPhotoPath[selectionCounter++] = tempPath;
                    break;
                case 4:
                    mCurrentPhotoPath[selectionCounter++] = tempPath;
                    readyToSend = true;
            }

            if(readyToSend)
                triggerAsyncTask();
            else {

                //show selection
                Intent openSelection = new Intent(MainActivity.this,DisplaySelection.class);
                openSelection.putExtra("image1Path",mCurrentPhotoPath[0]);
                openSelection.putExtra("image2Path",mCurrentPhotoPath[1]);
                openSelection.putExtra("image3Path",mCurrentPhotoPath[2]);
                openSelection.putExtra("image4Path",mCurrentPhotoPath[3]);
                openSelection.putExtra("image5Path",mCurrentPhotoPath[4]);
                openSelection.putExtra("selectionCount",selectionCounter);
                main.setAlpha((float).5);
                alphaModified = true;
                startActivityForResult(openSelection, RETURN_FROM_SELECTION_MENU);

            }





        } else if(requestCode == RETURN_FROM_SELECTION_MENU && resultCode == Activity.RESULT_OK) { // back from selection
            readyToSend = resultData.getBooleanExtra("DONE",false);

            if(readyToSend)
                triggerAsyncTask();


        }
    }


    /**
     * Creates a MyAsyncTask to perform a task over the network
     */
    public void triggerAsyncTask() {

        //verify valid permissions to read files
        verifyStoragePermissions(this);


        switch (selectionCounter) {
            case 1:
                new MyAsyncTask().execute("1",
                        mCurrentPhotoPath[0]);
                break;
            case 2:
                new MyAsyncTask().execute("1",
                        mCurrentPhotoPath[0],
                        mCurrentPhotoPath[1]);
                break;
            case 3:
                new MyAsyncTask().execute("1",
                        mCurrentPhotoPath[0],
                        mCurrentPhotoPath[1],
                        mCurrentPhotoPath[2]);
                break;
            case 4:
                new MyAsyncTask().execute("1",
                        mCurrentPhotoPath[0],
                        mCurrentPhotoPath[1],
                        mCurrentPhotoPath[2],
                        mCurrentPhotoPath[3]);
                break;
            case 5:
                new MyAsyncTask().execute("1",
                        mCurrentPhotoPath[0],
                        mCurrentPhotoPath[1],
                        mCurrentPhotoPath[2],
                        mCurrentPhotoPath[3],
                        mCurrentPhotoPath[4]);
        }

    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public void verifyStoragePermissions(Activity activity) {
        // Check if the app has write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //prompt the user for permission
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    /**
     * Creates an asynchronous thread to handle communication with the server
     */
    class MyAsyncTask extends AsyncTask<String,Void,Integer> {

        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private final static String SERVER_ADDR = "34.227.5.168";//"34.227.5.168" : "127.0.0.1"
        ArrayList<byte[]> imageList;

        int[] isFood, confidence;
        int total, food, not;

        /**
         * Facilitates communication with the server
         */
        @Override
        protected Integer doInBackground(String... args) {

        try {
            switch (args[0]) {
                case "1": // analyze request
                    establishConnection();
                    imageList = new ArrayList();
                    addFileToList(mCurrentPhotoPath[0]);
                    if(selectionCounter > 1) addFileToList(mCurrentPhotoPath[1]);
                    if(selectionCounter > 2) addFileToList(mCurrentPhotoPath[2]);
                    if(selectionCounter > 3) addFileToList(mCurrentPhotoPath[3]);
                    if(selectionCounter > 4) addFileToList(mCurrentPhotoPath[4]);
                    sendData();
                    receiveData();
                    endConnection();
                    return 1;
                case "2": //open gallery
                    establishConnection();
                    getStats();
                    endConnection();
                    return 2;
                case "3": //open statistics
                    establishConnection();
                    getStats();
                    endConnection();
                    return 3;
            }
        } catch (IOException e) {
            return -1;
        } catch (ClassNotFoundException e) {}
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {

            Context context = getApplicationContext();
            CharSequence message;
            Toast toast;
            switch (result) {
                case 1:
                    Intent displayResult = new Intent(MainActivity.this, DisplayResult.class);
                    main.setAlpha((float).5);
                    alphaModified = true;
                    displayResult.putExtra("isFood",isFood);
                    displayResult.putExtra("confidence",confidence);
                    displayResult.putExtra("imagePath",mCurrentPhotoPath);
                    displayResult.putExtra("selectionCount",selectionCounter);
                    startActivity(displayResult);
                    selectionCounter = 0;
                    readyToSend = false;
                    return;
                case 2:
                    message = "Loading Gallery...";
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    intent.putExtra("GALLERY_SIZE",total);
                    startActivity(intent);
                    return;
                case 3:
                    intent.putExtra("food",food);
                    intent.putExtra("not", not);
                    intent.putExtra("total",total);
                    startActivity(intent);
                    return;
                case -1:
                    message = "Error";
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
            }
        }


        /**
         * Adds the file at the given path to imageList
         * @param pictureLoc path for image to be added
         * @throws IOException
         */
        private void addFileToList(String pictureLoc) throws IOException {

            try {

                File pict = new File(pictureLoc);

                // Create a FileInputStream object to convert picture into a byte array
                FileInputStream g = new FileInputStream(pict);

                // Create byte array to hold picture
                byte[] byteArray = new byte[(int) pict.length()];
                g.read(byteArray);

                // Close FileInputStream
                g.close();

                imageList.add(byteArray);

            } catch (ConnectException ex) {}

        }// End addFileToList()

        /**
         * Sends imageList to the server
         */
        private void sendData() {

            try {
                //send request code
                out.writeInt(1);
                out.flush();

                // Send the byte array to the Seefood Server
                out.writeObject(imageList);
                out.flush();
                // End of sending data to the SeeFood Server


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * Receives results from the server
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private void receiveData() throws IOException, ClassNotFoundException {

            ArrayList<int[]> results = new ArrayList();
            // Wait for the SeeFood Server to send back results
            results = (ArrayList<int[]>) in.readObject();

            confidence = new int[results.size()];
            isFood = new int[results.size()];

            for(int i = 0; i < results.size(); i++) {

                isFood[i] = results.get(i)[0];
                confidence[i] = results.get(i)[1];
            }
        }


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
         * Requests the current stats from the SeeFood server.
         * @throws IOException
         */
        private void getStats() throws IOException {

            try {

                //send request code
                out.writeInt(3);
                out.flush();

                // Create variable to hold retrieved results
                total = food = not = -1;

                // Recieve the values from the SeeFood Server
                total = in.readInt();
                if (total != (-1)) {
                    food = in.readInt();
                    not = in.readInt();
                }
            } catch (ConnectException ex) {}
        }// End getStats()

        /**
         * Terminates connection with the SeeFood Server
         *
         * @throws IOException
         */
        private void endConnection() throws IOException {

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

    }
}

