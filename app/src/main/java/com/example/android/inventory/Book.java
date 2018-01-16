package com.example.android.inventory;

/**
 * An {@link Book} object contains information related to a single book.
 */

public class Book {

    /** Title of the book */
    private String mTitle;

    /** Author of the book */
    private String mAuthor;

    /** ISBN, the International Standard Book Number, of the book */
    private String mIsbn;

    /** Publisher of the book */
    private String mPublisher;

    /**
     * Constructs a new{@link Book} object.
     * @param title is the title of the book.
     * @param author is the author of the book.
     * @param isbn is the ISBN of the book.
     * @param publisher is the publisher of the book.
     */
    public Book(String title, String author, String isbn, String publisher) {
        mTitle = title;
        mAuthor = author;
        mIsbn = isbn;
        mPublisher = publisher;
    }

    /**
     * Returns the title of the book.
     */
    public String getBookTitle() {
        return mTitle;
    }

    /**
     * Returns the author of the book.
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Returns the ISBN of the book.
     */
    public String getIsbn() {
        return mIsbn;
    }

    /**
     * Returns the publisher of the book.
     */
    public String getPublisher() {
        return mPublisher;
    }
}
