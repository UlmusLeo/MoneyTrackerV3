package com.ulmus.database;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ulmus.datastructures.Individual;
import com.ulmus.datastructures.Transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * This is a SQLite database Adapter class that bridges the Java - SQLite transition 
 * most tasks to edit the database should go through the DBEditor  especially updating
 * and writing. This should only be used directly where potentially large portions of 
 * the database are being read into memory potentially slowing the user interface experience
 * @author Jake Ullman
 *
 */
public class MoneyTrackerDBAdapter {
    //Keys associated with transactions only
    public static final String KEY_TRANSACTION_ID = "_id";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_NOTE = "note";
    public static final String KEY_DATE = "date";
    public static final String KEY_OPEN = "open"; //if the transaction is currently open


    //keys associated with individuals
    public static final String KEY_INDIVIDUAL_ID = "individual_id";
    public static final String KEY_INDIVIDUAL_NAME = "contact";
    public static final String KEY_TOTAL_AMOUNT = "total_amount";
    public static final String KEY_LAST_DATE = "last_date"; //the date of the last update

    private static final String TAG = "MoneyTrackerDBAdapter";

    //Names for the database and the tables
    private static final String DATABASE_NAME = "moneytracker.db";
    private static final String DATABASE_TABLE_TRANSACTIONS = "transactions"; //All the individual transactions
    private static final String DATABASE_TABLE_INDIVIDUALS = "individuals"; //So showing what people owe can be loaded quickly into the listview

    private static final int DATABASE_VERSION = 2;
    //SQLITE code to create a table of individuals
    private static final String CREATE_TABLE_INDIVIDUALS = "create table "
            + DATABASE_TABLE_INDIVIDUALS + " (" + KEY_INDIVIDUAL_ID
            + " integer primary key autoincrement, " + KEY_INDIVIDUAL_NAME
            + " text not null, " + KEY_TOTAL_AMOUNT + " real not null, "
            + KEY_LAST_DATE + " integer not null)";
    //SQLITE code to create a table of transactions
    private static final String CREATE_TABLE_TRANSACTIONS = "create table "
            + DATABASE_TABLE_TRANSACTIONS + " (" + KEY_TRANSACTION_ID
            + " integer primary key autoincrement, " + KEY_AMOUNT
            + " real not null," + KEY_INDIVIDUAL_ID + " integer, " + KEY_NOTE
            + " text, " + KEY_DATE + " integer not null, " + KEY_OPEN + " integer, " + "FOREIGN KEY ("
            + KEY_INDIVIDUAL_ID + ") REFERENCES " + DATABASE_TABLE_INDIVIDUALS
            + "(" + KEY_INDIVIDUAL_ID + ")); ";

    private Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    private static boolean isBlankValue = false;

