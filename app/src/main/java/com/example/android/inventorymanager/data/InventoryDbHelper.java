package com.example.android.inventorymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ross on 10/11/16.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static byte[] convertBitmapToBytes(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //Insert the create SQL statement here once the table name and columns have been
        //defined in the contract class

        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " (" +
                InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME + " TEXT NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY + " INTEGER NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE + " TEXT NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE + " BLOB NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
