package com.example.android.inventory;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Allows user to create a new pet or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity {

    /** EditText field to enter the product name */
    private EditText mProductNameEditText;

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
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierEmailEditText = findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
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
                // Do nothing for now
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
