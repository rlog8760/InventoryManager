package com.example.android.inventorymanager;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
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
    private EditText mSupplierEmail;
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
        // all columns from the inventory table
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,         // Query the content URI for the current inventory
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
            int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            Uri imageUri = Uri.parse(image);
            String supplierEmail = cursor.getString(supplierColumnIndex);


            // Update the views on the screen with the values from the database
            mProductName.setText(name);
            mProductPrice.setText(price);
            mProductQuantity.setText(Integer.toString(quantity));
            mInventoryImageView.setImageURI(imageUri);
            imageViewUri = imageUri.toString();
            mSupplierEmail.setText(supplierEmail);


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
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mInventoryFieldsChanged) {
                    NavUtils.navigateUpFromSameTask(EditInventoryItemActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditInventoryItemActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing inventory item
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the inventory item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryUri
            // content URI already identifies the inventory item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory item
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveInventoryItem() {

        // Get the value that have been entered by the user in the fields in edit activity
        String productName = mProductName.getText().toString().trim();
        String productPrice = mProductPrice.getText().toString().trim();
        String productQuantity = mProductQuantity.getText().toString().trim();
        String productImage = imageViewUri;
        String supplierEmail = mSupplierEmail.getText().toString().trim();

        // Create a content values object where the column names are the keys and
        // the values from the fields in the editor activity are the keys
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, productName);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, productPrice);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, productQuantity);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE, productImage);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, supplierEmail);

        if (mCurrentInventoryUri == null) {

            Uri inventoryUri = null;

            try {
                inventoryUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
            } catch (IllegalArgumentException arg) {
                Log.v(LOG_TAG, "Exception has been thrown trying to insert to db - check data has been entered into all fields");
                arg.printStackTrace();
            }

            if (inventoryUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.saving_inventory_failed),
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(this, getString(R.string.check_fields_prompt), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.saving_inventory_succeeded),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            // Otherwise this is an EXISTING inventory item, so update the inventory item with content URI: mCurrentInventoryUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentInventoryUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = 0;
            try {
                rowsAffected = getContentResolver().update(mCurrentInventoryUri, contentValues, null, null);
            } catch (IllegalArgumentException iae) {
                Log.v(LOG_TAG, "Illegal argument exception was thrown. One of the fields entered in the form was invalid");
            }

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_inventory_failed),
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "One of the fields in the edit form was invalid", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.saving_inventory_succeeded),
                        Toast.LENGTH_SHORT).show();
            }

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
        mSupplierEmail = (EditText) findViewById(R.id.edit_supplier_email);

        // Now check if the user has started entering data on these fields
        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);


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

        //Button to place an order to the supplier for more stock
        Button orderButton = (Button) findViewById(R.id.make_order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                String[] emailAddresses = new String[]{mSupplierEmail.getText().toString()};
                intent.putExtra(Intent.EXTRA_EMAIL, emailAddresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, mProductName.getText().toString() + " Shipment Order");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        //Button to track a sale of the item
        Button saleButton = (Button) findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleItemSale();
            }
        });

        //Button to receive a shipment of stock for a particular item
        Button shipmentButton = (Button) findViewById(R.id.receive_shipment);
        shipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder shipmentAlert = new AlertDialog.Builder(EditInventoryItemActivity.this);
                final EditText shipmentNumber = new EditText(EditInventoryItemActivity.this);
                shipmentNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                shipmentNumber.setGravity(Gravity.CENTER_HORIZONTAL);
                shipmentAlert.setMessage("Please enter the amount of stock received in the shipment");
                shipmentAlert.setTitle("Shipment Received");
                shipmentAlert.setView(shipmentNumber);


                shipmentAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String shipmentQuantityValue = shipmentNumber.getText().toString();
                        if (!shipmentQuantityValue.trim().isEmpty()) {
                            int productQuantityValue = Integer.parseInt(mProductQuantity.getText().toString());
                            productQuantityValue += Integer.parseInt(shipmentQuantityValue);
                            mProductQuantity.setText(String.valueOf(productQuantityValue));
                        }
                    }
                });

                shipmentAlert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
                shipmentAlert.show();
            }
        });


    }

    private void handleItemSale() {
        int productQuantValue = 0;
        try {
            productQuantValue = Integer.parseInt(mProductQuantity.getText().toString());
        } catch (NumberFormatException nfe) {
            Log.v(LOG_TAG, "No value has been entered for the quantity so a sale cannot be tracked at this stage." +
                    "Please enter a value and then save to the db");
        }

        if (productQuantValue > 0) {
            productQuantValue -= 1;
            mProductQuantity.setText(String.valueOf(productQuantValue));
        } else {
            Toast.makeText(getApplicationContext(), "The sale cannot be fulfilled as there is no stock. Please order" +
                            " a shipment before trying again",
                    Toast.LENGTH_SHORT).show();
        }
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
            mInventoryFieldsChanged = true;
        }

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                mInventoryImageView.setImageURI(uri);
                imageViewUri = uri.toString();
                mInventoryFieldsChanged = true;
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
