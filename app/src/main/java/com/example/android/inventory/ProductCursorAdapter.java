package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.activity.DetailActivity;
import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * {@link ProductCursorAdapter} is an adapter for a recycler view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create card items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends RecyclerView.Adapter<ProductCursorAdapter.ProductViewHolder> {

    // Declare variables for the Cursor that holds product data
    private Cursor mCursor;
    private Context mContext;

    /**
     * Constructor for the ProductCursorAdapter that initializes the Context.
     *
     * @param context Context of the app
     */
    public ProductCursorAdapter(Context context) {
        mContext = context;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new ProductViewHolder that holds the view for each product
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.card_item, parent, false);
        return new ProductViewHolder(v);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param viewHolder The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder viewHolder, int position) {
        final ProductViewHolder holder = viewHolder;
        mCursor.moveToPosition(position); // Get to the right location in the cursor
        // Set data
        holder.setData(mCursor);
        // Indices for _id
        final long id = mCursor.getLong(mCursor.getColumnIndex(ProductEntry._ID));
        // Set an OnClickListener to open a DetailActivity
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to go to {@link DetailActivity}
                Intent intent = new Intent(mContext, DetailActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventory/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link DetailActivity} to display the data for the current product.
                mContext.startActivity(intent);
            }
        });

        // Find the columns of product quantity
        int quantityColumnIndex = mCursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        // Read the product quantity from the Cursor for the current product
        int quantity = mCursor.getInt(quantityColumnIndex);

        // If the quantity is more than 0, set the text of a sale button to display 'sell'.
        // Otherwise, set the text of a sale button to display 'sold out'.
        if (quantity > 0) {
            holder.saleButton.setText(mContext.getString(R.string.sell));
        } else{
            holder.saleButton.setText(mContext.getString(R.string.sold_out));
        }

        //Set OnClickListener on the sale button. We can decrement the available quantity by one.
        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Read from text fields
                String quantityString = holder.quantityTextView.getText().toString().trim();

                // Parse the string into an Integer value.
                int quantity = Integer.parseInt(quantityString);
                // If the quantity is more than 0, decrement the quantity by 1.
                // If quantity is 0, show a toast message.
                if (quantity > 0) {
                    // Set the text of a sale button to display 'sell'.
                    holder.saleButton.setText(mContext.getString(R.string.sell));
                    quantity = quantity - 1;
                } else if (quantity == 0) {
                    // Set the text of a sale button to display 'sold out'
                    holder.saleButton.setText(mContext.getString(R.string.sold_out));
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
    }

    /**
     * Returns the number of items to display
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor cursor) {
        // Check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == cursor) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = cursor; // new cursor value assigned

        // Check if this is a valid cursor, then update the cursor
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    // Inner class for creating ViewHolders
    class ProductViewHolder extends RecyclerView.ViewHolder {

        private TextView productNameTextView;
        private TextView authorTextView;
        private TextView priceTextView;
        private TextView quantityTextView;
        private Button saleButton;
        private CardView cardView;

        /**
         * Constructor for the ProductViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        ProductViewHolder(View itemView) {
            super(itemView);

            // Find individual views that we want to modify in the card item layout
            productNameTextView = itemView.findViewById(R.id.product_name_card);
            authorTextView = itemView.findViewById(R.id.product_author_card);
            priceTextView =itemView.findViewById(R.id.product_price_card);
            quantityTextView =itemView.findViewById(R.id.product_quantity_card);
            saleButton = itemView.findViewById(R.id.product_sale_button_card);
            cardView = itemView.findViewById(R.id.card_view);
        }

        /**
         * Find the columns of product attributes that we're interested in, then
         * read the product attributes from the Cursor for the current product.
         * Update the TextViews with the attributes for the current product
         * @param c The cursor from which to get the data.
         */
        public void setData(Cursor c) {
            productNameTextView.setText(c.getString(c.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
            authorTextView.setText(c.getString(c.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AUTHOR)));
            priceTextView.setText(String.valueOf(c.getDouble(c.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE))));
            quantityTextView.setText(String.valueOf(c.getInt(c.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY))));
        }
    }
}
