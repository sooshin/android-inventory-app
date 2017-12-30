package com.example.android.inventory.data;

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
     *  Inner class that defines constant values for the products database table.
     *  Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

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
