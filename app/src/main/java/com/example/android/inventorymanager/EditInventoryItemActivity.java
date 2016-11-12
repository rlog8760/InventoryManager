package com.example.android.inventorymanager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by Ross on 08/11/16.
 */
public class EditInventoryItemActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_IMAGE_REQUEST = 2;
    private static final String LOG_TAG = EditInventoryItemActivity.class.getSimpleName();

    private ImageView mInventoryImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_inventory);

        //Button to take a photo for the database entry
        Button takePhoto = (Button) findViewById(R.id.product_photo_btn);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        //Button to select a photo for the database entry from android gallery
        Button uploadPhoto = (Button) findViewById(R.id.product_photo_file_btn);
        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchSelectPictureIntent();
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent selectPictureIntent = new Intent();
        selectPictureIntent.setType("image/*");
        selectPictureIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectPictureIntent, "Select Picture"),
                SELECT_IMAGE_REQUEST);

    }

    //Image is returning a size now we need

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_inventory_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mInventoryImageView = (ImageView) findViewById(R.id.inventory_image);
            mInventoryImageView.setImageBitmap(imageBitmap);
        }

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mInventoryImageView = (ImageView) findViewById(R.id.inventory_image);
                mInventoryImageView.setImageBitmap(bitmap);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
}
