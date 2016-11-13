package com.example.android.inventorymanager.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ross on 10/11/16.
 */
public class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventorymanager";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY_ITEMS = "inventory";

    private InventoryContract() {

    }

    public static final class InventoryEntry implements BaseColumns {


        /**
         * The content URI to access the pet data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY_ITEMS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_ITEMS;

        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_INVENTORY_ITEM_NAME = "name";
        public final static String COLUMN_INVENTORY_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_INVENTORY_ITEM_PRICE = "price";
        public final static String COLUMN_INVENTORY_ITEM_IMAGE = "image";


    }
}
