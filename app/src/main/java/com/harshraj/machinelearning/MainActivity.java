package com.harshraj.machinelearning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.util.List;

public class MainActivity extends BaseActivity {
    private ImageView mImageView;
    private TextView mTextView;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);
        mTextView = findViewById(R.id.text_view);
        button=findViewById(R.id.btn_txtrecogn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,TextRecognition.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                case RC_STORAGE_PERMS2:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        bitmap = MyHelper.resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        bitmap = MyHelper.resizeImage(imageFile, path, mImageView);
                    }
                    if (bitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(bitmap);
                        barcodeDetector(bitmap);
                    }
                    break;
                case RC_TAKE_PICTURE:
                    bitmap = MyHelper.resizeImage(imageFile, imageFile.getPath(), mImageView);
                    if (bitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(bitmap);
                        barcodeDetector(bitmap);
                    }
                    break;
            }
        }
    }

    private void barcodeDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        //FirebaseVisionBarcodeDetectorOptions.Builder builder = new FirebaseVisionBarcodeDetectorOptions.Builder();
        //FirebaseVisionBarcodeDetectorOptions options = builder.setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE).build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                mTextView.setText(getInfoFromBarcode(firebaseVisionBarcodes));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mTextView.setText(R.string.error_detect);
            }
        });
    }

    private String getInfoFromBarcode(List<FirebaseVisionBarcode> barcodes) {
        StringBuilder result = new StringBuilder();
        for (FirebaseVisionBarcode barcode : barcodes) {
            //int valueType = barcode.getValueType();
            result.append(barcode.getRawValue() + "\n");
        }
        if ("".equals(result.toString())) {
            return getString(R.string.error_detect);
        } else {
            return result.toString();
        }
    }
}
