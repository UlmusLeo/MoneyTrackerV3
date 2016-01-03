package com.ulmus.datastructures;


import java.text.DateFormat;
import java.text.Format;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * This object represents the sum of all the transactions that the user has made with someone else
 * this is used to hold the name of the person who the user is making transactions with as well as
 * the total amount and date of the latest transaction it is a user friendly abstraction of the
 *  database representation that holds the the information about all transactions that the user has made
 *
 * @author Jake Ullman
 */
public class Individual implements Parcelable, Comparable<Individual>{
    private String name;
    private double amount;
    private int individualID; //The database ID number that is associated with this individual
    private long date;
    /**
     * Used to load the date from the database
     * @param indivID //The database ID number associated with the individual
     * @param name the name of the individual
     * @param amount the amount owed or lent to the individual
     * @param date the date of the latest transaction
     */
    public Individual(int indivID, String name, double amount, long date) {
        //create a complete conversation
        this.name = name;

        this.amount = amount;
        this.individualID = indivID;
        this.date = date;
    }
    /**
     * Used to create a new Individual when a user adds someone new.
     * Creates a new individual with the name <code>name</code> with the latest
     * transaction date at the time created
     * @param name
     */
    public Individual(String name){
        this.name = name;
        this.amount = 0;
        this.date = System.currentTimeMillis();
    }
    /**
     * Necessary for Parcelable
     * @param in
     */
    public Individual(Parcel in) {
        //create a conversation from a parcel from passed trough an intent
        this.name = in.readString();
        this.amount = in.readDouble();
        this.individualID = in.readInt();
        this.date = in.readLong();

    }

    /**
     * Should be called when a user has made a new transaction with this individual it adds
     * the amount in the transaction to the total
     * @param transaction the transaction that is added
     */
    public void addTransaction(Transaction transaction) {
        this.amount += transaction.getAmount();
        this.date = transaction.getDate();
    }
    /**
     * Should be called when a user deletes a transaction associated with this individual
     * @param transaction the transaction that is to be deleted
     */
    public void removeTransaction(Transaction transaction) {
        this.amount -= transaction.getAmount();
    }
    /**
     * Sets the name of this indivdual
     * @param name
     */
    public void setName(String name) {
        this.name = name;

    }
    /**
     * Sets the latest transaction date of this individual
     * @param date
     */
    public void setLatestDate(long date) {
        this.date = date;
    }

    /**
     * Sets the database ID of this individual
     * @param newId
     */
    public void setId(int newId) {
        individualID = newId;
    }
    /**
     * sets the sum amount of money owed or lent to this individual
     * @param amount
     */
    public void setTotalAmount(double amount) {
        this.amount = amount;
    }
    /**
     * @return The database ID associated with this individual
     */
    public int getId() {
        return individualID;
    }
    /**
     *
     * @return The sum amount of money owed or lent to this individual
     */
    public double getTotalAmount() {
        return amount;
    }
    /**
     *
     * @return a formatted string of the the sum amount of money owed or lent to this individual is the
     * absolute value of the amount so it can not be used soley to represent which direction money is owed
     */
    public String getTotalAmountFormatted() {
        return String.format(Locale.US,"$%.2f", Math.abs(amount));
    }

    /**
     *
     * @return the name of the individual
     */
    public String getName() {
        //returns the name of the person or persons whom this transaction is with
        if (name != null && !name.trim().equals(""))
            return name;
        else
            return "Unknown";
    }
    /**
     *
     * @return a String representation of the latest date that a transaction occurred that the user can understand
     */
    public String getStringDate() {
        return formatedDate1Line(date);
    }


    private static String formatedDate1Line(long time) {
        Format formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT);
        return formatter.format(time);
    }

    /**
     *
     * @return a long value that represents the latest date that a transaction has occured in the same form as 	System.currentTimeMillis()
     */
    public long getLatestDate() {
        return date;
    }

    @Override
    public int describeContents() {
        // For children classes return diffrent value
        return 0;
    }

    @Override
    /**
     * Necessary for Parcelable
     */
    public void writeToParcel(Parcel dest, int arg1) {
        //writes to parcel for passing in an intent
        dest.writeString(name);
        dest.writeDouble(amount);
        dest.writeInt(individualID);
        dest.writeLong(date);
    }
    /**
     * Necessary for Parcelable
     */
    public static final Parcelable.Creator<Individual> CREATOR = new Parcelable.Creator<Individual>() {
        public Individual createFromParcel(Parcel in) {
            return new Individual(in);
        }

        public Individual[] newArray(int size) {
            return new Individual[size];
        }
    };

    public static final int SORT_TYPE_LATEST_DATE = 0;
    private static int SortType = SORT_TYPE_LATEST_DATE;

    /**
     * Sets the sort type. Not thread safe.
     * @param type
     */
    public static void setSortType(int type) {
        SortType = type;
    }
    @Override
	/*
	 * Sorts individual based on the set sort type not thread safe
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    public int compareTo(Individual another) {
        switch(SortType) {
            case SORT_TYPE_LATEST_DATE:
                if (another.date > date)
                    return 1;
                else if (another.date < date)
                    return -1;
                else
                    return 0;
            case 1:

        }
        return 0;
    }

}
