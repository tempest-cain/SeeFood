package ceg4110.seefood;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;


public class GalleryActivity extends AppCompatActivity {

    String TAG = "HI";
    //int rowId, width,height,weight,marginStart,marginEnd;
    static LinearLayout rowsContainer;
    static LinearLayout imgContainer;
    //             static ImageView img;
    ScrollView scrollView;
    static float GALLERY_SIZE = 500;
    static int rowId[];
    int rowIndex = 1;
    ArrayList<Result> imageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Bundle receiveArgs = getIntent().getExtras();
        GALLERY_SIZE = receiveArgs.getInt("GALLERY_SIZE");

        //capture scrolling activity
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {

            long millis;

            float height;

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int action = event.getAction();
//                float height = event.getY();
//                long millis = System.currentTimeMillis();

                if(action == MotionEvent.ACTION_DOWN){
//                    this.height = height;
//                    this.millis = millis;
                    try {
                        addImages();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(action == MotionEvent.ACTION_UP){
                    if(this.height < height){
//                        Log.v(TAG, "Scrolled up "+(this.height - height) +" AND it took "+(millis - this.millis) +"ms");
                        try {
                            addImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(this.height > height){
//                        Log.v(TAG, "Scrolled down "+(this.height - height) +" AND it took "+(millis - this.millis) +"ms");
                        try {
                            addImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }

        });

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
        new MyAsyncTask().execute("2");

    }


    public void addImages() throws IOException {


        for(int i = 0; i< (int)GALLERY_SIZE/3+1; i++){

            LinearLayout row = (LinearLayout) findViewById(rowId[i]);



            Rect scrollBounds = new Rect();
            scrollView.getHitRect(scrollBounds);
            ImageView col1 = (ImageView) ((FrameLayout)row.getChildAt(0)).getChildAt(1);
            ImageView col2 = (ImageView) ((FrameLayout)row.getChildAt(1)).getChildAt(1);
            ImageView col3 = (ImageView) ((FrameLayout)row.getChildAt(2)).getChildAt(1);

            ImageView col1Result = (ImageView) ((FrameLayout)row.getChildAt(0)).getChildAt(2);
            ImageView col2Result = (ImageView) ((FrameLayout)row.getChildAt(1)).getChildAt(2);
            ImageView col3Result = (ImageView) ((FrameLayout)row.getChildAt(2)).getChildAt(2);

            if (row.getLocalVisibleRect(scrollBounds)) {

//                Drawable img1 = new BitmapDrawable();
//                Drawable img2 = new BitmapDrawable(imageList.get(1).getImage());
//                Drawable img3 = new BitmapDrawable(imageList.get(2).getImage());
                if(imageList != null) {
//                    Bitmap img1 = imageList.get(0).getImage();
//                    col1.setImageBitmap(img1);
//                col2.setBackground(img2);
//                col3.setBackground(img3);

                    col1Result.setBackgroundResource(R.drawable.tick);
                    col2Result.setBackgroundResource(R.drawable.x);
                    col3Result.setBackgroundResource(R.drawable.tick);
                }
            } else {
                col1.setBackgroundResource(R.color.loadingImage);
                col2.setBackgroundResource(R.color.loadingImage);
                col3.setBackgroundResource(R.color.loadingImage);



            }

//                    if (row.getId() == R.id.row3) {
//                        col1.setImageResource(R.drawable.camera);
//                        col2.setImageResource(R.drawable.camera);
//                        col3.setImageResource(R.drawable.camera);
//                    }
//            if (row.getId() == R.id.row2) {
//                col1.setBackgroundResource(R.drawable.food5);
//                col2.setBackgroundResource(R.drawable.food6);
//                col3.setBackgroundResource(R.drawable.food6);
//            }
        }
    }


    /**
     * Creates an array with a reference to each R.id.row(n)
     * rowId[0] is the id for row1
     */
    public static void getRowIds() {

        rowId = new int[(int)GALLERY_SIZE];
        int firstRowId = R.id.row1;
        for (int i = 0; i < GALLERY_SIZE; i++) {
            rowId[i] = firstRowId;
            firstRowId += 111;
        }

    }

    public static void create() {









//        LinearLayout l = (LinearLayout)rowsContainer.getChildAt(1);
//        ImageView v = (ImageView) (l.getChildAt(1));
//        v.setImageResource(R.drawable.camera2);


//        rowsContainer.addView(newRow);


//        img = (ImageView) findViewById(R.id.imageView);


//        img.setImageResource(R.mipmap.ic_launcher);
//        create();
//        imgContainer = (LinearLayout)rowsContainer.getChildAt(1);


//        rowsContainer.addView(imgContainer);
//        imgContainer.getChildAt(1).setBackgroundResource(R.drawable.browse);

//        for(int i = 0; i < 5; i++) {
//            img = new ImageView(rowsContainer.getContext(),);
//            img.setImageResource(R.mipmap.ic_launcher);
//
//
//            rowsContainer.addView(img);
//
//        }


//            Toast t;
//
//        CharSequence m = ""+R.id.row2+"\n"+R.id.row3+"\n"+R.id.row4+"\n"+R.id.row5+"\n"+R.id.row6+"\n"+R.id.row7+"\n"+
//                ""+R.id.row8+"\n"+R.id.row9+"\n"+R.id.row10+"\n"+R.id.row11+"\n"+R.id.row12+"\n"+R.id.row13+"\n";
//        t = Toast.makeText(rowsContainer.getContext(),m,Toast.LENGTH_LONG);
//        t.show();

    }

//        protected LinearLayout createRow() {
//
//
//
//
//        ImageView img = new ImageView(GalleryActivity.this);
//        return null;
//    }



    class MyAsyncTask extends AsyncTask<String,Void,Integer> {

        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private final static String SERVER_ADDR = "34.227.5.168";//"34.227.5.168" : "127.0.0.1"
        int isFood, confidence;
        int total, food, not;



        protected Integer doInBackground(String... args) {

            try {
                switch (args[0]) {
                    case "1": //analyze image
                        //analyze(args[1]);
                        return 1;
                    case "2": //gallery
                        gallery();
                        return 2;
                    case "3": //statistics
                        getStats();
                        return 3;
                }
            } catch (IOException e) {} catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return -1;

        }

        protected void onPostExecute(Integer result) {
            GALLERY_SIZE = total;
            try {
                if(result == 2)
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
                if (total != (-1)) {
                    food = in.readInt();
                    not = in.readInt();
                }
            } catch (ConnectException ex) {}
        }// End getStats()

        private void gallery() throws IOException, ClassNotFoundException {

            total = getTotal();

            // Create an ArrayList to store all images in the database
            imageList = new ArrayList();

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

        } // end gallery()


        /**
         * Gets the gallery of previously analyzed pictures and their results from
         * the SeeFood Server
         *
         * @throws IOException
         */
        private ArrayList<Result> getPictArrayList() throws IOException, ClassNotFoundException {

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
                //               al = new ArrayList<Result>();
                int alistSize = in.readInt();
//                int alistSize = 2;


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

        private int getTotal() throws IOException {

            establishConnection();

            out.writeInt(8);
            out.flush();

            int total = in.readInt();

            endConnection();

            return total;

        }
    }

}


class Result implements Serializable {

    private int food = -1;
    private int conf = -1;
    private byte[] image = null;

    public Result(int food, int conf, byte[] image){

        this.food = food;
        this.conf = conf;
        this.image = image;

    }// End Result()

    /**
     * Returns the value indicating whether food is present or not
     * @return Food present or not value
     */
    public int getFood(){
        return this.food;
    }// End getFood()

    /**
     * Returns the AI confidence for this picture
     * @return AI confidence value
     */
    public int getConf(){
        return this.conf;
    }// End getConf()

    /**
     * Returns the image
     * @return A BufferedImage containing the analyzed image in this object
     * @throws IOException
     */
    public Bitmap getImage() throws IOException {

        Bitmap img = BitmapFactory.decodeByteArray(image,0,image.length);

        // Return byte array
        return img;
    }// End getImage()

}