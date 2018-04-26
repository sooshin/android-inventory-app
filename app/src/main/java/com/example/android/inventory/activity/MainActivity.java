package com.example.android.inventory.activity;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.inventory.ProductCursorAdapter;
import com.example.android.inventory.EmptyRecyclerView;
import com.example.android.inventory.R;
import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the product data loader */
    private static final int PRODUCT_LOADER = 0;

    /** Adapter for the RecyclerView */
    private ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link RecyclerView} in the layout
        // Replaced RecyclerView with EmptyRecyclerView
        EmptyRecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);

        // Set the layoutManager on the {@link RecyclerView}
        recyclerView.setLayoutManager(layoutManager);

        // Find the empty layout and set it on the new recycler view
        RelativeLayout mEmptyLayout = findViewById(R.id.empty_view);
        recyclerView.setEmptyLayout(mEmptyLayout);

        // Setup a ProductCursorAdapter to create a card item for each row of product data in the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this);
        // Set the adapter on the {@link recyclerView}
        recyclerView.setAdapter(mCursorAdapter);

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purpose only.
     */
    private void insertDummyProduct() {
        // Create a ContentValues object where column names are the keys,
        // and product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "The Little Prince");
        values.put(ProductEntry.COLUMN_PRODUCT_AUTHOR, "Antoine de Saint-Exupéry");
        values.put(ProductEntry.COLUMN_PRODUCT_PUBLISHER, "Houghton Mifflin Harcourt");
        values.put(ProductEntry.COLUMN_PRODUCT_ISBN, "9780156012195");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 6.35);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 10);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Neho & Becky Supplier");
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "nehoandbecky@gmail.com");
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, "(200) 000-0000");

        // Insert a new row for "The Little Prince" into the provider using the ContentResolver.
        // Use the {@link ProductEntry.CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content UrI that will allow us to access The Little Prince's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI,
                null, null);
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
                insertDummyProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_AUTHOR,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                ProductEntry.CONTENT_URI,       // Provider content URI to query
                projection,                       // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,              // No selection arguments
                null);                // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Delete" button, so delete all products.
                deleteAllProducts();
            }
        });

        // The User clicked the "Cancel" button, so dismiss the dialog and continue displaying
        // the list of products. Any button will dismiss the popup dialog by default,
        // so the whole OnClickListener is null.
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Pop up ISBN edit text dialog for adding a product when a user press FAB button.
     * Prompt the user to select how to add a product. When a user wants to add a product,
     * the user can add a book by entering an ISBN in the edit text field or add it manually.
     */
    public void showIsbnDialog(View v) {
        // Create an AlertDialog.Builder and set the message.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.add_by_isbn_dialog_msg);

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_isbn, null));

        // Set click listeners for the positive and negative buttons on the dialog
        // Do not dismiss AlertDialog after clicking Positive button
        builder.setPositiveButton(R.string.enter_an_isbn, null);
        builder.setNegativeButton(R.string.add_manually, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Create a new intent to open the {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                // Start a new activity
                startActivity(intent);
            }
        });

        // Create the AlertDialog
        final AlertDialog alertDialog = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                // Find the isbn edit text
                final EditText isbnEditText = alertDialog.findViewById(R.id.edit_dialog_isbn);

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Read from isbn input field
                        String isbnStringDialog = isbnEditText.getText().toString().trim();
                        // If the length of the isbn String is 13, create a new intent.
                        // Otherwise, show toast message "Enter 13-digit ISBN".
                        if (isbnStringDialog.length() == 13) {
                            // Create a new intent to open the {@link IsbnActivity}
                            Intent intent = new Intent(MainActivity.this, IsbnActivity.class);
                            // Send the data
                            intent.putExtra(getString(R.string.isbn_in_a_dialog), isbnStringDialog);
                            // Start a new activity
                            startActivity(intent);
                            dialogInterface.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.enter_13_digit_isbn),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });

            }
        });

        // Show the AlertDialog
        alertDialog.show();
    }
}
