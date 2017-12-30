package com.example.android.inventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;
import com.example.android.inventory.data.ProductDbHelper;

public class MainActivity extends AppCompatActivity {

    /** Database helper that will provide us access to the database */
    private ProductDbHelper mDbHelper;

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
            }
        });

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new ProductDbHelper(this);
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
        Cursor cursor = queryProduct();

        TextView displayView = findViewById(R.id.text_view_product);

        try {
            // Create a header in the Text View that looks like this:
            // The products table contains <number of rows in Cursor> products.
            // _id - product_name -author - publisher - isbn - price - quantity - supplier_name - supplier_email - supplier_phone_number

            displayView.setText("The products table contains " + cursor.getCount() + " products.\n\n");
            displayView.append(ProductEntry._ID + " - " +
                    ProductEntry.COLUMN_PRODUCT_NAME + " - " +
                    ProductEntry.COLUMN_PRODUCT_AUTHOR + " - " +
                    ProductEntry.COLUMN_PRODUCT_PUBLISHER + " - " +
                    ProductEntry.COLUMN_PRODUCT_ISBN + " - " +
                    ProductEntry.COLUMN_PRICE + " - " +
                    ProductEntry.COLUMN_QUANTITY + " - " +
                    ProductEntry.COLUMN_SUPPLIER_NAME + " - " +
                    ProductEntry.COLUMN_SUPPLIER_EMAIL + " - " +
                    ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productAuthorColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AUTHOR);
            int productPublisherColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PUBLISHER);
            int productIsbnColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ISBN);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Check if the Cursor has items before calling the method moveToNext().
            if (cursor.moveToFirst()) {
                // Iterate through all the returned rows in the cursor
                do {
                    // Use that index to extract the String or int value of the word
                    // at the current row the cursor is on.
                    int currentID = cursor.getInt(idColumnIndex);
                    String currentProductName = cursor.getString(productNameColumnIndex);
                    String currentProductAuthor = cursor.getString(productAuthorColumnIndex);
                    String currentProductPublisher = cursor.getString(productPublisherColumnIndex);
                    String currentProductIsbn = cursor.getString(productIsbnColumnIndex);
                    int currentPrice = cursor.getInt(priceColumnIndex);
                    int currentQuantity = cursor.getInt(quantityColumnIndex);
                    String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                    String currentSupplierEmail = cursor.getString(supplierEmailColumnIndex);
                    String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

                    // Display the values from each column of the current row in the cursor in the TextView
                    displayView.append(("\n" + currentID + " - " +
                            currentProductName + " - " +
                            currentProductAuthor + " - " +
                            currentProductPublisher + " - " +
                            currentProductIsbn + " - " +
                            currentPrice + " - " +
                            currentQuantity + " - " +
                            currentSupplierName + " - " +
                            currentSupplierEmail + " - " +
                            currentSupplierPhone));
                } while (cursor.moveToNext());
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    /**
     * Read product data and return a cursor object.
     */
    private Cursor queryProduct() {
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

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

        Cursor cursor = db.query(
                ProductEntry.TABLE_NAME,
                projection,
                null,
                null,
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
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

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

        // Insert a new row for "The Little Prince" in the database, returning the ID of that new row.
        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);

        Log.v(LOG_TAG, "New row ID " + newRowId);
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