    public MoneyTrackerDBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    /*
     * sets up, updates and creates the database
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            isBlankValue = true;
            db.execSQL(CREATE_TABLE_INDIVIDUALS);

            db.execSQL(CREATE_TABLE_TRANSACTIONS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion == 1 && newVersion == 2){

                ArrayList<Individual> holdIndiv = new ArrayList<Individual>(20);
                ArrayList<Transaction> holdTrans = new ArrayList<Transaction>(200);
                TreeMap<Integer,Integer> indivMap = new TreeMap<Integer, Integer>();

                Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will attempt to save all old data");

                Cursor indivudualCur = db.query(DATABASE_TABLE_INDIVIDUALS, new String[] {
                        KEY_INDIVIDUAL_ID, KEY_INDIVIDUAL_NAME, KEY_TOTAL_AMOUNT,
                        KEY_LAST_DATE }, null, null, null, null, null);
                while(indivudualCur.moveToNext()){
                    holdIndiv.add(new Individual(indivudualCur.getInt(0),indivudualCur.getString(1), indivudualCur.getDouble(2), indivudualCur.getLong(3)));

                    indivMap.put(holdIndiv.get(holdIndiv.size()-1).getId(), holdIndiv.size());
                }
                indivudualCur.close();

                Cursor transCur = db.query(DATABASE_TABLE_TRANSACTIONS, new String[] {
                        KEY_TRANSACTION_ID, KEY_INDIVIDUAL_ID, KEY_AMOUNT, KEY_NOTE,
                        KEY_DATE }, null, null, null, null, null);
                while(transCur.moveToNext()){
                    holdTrans.add(new Transaction(transCur.getInt(0), transCur.getInt(1), transCur.getDouble(2), transCur.getString(3), transCur.getLong(4), Transaction.TRANSACTION_OPEN));
                }

                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TRANSACTIONS);
                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_INDIVIDUALS);
                onCreate(db);

                for(Individual i:holdIndiv){
                    ContentValues initialValuesConv = new ContentValues();
                    initialValuesConv.put(KEY_INDIVIDUAL_NAME, i.getName());
                    initialValuesConv.put(KEY_TOTAL_AMOUNT, i.getTotalAmount());
                    initialValuesConv.put(KEY_LAST_DATE, i.getLatestDate());
                    // insert and add set the thread_id
                    db.insert(DATABASE_TABLE_INDIVIDUALS, null,
                            initialValuesConv);
                }
                for(Transaction t:holdTrans){
                    ContentValues initialValues = new ContentValues();
                    initialValues.put(KEY_INDIVIDUAL_ID, indivMap.get(t.getIndividualId()));
                    initialValues.put(KEY_AMOUNT, t.getAmount());
                    initialValues.put(KEY_NOTE, t.getNote());
                    initialValues.put(KEY_DATE, t.getDate());
                    initialValues.put(KEY_OPEN, t.getRawOpenTransaction());


                    db.insert(DATABASE_TABLE_TRANSACTIONS,
                            null, initialValues);
                }
            }

        }

    }

    public boolean isBlank() {
        return isBlankValue;
    }

    /**
     * opens the database
     * @return
     * @throws SQLException
     */
    public MoneyTrackerDBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    /**
     * closes the database
     */
    public void close() {
        DBHelper.close();
    }
    /**
     * Adds a new individual into the database and updates the individuals ID to what it was added to
     * @param indiv
     * @return the ID that the Individual has in the database
     */
    public int insertIndividual(Individual indiv) {
        ContentValues initialValuesConv = new ContentValues();
        initialValuesConv.put(KEY_INDIVIDUAL_NAME, indiv.getName());
        initialValuesConv.put(KEY_TOTAL_AMOUNT, indiv.getTotalAmount());
        initialValuesConv.put(KEY_LAST_DATE, indiv.getLatestDate());
        // insert and add set the thread_id
        indiv.setId((int) db.insert(DATABASE_TABLE_INDIVIDUALS, null,
                initialValuesConv));
        return indiv.getId() ;
    }
    /**
     * Inserts the transaction into the database and sets its ID to the ID that it is now at in the database
     * an insert also updates the individual in the the database that the transaction is with
     * @param trans the transaction to insert
     * @return the ID that it was inserted with
     * @throws IllegalArgumentException if there is no individual that is associated with this ID to check this
     *  it only checks that the individualID >= 0 which could cause unexpected results if the individual is not found
     */
    public int insertTransaction(Transaction trans) {

        if (trans.getIndividualId() >= 0) {
            ContentValues args = new ContentValues();
            // initialValuesConv.put(KEY_CONTACT_NAME, number);
            // args.put(KEY_TOTAL_MESSAGES,(KEY_TOTAL_MESSAGES+ " + 1"));
            args.put(KEY_LAST_DATE, trans.getDate());
            db.update(DATABASE_TABLE_INDIVIDUALS, args, KEY_INDIVIDUAL_ID
                    + "=?", new String[] { trans.getIndividualId() + "" });
            db.execSQL("UPDATE " + DATABASE_TABLE_INDIVIDUALS + " SET "
                    + KEY_TOTAL_AMOUNT + " = " + KEY_TOTAL_AMOUNT + " + "
                    + trans.getAmount() + " WHERE " + KEY_INDIVIDUAL_ID
                    + " = ?", new String[] { trans.getIndividualId() + "" });
            // LOW use something that is not raw SQLite to increment
        } else
            throw new IllegalArgumentException(
                    "Must have an associated Individual");

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_INDIVIDUAL_ID, trans.getIndividualId());
        initialValues.put(KEY_AMOUNT, trans.getAmount());
        initialValues.put(KEY_NOTE, trans.getNote());
        initialValues.put(KEY_DATE, trans.getDate());
        initialValues.put(KEY_OPEN, trans.getRawOpenTransaction());


