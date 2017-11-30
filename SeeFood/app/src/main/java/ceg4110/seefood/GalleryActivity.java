package ceg4110.seefood;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import result.Result;


public class GalleryActivity extends AppCompatActivity {

    static LinearLayout rowsContainer;
    static LinearLayout imgContainer;
    ScrollView scrollView;
    static float GALLERY_SIZE = 500;
    static int rowId[];
    int rowIndex = 1;
    ArrayList<Result> imageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //receive arguments from previous view
        Bundle receiveArgs = getIntent().getExtras();
        GALLERY_SIZE = receiveArgs.getInt("GALLERY_SIZE");

        scrollView = (ScrollView) findViewById(R.id.scrollView);

        getRowIds();

        rowsContainer = (LinearLayout) findViewById(R.id.rowsContainer);
        imgContainer = (LinearLayout) findViewById(R.id.row1);

        //Create LayoutInflater to add rows dynamically
        for (int i = 0; i < (int)GALLERY_SIZE/3; i++) {

            LayoutInflater inflater = this.getLayoutInflater();
            ViewGroup newRow = (ViewGroup) inflater.inflate(R.layout.new_row, (ViewGroup) findViewById(R.id.newRow));
            ViewGroup.LayoutParams rowParams = imgContainer.getLayoutParams();
            newRow.setId(rowId[rowIndex++]);
            rowsContainer.addView(newRow,rowsContainer.getChildCount(), rowParams);
        }

        rowIndex = 0;
        new MyAsyncTask().execute();

    }


    /**
     * Adds images to gallery.
     * @throws IOException
     */
    public void addImages() throws IOException {

        int imageCounter = 0;
        int size = imageList.size();

        for(int i = imageCounter/3; imageCounter<size;i = imageCounter/3) {

            LinearLayout row = (LinearLayout) findViewById(rowId[i]);

            ImageView imgv = (ImageView) ((FrameLayout) row.getChildAt(imageCounter%3)).getChildAt(1);
            ImageView result = (ImageView) ((FrameLayout) row.getChildAt(imageCounter%3)).getChildAt(2);

            Result imgData = imageList.get(imageCounter++);
            Bitmap img = BitmapFactory.decodeByteArray(imgData.getImage(), 0, imgData.getImage().length);
            imgv.setImageBitmap(img);
            result.setBackgroundResource(imgData.getFood() == 1 ? R.drawable.tick : R.drawable.x);

        }

        //remove empty ImageViews
        LinearLayout lastRow = (LinearLayout) findViewById(rowId[imageCounter/3]);
        if(imageCounter%3 == 0){
            lastRow.removeAllViews();
        } else if(imageCounter%3 == 1) {
            ((FrameLayout)(lastRow.getChildAt(1))).removeAllViews();
            ((FrameLayout)(lastRow.getChildAt(2))).removeAllViews();
        }else if(imageCounter%3 == 2) {
            ((FrameLayout)(lastRow.getChildAt(2))).removeAllViews();
        }

    }


    /**
     * Creates an array with a reference to each R.id.row(n)
     * rowId[0] is the id for row1
     */
    public static void getRowIds() {

        RowId idArray = new RowId();
        rowId = idArray.getRowIdArray((int)GALLERY_SIZE/3);

    }

    /**
     * Creates an asynchronous thread to handle communication with the server
     */
    class MyAsyncTask extends AsyncTask<Void,Void,Integer> {

        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private final static String SERVER_ADDR = "34.227.5.168";//"34.227.5.168" : "127.0.0.1"

        /**
         * Facilitates communication with the server
         */
        protected Integer doInBackground(Void... voids) {

            try {
                // get gallery images
                establishConnection();
                gallery();
                endConnection();
                return 1;
            } catch (IOException e) {
                return -1;
            } catch (ClassNotFoundException e) {
                return -1;
            }

        }

        /**
         * Adds images to the gallery.
         * @param result
         */
        protected void onPostExecute(Integer result) {

            try {
                if(result == 1)
                    addImages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void establishConnection() throws IOException {

            // If no active connection exists, establish one with the SeeFood Server
            if (socket == null) {
                socket = new Socket(SERVER_ADDR, 3000);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            }

        }// End establishConnection()

        private void gallery() throws IOException, ClassNotFoundException {

            imageList = getPictArrayList();

        } // end gallery()


        /**
         * Gets the gallery of previously analyzed pictures and their results from
         * the SeeFood Server
         * @throws IOException
         */
        private ArrayList<Result> getPictArrayList() throws IOException, ClassNotFoundException {

            ArrayList<Result> al = null;

            try {

                // Tell server to get picture objects
                out.writeInt(2);
                out.flush();



                // Read each picture object into an ArrayList
                // The ArrayList returned will have all images along with values
                // to  specify whether they are food or not
                al = (ArrayList<Result>) in.readObject();

            } catch (ConnectException ex) {
                //Not connected to SeeFood server
            }

            // Return the ArrayList
            return al;

        }// End gallery()

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


