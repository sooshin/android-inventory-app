package com.example.android.inventory;

/**
 * Created by sj on 1/14/2018.
 */

public class Book {

    private String mTitle;

    private String mAuthor;

    private String mIsbn;

    private String mPublisher;

    public Book(String title, String author, String isbn, String publisher) {
        mTitle = title;
        mAuthor = author;
        mIsbn = isbn;
        mPublisher = publisher;
    }

    public String getBookTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getIsbn() {
        return mIsbn;
    }

    public String getPublisher() {
        return mPublisher;
    }
}
