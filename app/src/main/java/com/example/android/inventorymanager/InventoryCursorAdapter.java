package com.example.android.inventorymanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorymanager.data.InventoryContract;

import java.text.DecimalFormat;

/**
 * Created by Ross on 13/11/16.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();

    public InventoryCursorAdapter(Context context, Cursor c) {

        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productName = (TextView) view.findViewById(R.id.product_name);
        TextView productPrice = (TextView) view.findViewById(R.id.product_price);
        final TextView productQuantity = (TextView) view.findViewById(R.id.current_quantity);
        Button saleButton = (Button) view.findViewById(R.id.list_item_sale_button);

        int productNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);
        int productImageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE);
        int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
        int rowId = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);

        // Extract properties from cursor
        final String inventoryProductName = cursor.getString(productNameColumnIndex);
        final String inventoryProductPrice = cursor.getString(productPriceColumnIndex);
        final int inventoryProductQuantity = cursor.getInt(productQuantityColumnIndex);
        final String inventoryProductImage = cursor.getString(productImageColumnIndex);
        final String inventorySupplierEmail = cursor.getString(supplierEmailColumnIndex);
        final int inventoryRowId = cursor.getInt(rowId);

        Double price = Double.valueOf(inventoryProductPrice);
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedPrice = "£" + df.format(price);

        // Populate fields with extracted properties
        productName.setText(view.getResources().getString(R.string.inventory_item) + " " + inventoryProductName);
        productPrice.setText(view.getResources().getString(R.string.inventory_item_price) + " " + formattedPrice);
        productQuantity.setText(view.getResources().getString(R.string.inventory_item_quantity) + " " + Integer.toString(inventoryProductQuantity));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inventoryProductQuantity > 0) {
                    int mProductQuant = 0;
                    mProductQuant = inventoryProductQuantity - 1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, inventoryProductName);
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, inventoryProductPrice);
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, mProductQuant);
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE, inventoryProductImage);
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, inventorySupplierEmail);

                    Uri mCurrentInventoryUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, inventoryRowId);
                    int rowsAffected = context.getContentResolver().update(mCurrentInventoryUri, contentValues, null, null);
                    context.getContentResolver().notifyChange(InventoryContract.InventoryEntry.CONTENT_URI, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(context.getApplicationContext(), "Quantity not updated in the db", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context.getApplicationContext(), "Quantity updated in db successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



    }
}
