package ceg4110.seefood;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

public class DisplayResult extends AppCompatActivity {

    int isFood;
    int confidence;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        Bundle receiveArgs = getIntent().getExtras();
        isFood = receiveArgs.getInt("isFood");
        confidence = receiveArgs.getInt("confidence");
        imagePath = receiveArgs.getString("imagePath");


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.75),(int)(height*.6));


        FrameLayout resultImageHolder = (FrameLayout)findViewById(R.id.resultImageHolder);

        Drawable imageDrawable = Drawable.createFromPath(imagePath);

        ImageView image = (ImageView)(((FrameLayout) findViewById(R.id.resultImageHolder)).getChildAt(0));
        TextView resultText = (TextView)findViewById(R.id.resultText);
        TextView resultConfidence = (TextView)findViewById(R.id.resultConfidence);

        image.setBackground(imageDrawable);
        resultText.setText(isFood == 1?"I see food":"Not food");
        resultConfidence.setText("H");


    }
}
