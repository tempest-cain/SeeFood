package ceg4110.seefood;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);


        Bundle receiveArgs = getIntent().getExtras();

        int food = receiveArgs.getInt("food");
        int notFood = receiveArgs.getInt("not");
        int total = receiveArgs.getInt("total");

        int percentFood = (int) ((food / (double) total) * 100);


        FrameLayout overallStatsLayout = (FrameLayout) findViewById(R.id.overallStatsLayout);
        LinearLayout detailsLayout = (LinearLayout) findViewById(R.id.statsDetails);


        ImageView percentageIndicator = (ImageView)overallStatsLayout.getChildAt(0);

            percentageIndicator.setImageResource(getPercentageDrawable(percentFood));

            String percentage = ""+percentFood;
            ((TextView) overallStatsLayout.getChildAt(1)).setText(percentage);

            ((ImageView) ((LinearLayout) detailsLayout.getChildAt(0)).getChildAt(0)).setImageResource(R.drawable.stats_food);
            ((ImageView) ((LinearLayout) detailsLayout.getChildAt(1)).getChildAt(0)).setImageResource(R.drawable.stats_not_food);
            ((ImageView) ((LinearLayout) detailsLayout.getChildAt(2)).getChildAt(0)).setImageResource(R.drawable.stats_total);

            String foodCount = ""+food;
            String notFoodCount = ""+notFood;
            String totalCount = ""+total;


            ((TextView) (((LinearLayout) detailsLayout.getChildAt(0)).getChildAt(1))).setText(foodCount);
            ((TextView) (((LinearLayout) detailsLayout.getChildAt(1)).getChildAt(1))).setText(notFoodCount);
            ((TextView) (((LinearLayout) detailsLayout.getChildAt(2)).getChildAt(1))).setText(totalCount);

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
        }
        return R.drawable.confidence_above_10;
    }

}
