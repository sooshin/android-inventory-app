package com.example.android.inventory;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    /** EditText field to enter the product name */
    private EditText mProductNameEditText;

    /** EditText field to enter the author */
    private EditText mAuthorEditText;

    /** EditText field to enter the publisher */
    private EditText mPublisherEditText;

    /** EditText field to enter the ISBN */
    private EditText mIsbnEditText;

    /** EditText field to enter the price of the product */
    private EditText mPriceEditText;

    /** EditText field to enter the quantity of the product */
    private EditText mQuantityEditText;

    /** EditText field to enter supplier's name */
    private  EditText mSupplierNameEditText;

    /** EditText field to enter supplier's email */
    private EditText mSupplierEmailEditText;

    /** EditText field to enter supplier's phone number */
    private EditText mSupplierPhoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = findViewById(R.id.edit_product_name);
        mAuthorEditText = findViewById(R.id.edit_product_author);
        mPublisherEditText = findViewById(R.id.edit_product_publisher);
        mIsbnEditText = findViewById(R.id.edit_product_isbn);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierEmailEditText = findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void insertProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductNameEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String publisherString = mPublisherEditText.getText().toString().trim();
        String isbnString = mIsbnEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_AUTHOR, authorString);
        values.put(ProductEntry.COLUMN_PRODUCT_PUBLISHER, publisherString);
        values.put(ProductEntry.COLUMN_PRODUCT_ISBN, isbnString);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL,supplierEmailString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);

        // Insert a new product into the provider, returning the content URI for the new product.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                insertProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