        trans.setTransactionId((int) db.insert(DATABASE_TABLE_TRANSACTIONS,
                null, initialValues));
        return trans.getTransactionId();
    }

    /**
     * @return  a cursor containing all the transactions in the database
     * the cursor data values are as follows
     * 0 = TransactionID (int)
     * 1 = IndividualID (int)
     * 2 = transaction amount (double)
     * 3 = note (String)
     * 4 = date (long)
     */
    public Cursor getAllTransactions() {
        return db.query(DATABASE_TABLE_TRANSACTIONS, new String[] {
                KEY_TRANSACTION_ID, KEY_INDIVIDUAL_ID, KEY_AMOUNT, KEY_NOTE,
                KEY_DATE, KEY_OPEN }, null, null, null, null, null);
    }

    /**
     * @param individual_ids a list of the id's that are to be retrieved
     * @return a cursor containing all the transactions associated with the list of individualIDs given sorted by date with oldest first.
     * the cursor data values are as follows
     * 0 = TransactionID (int)
     * 1 = IndividualID (int)
     * 2 = transaction amount (double)
     * 3 = note (String)
     * 4 = date (long)
     */
    public Cursor getIndividualsTransactions(String... individual_ids) {
        if (individual_ids.length > 0) {
            String fields = "";
            for (int i = 0; i < individual_ids.length; i++) {
                fields += "?,";
            }
            fields = fields.substring(0, fields.length() - 1);
            return db.query(DATABASE_TABLE_TRANSACTIONS, new String[] {
                    KEY_TRANSACTION_ID, KEY_INDIVIDUAL_ID, KEY_AMOUNT,
                    KEY_NOTE, KEY_DATE, KEY_OPEN }, KEY_INDIVIDUAL_ID + " IN (" + fields
                    + ")", individual_ids, null, null, KEY_DATE);
        } else
            return null;
    }

    /**
     * Returns a single transaction
     * @param transactionId
     * @return A Transaction associated with the given transactionId
     * @throws SQLException
     */
    public Transaction getSingleTransaction(int transactionId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE_TRANSACTIONS,
                new String[] { KEY_TRANSACTION_ID, KEY_INDIVIDUAL_ID,
                        KEY_AMOUNT, KEY_NOTE, KEY_DATE, KEY_OPEN }, KEY_TRANSACTION_ID
                        + "=" + transactionId, null, null, null, null, null);

        Transaction returnTransaction = null;

        if (mCursor != null) {
            mCursor.moveToFirst();
            returnTransaction = new Transaction(mCursor.getInt(0), mCursor.getInt(1), mCursor.getDouble(2), mCursor.getString(3), mCursor.getLong(4), mCursor.getInt(5));
        }
        mCursor.close();
        return returnTransaction;
    }
    /**
     * @return a cursor containing all the individuals in the database
     * the cursor data values are as follows
     * 0 = IndividualID (int)
     * 1 = individual name (String)
     * 2 = total Amount (double)
     * 3 = latest date (long)
     */
    public Cursor getAllIndividuals() {
        return db.query(DATABASE_TABLE_INDIVIDUALS, new String[] {
                KEY_INDIVIDUAL_ID, KEY_INDIVIDUAL_NAME, KEY_TOTAL_AMOUNT,
                KEY_LAST_DATE }, null, null, null, null, null);

    }
    /**
     *
     * @param individual_ids
     * @return A cursor containing the individuals with the specified ids
     * the cursor data values are as follows
     * 0 = IndividualID (int)
     * 1 = individual name (String)
     * 2 = total Amount (double)
     * 3 = latest date (long)
     */
    public Cursor getIndividuals(String... individual_ids) {
        String fields = "";
        for (int i = 0; i < individual_ids.length; i++) {
            fields += "?,";
        }
        fields = fields.substring(0, fields.length() - 1);
        return db.query(DATABASE_TABLE_INDIVIDUALS, new String[] {
                        KEY_INDIVIDUAL_ID, KEY_INDIVIDUAL_NAME, KEY_TOTAL_AMOUNT,
                        KEY_LAST_DATE }, KEY_INDIVIDUAL_ID + " IN (" + fields + ")",
                individual_ids, null, null, null);
    }

    /**
     * Updates the individual specified with the id with the name specified
     * @param individual_id the individual to update
     * @param name the new name for the individual
     * @return if the operation was successful
     */
    public boolean updateIndividualName(int individual_id, String name) {
        ContentValues args = new ContentValues();
        args.put(KEY_INDIVIDUAL_NAME, name);
        return db.update(DATABASE_TABLE_INDIVIDUALS, args, KEY_INDIVIDUAL_ID
                + "=?", new String[] { individual_id + "" }) > 0;

    }

    /**
     * Updates the individual with the id that matches indiv
     *  with the information contained in indiv
     * @param indiv the individual to update
     * @return if the operation was successful
     */
    public boolean updateIndividual(Individual indiv) {

        ContentValues args = new ContentValues();
        args.put(KEY_INDIVIDUAL_NAME, indiv.getName());
        args.put(KEY_LAST_DATE, indiv.getLatestDate());
        args.put(KEY_TOTAL_AMOUNT,indiv.getTotalAmount());

        return db.update(DATABASE_TABLE_INDIVIDUALS, args, KEY_INDIVIDUAL_ID
                + "=?", new String[] { indiv.getId() + "" }) > 0;

    }

    /**
     * Updates the transaction with the same TransactionID as t and the individual i
     * with the current content of t and the current total amount of i
     * @param t The transaction that contains the updated information except for the amount
     * @return if the operation was successful
     */
    public boolean updateTransaction(Transaction t,Individual i){

        ContentValues args = new ContentValues();
        args.put(KEY_DATE, t.getDate());
        args.put(KEY_AMOUNT, t.getAmount());
        args.put(KEY_NOTE, t.getNote());
        args.put(KEY_OPEN, t.getRawOpenTransaction());


        db.execSQL("UPDATE " + DATABASE_TABLE_INDIVIDUALS + " SET "
                        + KEY_TOTAL_AMOUNT + " = " + i.getTotalAmount()
                        +" WHERE "+ KEY_INDIVIDUAL_ID + " = ?",
                new String[] { t.getIndividualId() + "" });


        return db.update(DATABASE_TABLE_TRANSACTIONS, args, KEY_TRANSACTION_ID
                + "=?", new String[] { t.getTransactionId() + "" }) > 0;
    }

    /**
     * Deletes a transaction
     * @param t the transaction to be deleted
     * @return if the operation was successful
     */
    public boolean deleteTransaction(Transaction t) {
        // TODO read just the amount after deleting a transaction;
        db.execSQL("UPDATE " + DATABASE_TABLE_INDIVIDUALS + " SET "
                        + KEY_TOTAL_AMOUNT + " = " + KEY_TOTAL_AMOUNT + " - " + t.getAmount() +" WHERE "
                        + KEY_INDIVIDUAL_ID + " = ?",
                new String[] { t.getIndividualId() + "" });

        return db.delete(DATABASE_TABLE_TRANSACTIONS, KEY_TRANSACTION_ID + "="
                + t.getTransactionId(), null) > 0;
    }

    /**
     * Deletes a individual and all transactions associated with it
     * @param individual_id
     * @return the number of transactions deleted
     */
    public int deleteIndividual(long individual_id) {
        db.delete(DATABASE_TABLE_INDIVIDUALS, KEY_INDIVIDUAL_ID + "="
                + individual_id, null);
        return db.delete(DATABASE_TABLE_TRANSACTIONS, KEY_INDIVIDUAL_ID + "="
                + individual_id, null);
    }

    /**
     * Deletes the entire database
     * @return
     */
    public int deleteAll() {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_INDIVIDUALS);
        db.execSQL(CREATE_TABLE_INDIVIDUALS);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        isBlankValue = true;
        return -1;
    }
}
