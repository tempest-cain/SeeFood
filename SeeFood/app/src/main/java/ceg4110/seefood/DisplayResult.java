package ceg4110.seefood;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
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

public class DisplayResult extends FragmentActivity {
        static int NUM_ITEMS;

        MyAdapter mAdapter;

        ViewPager mPager;

        static int isFood[];
        static int confidence[];
        static String filePath[];

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_display_result);

            //receive arguments from previous activity
            Bundle receiveArgs = getIntent().getExtras();
            isFood = receiveArgs.getIntArray("isFood");
            confidence = receiveArgs.getIntArray("confidence");
            filePath = receiveArgs.getStringArray("imagePath");
            NUM_ITEMS = receiveArgs.getInt("selectionCount");

            mAdapter = new MyAdapter(getSupportFragmentManager());

            mPager = (ViewPager)findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);

            // Watch for button clicks.
            final ImageView button = (ImageView)findViewById(R.id.goto_first);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch (View v, MotionEvent event) {

                    switch(event.getAction()) {
                        case ACTION_UP:
                            button.setImageResource(R.drawable.goto_first);
                            mPager.setCurrentItem(0);
                    }
                    return true;
                }
            });
            final ImageView button2 = (ImageView)findViewById(R.id.goto_last);
            button2.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch (View v, MotionEvent event) {

                    switch(event.getAction()) {
                        case ACTION_UP:
                            button2.setImageResource(R.drawable.goto_last);
                            mPager.setCurrentItem(NUM_ITEMS-1);
                    }
                    return true;
                }
            });

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

                    String percentage = ""+confidence[0];
                    Bitmap img = BitmapFactory.decodeFile(filePath[0]);
                    BitmapDrawable imgDrawable = new BitmapDrawable(img);

                    ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                    ((ImageView)((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(1)).setBackgroundResource(isFood[0] == 1 ? R.drawable.tick : R.drawable.x);

                    ((ImageView)vg.findViewById(R.id.foodOrNot)).setBackgroundResource(isFood[0] == 1 ? R.drawable.is_food_1 : R.drawable.not_food_1);
                    ((ImageView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(0)).setBackgroundResource(getPercentageDrawable(confidence[0]));
                    ((TextView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(1)).setText(percentage);


                } else if(mNum == 1 && filePath[1] != null) {

                    String percentage = ""+confidence[1];
                    Bitmap img = BitmapFactory.decodeFile(filePath[1]);
                    BitmapDrawable imgDrawable = new BitmapDrawable(img);

                    ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                    ((ImageView)((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(1)).setBackgroundResource(isFood[1] == 1 ? R.drawable.tick : R.drawable.x);

                    ((ImageView)vg.findViewById(R.id.foodOrNot)).setBackgroundResource(isFood[1] == 1 ? R.drawable.is_food_1 : R.drawable.not_food_1);
                    ((ImageView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(0)).setBackgroundResource(getPercentageDrawable(confidence[1]));
                    ((TextView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(1)).setText(percentage);

                } else if(mNum == 2 && filePath[2] != null) {

                    String percentage = ""+confidence[2];
                    Bitmap img = BitmapFactory.decodeFile(filePath[2]);
                    BitmapDrawable imgDrawable = new BitmapDrawable(img);

                    ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                    ((ImageView)((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(1)).setBackgroundResource(isFood[2] == 1 ? R.drawable.tick : R.drawable.x);

                    ((ImageView)vg.findViewById(R.id.foodOrNot)).setBackgroundResource(isFood[2] == 1 ? R.drawable.is_food_1 : R.drawable.not_food_1);
                    ((ImageView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(0)).setBackgroundResource(getPercentageDrawable(confidence[2]));
                    ((TextView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(1)).setText(percentage);

                } else if(mNum == 3 && filePath[3] != null) {

                    String percentage = ""+confidence[3];
                    Bitmap img = BitmapFactory.decodeFile(filePath[3]);
                    BitmapDrawable imgDrawable = new BitmapDrawable(img);

                    ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                    ((ImageView)((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(1)).setBackgroundResource(isFood[3] == 1 ? R.drawable.tick : R.drawable.x);

                    ((ImageView)vg.findViewById(R.id.foodOrNot)).setBackgroundResource(isFood[3] == 1 ? R.drawable.is_food_1 : R.drawable.not_food_1);
                    ((ImageView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(0)).setBackgroundResource(getPercentageDrawable(confidence[3]));
                    ((TextView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(1)).setText(percentage);

                } else if(mNum == 4 && filePath[4] != null) {

                    String percentage = ""+confidence[4];
                    Bitmap img = BitmapFactory.decodeFile(filePath[4]);
                    BitmapDrawable imgDrawable = new BitmapDrawable(img);

                    ((ImageView) ((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(0)).setBackground(imgDrawable);
                    ((ImageView)((FrameLayout) vg.findViewById(R.id.resultImageHolder)).getChildAt(1)).setBackgroundResource(isFood[4] == 1 ? R.drawable.tick : R.drawable.x);

                    ((ImageView)vg.findViewById(R.id.foodOrNot)).setBackgroundResource(isFood[4] == 1 ? R.drawable.is_food_1 : R.drawable.not_food_1);
                    ((ImageView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(0)).setBackgroundResource(getPercentageDrawable(confidence[4]));
                    ((TextView)((FrameLayout)vg.findViewById(R.id.resultConfidence)).getChildAt(1)).setText(percentage);

                }


                return v;
            }

            /**
             * Returns the right drawable resource.
             */
            public int getPercentageDrawable(int percentFood) {

                if(percentFood >= 95) {
                    return R.drawable.confidence_above_95;
                } else if (percentFood >= 90) {
                    return R.drawable.confidence_above_90;
                } else if (percentFood >= 80) {
                    return R.drawable.confidence_above_80;
                } else if (percentFood >= 70) {
                    return R.drawable.confidence_above_70;
                } else if (percentFood >= 60) {
                    return R.drawable.confidence_above_60;
                } else if (percentFood >= 50) {
                    return R.drawable.confidence_above_50;
                } else if (percentFood >= 40) {
                    return R.drawable.confidence_above_40;
                } else if (percentFood >= 30) {
                    return R.drawable.confidence_above_30;
                } else if (percentFood >= 20) {
                    return R.drawable.confidence_above_20;
                } else if (percentFood >= 10) {
                    return R.drawable.confidence_above_10;
                } else {
                    return R.drawable.confidence_above_10;
                }
            }

            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
            }
        }
    }

