package com.example.android.inventorymanager;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventorymanager.data.InventoryContract;

import java.text.DecimalFormat;

/**
 * Created by Ross on 13/11/16.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {

        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView productName = (TextView) view.findViewById(R.id.product_name);
        TextView productPrice = (TextView) view.findViewById(R.id.product_price);
        TextView productQuantity = (TextView) view.findViewById(R.id.current_quantity);

        int productNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);

        // Extract properties from cursor
        String inventoryProductName = cursor.getString(productNameColumnIndex);
        String inventoryProductPrice = cursor.getString(productPriceColumnIndex);
        int inventoryProductQuantity = cursor.getInt(productQuantityColumnIndex);

        Double price = Double.valueOf(inventoryProductPrice);
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedPrice = "£" + df.format(price);

        // Populate fields with extracted properties
        productName.setText(view.getResources().getString(R.string.inventory_item) + " " + inventoryProductName);
        productPrice.setText(view.getResources().getString(R.string.inventory_item_price) + " " + formattedPrice);
        productQuantity.setText(view.getResources().getString(R.string.inventory_item_quantity) + " " + Integer.toString(inventoryProductQuantity));

    }
}
