package com.github.yinyee.locator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class BarcodeReader extends AppCompatActivity {

    private BarcodeDetector detector;
    private TextView textView;
    private Button button;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        // link to display elements
        textView = (TextView) findViewById(R.id.txtContent);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);

        // instantiate barcode detector
        detector = new BarcodeDetector.Builder(getApplicationContext()).build();

        if(!detector.isOperational()){
            textView.setText("Could not set up the detector!");
            return;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.e("BarcodeReader", "Clicked on the button");
            }
        });

        Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.kitty);
        imageView.setImageBitmap(myBitmap);

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        // Always picks the first barcode in the array
        Barcode thisCode = barcodes.valueAt(0);
        textView.setText(thisCode.rawValue + " -url " + thisCode.url);
    }
}