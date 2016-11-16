package com.example.android.inventorymanager;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventorymanager.data.InventoryContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ross on 08/11/16.
 */
public class EditInventoryItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * identified for the inventory loader
     **/
    private static final int EXISTING_INVENTORY_LOADER = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_IMAGE_REQUEST = 2;
    private static final String LOG_TAG = EditInventoryItemActivity.class.getSimpleName();
    /**
     * Setting up the content URI (null if it's a new inventory item)
     */

    private Uri mCurrentInventoryUri;
    private EditText mProductName;
    private EditText mProductPrice;
    private EditText mProductQuantity;
    private ImageView mInventoryImageView;
    private String imageViewUri;
    private String mCurrentPhotoPath;
    private Uri mCurrentPhotoUri;
    /**
     * boolean flag to keep track of whether or not the editor activity fields have been edited
     **/
    private boolean mInventoryFieldsChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryFieldsChanged = true;
            return false;
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        Log.v(LOG_TAG, "Inside onLoadFinished");
        Log.v(LOG_TAG, "Cursor size returned: " + cursor.getCount());

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of inventory attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            Uri imageUri = Uri.parse(image);


            // Update the views on the screen with the values from the database
            mProductName.setText(name);
            mProductPrice.setText(price);
            mProductQuantity.setText(Integer.toString(quantity));
            mInventoryImageView.setImageURI(imageUri);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText("");
        mProductPrice.setText("");
        mProductQuantity.setText("");
        // Select "Unknown" gender
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveInventoryItem();
                finish();
                return true;
        }

        return true;
    }

    private void saveInventoryItem() {

        // Get the value that have been entered by the user in the fields in edit activity
        String productName = mProductName.getText().toString().trim();
        String productPrice = mProductPrice.getText().toString().trim();
        String productQuantity = mProductQuantity.getText().toString().trim();
        String productImage = imageViewUri;

        // Create a content values object where the column names are the keys and
        // the values from the fields in the editor activity are the keys
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, productName);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, productPrice);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, productQuantity);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE, productImage);

        Uri inventoryUri = null;

        try {
            inventoryUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
        } catch (IllegalArgumentException arg) {
            Log.v(LOG_TAG, "Exception has been thrown trying to insert to db - check data has been entered into all fields");
            Toast.makeText(this, getString(R.string.check_fields_prompt), Toast.LENGTH_SHORT).show();
            arg.printStackTrace();
        }

        if (inventoryUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.saving_inventory_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.saving_inventory_succeeded),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_inventory);

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        if (mCurrentInventoryUri == null) {
            setTitle(R.string.add_inventory_item_activity_title);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_inventory_item_activity_title);
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        //Get references for all the views that we have in the editor activity
        mProductName = (EditText) findViewById(R.id.edit_product_name);
        mProductPrice = (EditText) findViewById(R.id.edit_product_price);
        mProductQuantity = (EditText) findViewById(R.id.edit_product_quantity);
        mInventoryImageView = (ImageView) findViewById(R.id.inventory_image);

        // Now check if the user has started entering data on these fields
        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);

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
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                mCurrentPhotoUri = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent selectPictureIntent = new Intent();
        selectPictureIntent.setType("image/*");
        selectPictureIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        selectPictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
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
            mInventoryImageView.setImageURI(mCurrentPhotoUri);
            imageViewUri = mCurrentPhotoUri.toString();
        }

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                mInventoryImageView.setImageURI(uri);
                imageViewUri = uri.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
