package com.ulmus.datastructures;


import java.text.DateFormat;
import java.text.Format;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is a class that represents a transaction that a user could make it is to support user
 * interaction with the Database and abstracts the necessary values needed to describe how much
 * a transaction is for who is is with and what it was for and when it happened well as the
 * information that associates it with the DB
 *
 * @author Jake Ullman
 */
public class Transaction implements Parcelable {

    public static final int TRANSACTION_FUFILLED = 1;
    public static final int TRANSACTION_OPEN = 0;
    /**
     * Necessary for Parcelable
     */
    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
    private double amount;
    private long date;
    private int open;
    private int individualID; //The database ID number that assoicates a person
    private String note;  //can be null
    private int transactionID; //The database ID number that is connected to this transaction

    /**
     * Used to load data from the database
     *
     * @param transactionID The ID of this transaction in the database
     * @param individualID  The ID of the Individual in the database associated with this transaction
     * @param amount        The amount that the transaction is for
     * @param note          The optional note that describes what this is for
     * @param date          The date that this transaction occurred
     */
    public Transaction(int transactionID, int individualID, double amount, String note, long date, int open) {
        //create a complete conversation
        this.transactionID = transactionID;
        this.amount = amount;
        this.individualID = individualID;
        this.date = date;
        this.note = note;
        this.open = open;
    }

    /**
     * Used to create a new transaction
     *
     * @param individualID The ID of the Individual in the database associated with this transaction
     * @param amount       The amount that the transaction is for
     * @param note         The optional note that describes what this is for
     * @param date         The date that this transaction occurred
     */
    public Transaction(int individualID, double amount, String note, long date, int open) {
        //create a complete conversation
        this.amount = amount;
        this.individualID = individualID;
        this.date = date;
        this.note = note;
        this.open = open;

    }

    /**
     * Creates a transaction from a Parcel allowing a transaction to be passed in an intent
     *
     * @param in
     */
    public Transaction(Parcel in) {
        //create a conversation from a parcel from passed trough an intent
        this.amount = in.readDouble();
        this.individualID = in.readInt();
        this.date = in.readLong();

    }

    private static String formatedDate1Line(long time) {
        Format formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        return formatter.format(time);
    }

    /**
     * Parces the a dollar amount and returns a double must be in the format of #-#.#-# or $#-#.#-# or #-# or $#-# where #-# is any number of digits
     *
     * @param input
     * @return
     */
    public static double parseDollarAmount(String input) {
        double amount = 0;
        String temp = "";
        boolean dUsed = false;
        boolean number = false;

        for (char c : input.toCharArray())
            if (Character.isDigit(c) || c == '.') {
                temp = temp + c;
                if (Character.isDigit(c))
                    number = true;
                else if (c == '.' && !dUsed)
                    dUsed = true;
                else if (c == '.' && dUsed)
                    return 0.0;
            }

        if (number)
            amount = Double.parseDouble(temp);
        else
            return 0.0;
        return amount;
    }

    /**
     * Sets if a transaction is currently open
     *
     * @param open
     */
    public void setOpen(boolean open) {
        if (open)
            this.open = TRANSACTION_OPEN;
        else
            this.open = TRANSACTION_FUFILLED;
    }

    /**
     * @return The ID of the individual that this transaction is associated with
     */
    public int getIndividualId() {
        return individualID;
    }

    /**
     * sets the individualId of the transaction (The individual with whom this transaction is associated with)
     *
     * @param newId the new ID
     */
    public void setIndividualId(int newId) {
        individualID = newId;
    }

    /**
     * @return The ID of the transaction in the database
     */
    public int getTransactionId() {
        return transactionID;
    }

    /**
     * Sets the transactionID of this transaction this is the Id of the transaction in the database
     *
     * @param newId
     */
    public void setTransactionId(int newId) {
        transactionID = newId;
    }

    /**
     * @return Returns the amount that this transaction is for a negative amount means a transaction where the user is in debt to someone else
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount that this transaction is for a negative amount means a transaction where the user is in debt to someone else
     *
     * @param amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return Returns if the transaction is open in the format for the database
     */
    public int getRawOpenTransaction() {
        return open;
    }

    /**
     * @return Returns if the transaction is open as boolean
     */
    public boolean isOpenTransaction() {
        return open == TRANSACTION_OPEN;
    }

    /**
     * @return Returns the date as a formatted user readable string
     */
    public String getStringDate() {
        return formatedDate1Line(date);
    }

    /**
     * @return Returns the note that is an optional descriptor about what this transaction is for
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets the note that is an optional descriptor of what the transaction is for
     *
     * @param note
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return Returns if this transaction is a debt
     */
    public boolean isDebt() {
        return amount < 0;
    }

    /**
     * @return Returns the date as a long in the same format as System.currentTimeMillis()
     */

    public long getDate() {
        return date;
    }

    /**
     * sets the date of the transaction
     *
     * @param date
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * @return Returns the amount in a user readable format ex $100.00 returns the absolute value of the
     * amount so be make sure the user knows which direction the transaction is for
     */
    public String getAmountFormatted() {
        return String.format(Locale.US, "$%.2f", Math.abs(amount));
    }

    @Override
    /**
     * Necessary for Parcelable
     */
    public int describeContents() {
        // For children classed return diffrent value
        return 0;
    }

    @Override
    /**
     * Necessary for Parcelable
     */
    public void writeToParcel(Parcel dest, int arg1) {
        //writes to parcel for passing in an intent
        dest.writeDouble(amount);
        dest.writeInt(individualID);
        dest.writeLong(date);
    }

    public String toString() {
        return "Transaction #" + transactionID + " For: " + individualID + " " + getStringDate() + " $" + amount + ", " + note + ", open: " + open;
    }

    public boolean equals(Object o){
        if(o == this)
            return true;
        if(o == null || !(o instanceof Transaction))
            return false;
        Transaction other = (Transaction) o;

        if(open != other.open)
            return false;
        if(date != other.date)
            return false;
        if(individualID != other.individualID)
            return false;
        if(transactionID != other.transactionID)
            return false;
        if(amount != other.amount)
            return false;
        if(!note.equals(other.note))
            return false;

        return true;
    }

}
