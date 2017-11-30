package ceg4110.seefood;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

public class DisplaySelection extends FragmentActivity {

    static int NUM_ITEMS;
    MyAdapter mAdapter;
    static String[] filePath = new String[5];
    ViewPager mPager;
    final int BACK_TO_MAIN_ACTIVITY = 4;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_selection);

        //receive arguments from previous view
        Bundle receiveArgs = getIntent().getExtras();
        filePath[0] = receiveArgs.getString("image1Path");
        filePath[1] = receiveArgs.getString("image2Path");
        filePath[2] = receiveArgs.getString("image3Path");
        filePath[3] = receiveArgs.getString("image4Path");
        filePath[4] = receiveArgs.getString("image5Path");
        NUM_ITEMS = receiveArgs.getInt("selectionCount");

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Watch for button clicks.
        final ImageView button = (ImageView)findViewById(R.id.addMoreImages);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP:
                        button.setImageResource(R.drawable.add_more_images);
                        Intent intent = new Intent();
                        intent.putExtra("DONE", false);
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    case ACTION_DOWN:
                        button.setImageResource(R.drawable.add_more_images2);
                        break;
                }
                return true;
            }
        });
        final ImageView button2 = (ImageView)findViewById(R.id.sendImages);
        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {

                switch(event.getAction()) {
                    case ACTION_UP:
                        button2.setImageResource(R.drawable.send);
                        Intent intent = new Intent();
                        intent.putExtra("DONE", true);
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    case ACTION_DOWN:
                        button2.setImageResource(R.drawable.send2);
                        break;
                }
                return true;
            }
        });

        //set dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.75),(int)(height*.6));

    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return ArrayListFragment.newInstance(position);
        }
    }

    public static class ArrayListFragment extends ListFragment {
        int mNum;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static ArrayListFragment newInstance(int num) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_pager_list, container, false);
            View tv = v.findViewById(R.id.text);
            String tvText = "Image " +(mNum+1)+"/"+ NUM_ITEMS;
            ((TextView)tv).setText(tvText);

            ViewGroup vg = (ViewGroup) v.findViewById(R.id.faragmentContainer);

            if(mNum == 0 && filePath[0] != null) {
                Bitmap img = BitmapFactory.decodeFile(filePath[0]);
                BitmapDrawable imgDrawable = new BitmapDrawable(img);
                ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).removeViewAt(1);

            } else if(mNum == 1 && filePath[1] != null) {

                Bitmap img = BitmapFactory.decodeFile(filePath[1]);
                BitmapDrawable imgDrawable = new BitmapDrawable(img);
                ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).removeViewAt(1);

            } else if(mNum == 2 && filePath[2] != null) {

                Bitmap img = BitmapFactory.decodeFile(filePath[2]);
                BitmapDrawable imgDrawable = new BitmapDrawable(img);
                ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).removeViewAt(1);

            } else if(mNum == 3 && filePath[3] != null) {

                Bitmap img = BitmapFactory.decodeFile(filePath[3]);
                BitmapDrawable imgDrawable = new BitmapDrawable(img);
                ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).removeViewAt(1);

            } else if(mNum == 4 && filePath[4] != null) {

                Bitmap img = BitmapFactory.decodeFile(filePath[4]);
                BitmapDrawable imgDrawable = new BitmapDrawable(img);
                ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).removeViewAt(1);

            }

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

    }
}

