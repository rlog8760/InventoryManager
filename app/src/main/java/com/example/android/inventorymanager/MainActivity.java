package com.example.android.inventorymanager;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventorymanager.data.InventoryContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditInventoryItemActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory item data
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditInventoryItemActivity.class);

                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                Log.v("MainActivity", "Current inventory ur: " + currentInventoryUri);

                intent.setData(currentInventoryUri);

                startActivity(intent);
            }
        });


        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY
        };

        return new CursorLoader(this,
                InventoryContract.InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
