package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
    }


    /**
     * When the activity starts again, the list will refresh with the new product in the database.
     */
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the products database.
     */
    private void displayDatabaseInfo() {
        // Read product data and return a cursor object.
        Cursor cursor = queryProduct();

        // Find the ListView which will be populated with the product data
        ListView productListView = findViewById(R.id.list);
        // Setup a CursorAdapter to create a list item for each row of product data in the Cursor.
        ProductCursorAdapter cursorAdapter = new ProductCursorAdapter(this, cursor);
        // Attach the cursor adapter to the ListView
        productListView.setAdapter(cursorAdapter);
    }

    /**
     * Read product data and return a cursor object.
     */
    private Cursor queryProduct() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_AUTHOR,
                ProductEntry.COLUMN_PRODUCT_PUBLISHER,
                ProductEntry.COLUMN_PRODUCT_ISBN,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link ProductEntry.CONTENT_URI) to access the product data.
        Cursor cursor = getContentResolver().query(
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
        return cursor;
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purpose only.
     */
    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "The Little Prince");
        values.put(ProductEntry.COLUMN_PRODUCT_AUTHOR, "Antoine de Saint-Exupéry");
        values.put(ProductEntry.COLUMN_PRODUCT_PUBLISHER, "Reynal & Hitchcock ");
        values.put(ProductEntry.COLUMN_PRODUCT_ISBN, "1234567890123");
        values.put(ProductEntry.COLUMN_PRICE, 8);
        values.put(ProductEntry.COLUMN_QUANTITY, 10);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, "");
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Neho & Becky Supplier");
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "nehoandbecky@gmail.com");
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "(200) 222-2345");

        // Insert a new row for "The Little Prince" into the provider using the ContentResolver.
        // Use the {@link ProductEntry.CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content UrI that will allow us to access The Little Prince's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
