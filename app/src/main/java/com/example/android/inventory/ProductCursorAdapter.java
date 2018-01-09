package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    private Context mContext;

    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c        The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     *                 correct position.
     * @param parent The parent to which the view is attached
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the product name for the current product can be set on the
     * product name TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     *                 correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView productNameTextView = view.findViewById(R.id.product_name);
        TextView authorTextView = view.findViewById(R.id.product_author);
        TextView priceTextView = view.findViewById(R.id.product_price);
        final TextView quantityTextView = view.findViewById(R.id.product_quantity);
        ImageView imageView = view.findViewById(R.id.product_image);
        final Button saleButton = view.findViewById(R.id.product_sale_button);

        // Find the columns of product attributes that we're interested in
        int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int authorColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AUTHOR);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(productNameColumnIndex);
        String author = cursor.getString(authorColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);
        String imageString = cursor.getString(imageColumnIndex);
        final long id = cursor.getLong(idColumnIndex);

        // Set image in the list view
        if (!TextUtils.isEmpty(imageString)) {
            imageView.setImageBitmap(getBitmapFromUri(Uri.parse(imageString), view));
        }

        // If the quantity is more than 0, set the text of a sale button to display 'sell'.
        // Otherwise, set the text of a sale button to display 'sold out'.
        if (quantity > 0) {
            saleButton.setText(mContext.getString(R.string.sell));
        } else{
            saleButton.setText(mContext.getString(R.string.sold_out));
        }

        //Set OnClickListener on the sale button. We can decrement the available quantity by one.
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Read from text fields
                String quantityString = quantityTextView.getText().toString().trim();

                // Parse the string into an Integer value.
                int quantity = Integer.parseInt(quantityString);
                // If the quantity is more than 0, decrement the quantity by 1.
                // If quantity is 0, show a toast message.
                if (quantity > 0) {
                    // Set the text of a sale button to display 'sell'.
                    saleButton.setText(mContext.getString(R.string.sell));
                    quantity = quantity - 1;
                } else if (quantity == 0) {
                    // Set the text of a sale button to display 'sold out'
                    saleButton.setText(mContext.getString(R.string.sold_out));
                    Toast.makeText(view.getContext(),
                            view.getContext().getString(R.string.detail_update_zero_quantity),
                            Toast.LENGTH_SHORT).show();
                }

                // Create a ContentValues object where column names are the keys,
                // and product attributes from the textView fields are the values.
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

                // Update the product with content URI: mCurrentProductUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentProductUri will already identify the correct row in the database that
                // we want to modify.
                Uri mCurrentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                int rowsAffected = view.getContext().getContentResolver().update(mCurrentProductUri, values,
                        null, null);
            }
        });

        // Update the TextViews with the attributes for the current product
        productNameTextView.setText(productName);
        authorTextView.setText(author);
        priceTextView.setText(String.valueOf(price));
        quantityTextView.setText(String.valueOf(quantity));
    }

    /**
     * Returns a Bitmap object from the URI which is the location of the image.
     */
    public Bitmap getBitmapFromUri(Uri uri , View view) {
        ImageView mImageView = view.findViewById(R.id.product_image);

        // Check the Uri is null or empty
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream inputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);

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

            inputStream = mContext.getContentResolver().openInputStream(uri);
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
}
