package com.example.android.inventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */

public final class ProductContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventory/products/ is a valid path for
     * looking at product data. content://com.example.android.inventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCT = "products";


    /**
     *  Inner class that defines constant values for the products database table.
     *  Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        /** Name of database table for products */
        public static final String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /** Name of the product. Type: TEXT */
        public static final String COLUMN_PRODUCT_NAME = "product_name";

        /** Price of the product. Type: INTEGER */
        public static final String COLUMN_PRICE = "price";

        /** Quantity of the product. Type: INTEGER */
        public static final String COLUMN_QUANTITY = "quantity";

        /** Image of the product.  */
        public static final String COLUMN_PRODUCT_IMAGE = "product_image";

        /** Supplier name. Type: TEXT */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /** Supplier email. Type: TEXT */
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /** Supplier phone number. Type: TEXT */
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";

        /** Author of the product. Type: TEXT */
        public static final String COLUMN_PRODUCT_AUTHOR = "author";

        /** Publisher of the product. Type: Text */
        public static final String COLUMN_PRODUCT_PUBLISHER = "publisher";

        /** ISBN of the product. Type: Text */
        public static final String COLUMN_PRODUCT_ISBN = "isbn";
    }
}
