package com.example.android.inventorymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ross on 10/11/16.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER + " TEXT NOT NULL, +" +
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_IMAGE + "BLOB);";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
