package com.example.android.inventory.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.R;
import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Tag for the log messages */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private static final int PICK_IMAGE_REQUEST = 1;

    /** URI for the product image */
    private Uri mImageUri;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product name */
    @BindView(R.id.edit_product_name) EditText mProductNameEditText;

    /** EditText field to enter the author */
    @BindView(R.id.edit_product_author) EditText mAuthorEditText;

    /** EditText field to enter the publisher */
    @BindView(R.id.edit_product_publisher) EditText mPublisherEditText;

    /** EditText field to enter the ISBN */
    @BindView(R.id.edit_product_isbn) EditText mIsbnEditText;

    /** EditText field to enter the price of the product */
    @BindView(R.id.edit_product_price) EditText mPriceEditText;

    /** EditText field to enter the quantity of the product */
    @BindView(R.id.edit_product_quantity) EditText mQuantityEditText;

    /** EditText field to enter supplier's name */
    @BindView(R.id.edit_supplier_name) EditText mSupplierNameEditText;

    /** EditText field to enter supplier's email */
    @BindView(R.id.edit_supplier_email) EditText mSupplierEmailEditText;

    /** EditText field to enter supplier's phone number */
    @BindView(R.id.edit_supplier_phone) EditText mSupplierPhoneEditText;

    /** ImageView for the product image */
    @BindView(R.id.edit_product_image) ImageView mImageView;

    @BindView(R.id.edit_add_image_button) Button addImageButton;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /** TextInputLayout to display the floating label on EditText */
    @BindView(R.id.layout_product_name) TextInputLayout layoutProductName;
    @BindView(R.id.layout_product_author) TextInputLayout layoutProductAuthor;
    @BindView (R.id.layout_product_isbn) TextInputLayout layoutProductIsbn;
    @BindView (R.id.layout_product_price) TextInputLayout layoutProductPrice;
    @BindView (R.id.layout_product_quantity) TextInputLayout layoutProductQuantity;
    @BindView (R.id.layout_supplier_name) TextInputLayout layoutSupplierName;
    @BindView (R.id.layout_supplier_phone) TextInputLayout layoutSupplierPhone;

    /** The boolean isValidate value is false if this is supposed to be a new product and
     * all the fields in the editor are blank. Otherwise, the isValidate value is true.
     */
    private boolean isValidate = true;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int GALLERY_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Bind the view using ButterKnife
        ButterKnife.bind(this);

        // Receive the data from the IsbnActivity. Check if an extra with "title" (or "author" or
        // "isbn" or "publisher") was passed in the intent.
        if( getIntent().hasExtra(getString(R.string.title)) || getIntent().hasExtra(getString(R.string.author)) ||
                getIntent().hasExtra(getString(R.string.isbn)) || getIntent().hasExtra(getString(R.string.publisher)) ) {

            // Get the data from the IsbnActivity and set the data in the EditText field.
            Intent intent = getIntent();
            String title = intent.getStringExtra(getString(R.string.title));
            mProductNameEditText.setText(title);
            String author = intent.getStringExtra(getString(R.string.author));
            mAuthorEditText.setText(author);
            String isbn = intent.getStringExtra(getString(R.string.isbn));
            mIsbnEditText.setText(isbn);
            String publisher = intent.getStringExtra(getString(R.string.publisher));
            mPublisherEditText.setText(publisher);
        }

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(R.string.editor_activity_title_new_product);

            mImageView.setImageResource(R.drawable.ic_image_black_24dp);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(R.string.editor_activity_title_edit_product);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (EasyPermissions.hasPermissions(EditorActivity.this, galleryPermissions)) {
                    pickImageFromGallery();
                } else {
                    EasyPermissions.requestPermissions(EditorActivity.this, "Access for storage",
                            GALLERY_PERMISSION_REQUEST_CODE, galleryPermissions);
                }
            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPublisherEditText.setOnTouchListener(mTouchListener);
        mIsbnEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        addImageButton.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
    }

    private void  pickImageFromGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            // Allow the user to select files
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            // Allow the user to select and return existing documents.
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            // Only files that can be opened are displayed
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        // Display image files of all format.
        intent.setType(getString(R.string.image_all_format));
        // Start a file picker activity with an intent to pick a file and receive a result back.
        // To receive a result, call startActivityForResult(). The result will be the URI of the file picked.
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result for the "pick a file" intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        // Check which request we're responding to and make sure the request was successful
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The location of the image is delivered to us in the URI data type.
            if (data != null) {
                mImageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                // Display the image on the ImageView
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
    }

    /**
     * Returns a Bitmap object from the URI which is the location of the image.
     */
    public Bitmap getBitmapFromUri(Uri uri) {
        // Check the Uri is null or empty
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream inputStream = null;
        try {
            inputStream = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            inputStream = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to open the image file.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem with loading a file.");
            }
        }
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductNameEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String publisherString = mPublisherEditText.getText().toString().trim();
        String isbnString = mIsbnEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
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
        // If the price is not provided by the user, don't try to parse the string into a
        // double value. Use 0.0 by default.
        double price = 0.0;
        if(!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        // If the quantity is not provided by the user, don't try to parse the string into an
        // Integer value. Use 0 by default.
        int quantity = 0;
        if(!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Check if the mImageUri is not null and then convert it to string
        if (mImageUri != null) {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());
            Log.e(LOG_TAG, "Uri saveProduct() : " + mImageUri.toString());
        }

        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL,supplierEmailString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
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
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values,
                    null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows are affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the menu can be updated
     * (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Check user input is validated. No null values are accepted for the product name,
                // author, isbn, price, quantity, supplier name, supplier phone.
                if (isValidateInput()) {
                    // If a user input is valid, save the product to database and navigate up to parent activity
                    // which is the {@link MainActivity}.
                    saveProduct();
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                } else if (!isValidate) {
                    // If isValidate value is false, navigate up to parent activity.
                    // Since no fields were modified, we can navigate up to parent activity without creating a new product.
                    // No need to create ContentValues and no need to do any ContentProvider operations.
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            NavUtils.navigateUpFromSameTask(EditorActivity.this);
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_AUTHOR,
                ProductEntry.COLUMN_PRODUCT_PUBLISHER,
                ProductEntry.COLUMN_PRODUCT_ISBN,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,             // Query the content URI for the current product
                projection,                        // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int authorColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AUTHOR);
            int publisherColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PUBLISHER);
            int isbnColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ISBN);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String publisher = cursor.getString(publisherColumnIndex);
            String isbn = cursor.getString(isbnColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            final String imageString = cursor.getString(imageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(title);
            mAuthorEditText.setText(author);
            mPublisherEditText.setText(publisher);
            mIsbnEditText.setText(isbn);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));

            if(imageString != null) {

                // Attach a ViewTreeObserver listener to ImageView.
                ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        Log.e(LOG_TAG, "mImageUri onLoadFinished: " + mImageUri);
                        mImageUri = Uri.parse(imageString);
                        mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
                    }
                });
            } else {
                mImageView.setImageResource(R.drawable.ic_image_black_24dp);
            }

            mSupplierNameEditText.setText(supplierName);
            mSupplierEmailEditText.setText(supplierEmail);
            mSupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mAuthorEditText.setText("");
        mPublisherEditText.setText("");
        mIsbnEditText.setText("");
        mPriceEditText.setText(String.valueOf(""));
        mQuantityEditText.setText(String.valueOf(""));
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                        the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });

        // The User clicked the "Cancel" button, so dismiss the dialog and continue editing the product.
        // Any button will dismiss the popup dialog by default, so the whole OnClickListener is null.
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null,
                    null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Navigate to parent activity
        NavUtils.navigateUpFromSameTask(EditorActivity.this);
    }

    /**
     * Check user input is validated. No null values are accepted for the product name, author,
     * isbn, price, quantity, supplier name, supplier phone.
     */
    private boolean isValidateInput() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductNameEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String publisherString = mPublisherEditText.getText().toString().trim();
        String isbnString = mIsbnEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // The boolean isValidate value is false if this is supposed to be a new product
        // and all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(publisherString) && TextUtils.isEmpty(isbnString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierEmailString) &&
                TextUtils.isEmpty(supplierPhoneString) &&
                mImageUri == null) {
            isValidate = false;
            return false;
        }

        // If the boolean isValidate value is true (all the fields are not blank) and user input
        // is not validated, display a red error message below the edit text and
        // make a Toast message that prompts the user to input the correct information
        if (isValidate && TextUtils.isEmpty(productNameString)) {
            layoutProductName.setError(getString(R.string.error_product_name));
            Toast.makeText(this, getString(R.string.empty_product_name),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutProductName.setErrorEnabled(false);
        }

        if (isValidate && TextUtils.isEmpty(authorString)) {
            layoutProductAuthor.setError(getString(R.string.error_product_author));
            Toast.makeText(this, getString(R.string.empty_product_author),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutProductAuthor.setErrorEnabled(false);
        }

        if (isValidate && TextUtils.isEmpty(isbnString)) {
            layoutProductIsbn.setError(getString(R.string.error_product_isbn));
            // hide the keyboard to allow a user to see isbn edit text field
            hideKeyboard();
            Toast.makeText(this, getString(R.string.empty_product_isbn),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutProductIsbn.setErrorEnabled(false);
        }

        if (isValidate && TextUtils.isEmpty(priceString)) {
            layoutProductPrice.setError(getString(R.string.error_product_price));
            Toast.makeText(this, getString(R.string.empty_product_price),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutProductPrice.setErrorEnabled(false);
        }

        if (isValidate && TextUtils.isEmpty(quantityString)) {
            layoutProductQuantity.setError(getString(R.string.error_product_quantity));
            Toast.makeText(this, getString(R.string.empty_product_quantity),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutProductQuantity.setErrorEnabled(false);
        }

        if (isValidate && TextUtils.isEmpty(supplierNameString)) {
            layoutSupplierName.setError(getString(R.string.error_supplier_name));
            // hide the keyboard to allow a user to see the supplier name edit text field
            hideKeyboard();
            Toast.makeText(this, getString(R.string.empty_supplier_name),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutSupplierName.setErrorEnabled(false);
        }

        if (isValidate && TextUtils.isEmpty(supplierPhoneString)) {
            layoutSupplierPhone.setError(getString(R.string.error_supplier_phone));
            // hide the keyboard to allow a user to see the supplier phone edit text field
            hideKeyboard();
            Toast.makeText(this, getString(R.string.empty_supplier_phone),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            layoutSupplierPhone.setErrorEnabled(false);
        }
        return true;
    }

    /**
     * When checking user input is validated, hide the keyboard to allow a user to see the blank field.
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
