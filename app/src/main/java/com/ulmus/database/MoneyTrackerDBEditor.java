package com.ulmus.database;

import java.util.ArrayList;

import com.ulmus.datastructures.Individual;
import com.ulmus.datastructures.Transaction;

import android.content.Context;
import android.database.Cursor;
/**
 * This is a DBEditor class for quick and short queries to the database. 
 * These methods should not be used to manipulate large portions of the database 
 * Use of this class is ideal for user input defined actions for the database
 * @author Jake
 *
 */
public class MoneyTrackerDBEditor {

    /**
     * @param context the context to open the database
     * @param indiv the individual whos transactions are to be retrived
     * @return an ArrayList of the specified individuals Transactions
     */
    public static ArrayList<Transaction> getIndividualsTransactions(Context context, Individual indiv){
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);

        ArrayList<Transaction> transactions= new ArrayList<Transaction>();

        moneyDB.open();

        Cursor transactionCur = moneyDB.getIndividualsTransactions(indiv.getId()+"");

        while(transactionCur.moveToNext()){
            transactions.add(new Transaction(transactionCur.getInt(0), transactionCur.getInt(1), transactionCur.getDouble(2), transactionCur.getString(3), transactionCur.getLong(4),transactionCur.getInt(5)));
        }

        transactionCur.close();

        moneyDB.close();

        return transactions;

    }
    /**
     *
     * @param context
     * @param id
     * @return the individual with the specified ID
     */
    public static Individual getIndividual(Context context, int id) {
        //TODO modify DB adapter to return a  single individual?
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        moneyDB.open();
        Cursor indivudualCur =
                moneyDB.getIndividuals(id+"");
        Individual temp = null;
        if(indivudualCur.moveToNext()){
            temp = new Individual(indivudualCur.getInt(0),indivudualCur.getString(1), indivudualCur.getDouble(2), indivudualCur.getLong(3));
        }

        moneyDB.close();
        return temp;
    }
    /**
     * Updates the individuals name in the database and the sets <code> indiv </code> name to <code> name </code>
     * @param context
     * @param indiv the individual to update
     * @param name the new name
     */
    public static void setIndividualName(Context context, Individual indiv, String name) {
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        moneyDB.open();
        moneyDB.updateIndividualName(indiv.getId(), name);
        moneyDB.close();

        indiv.setName(name);
    }
    /**
     * Edits the Transaction in the DB will update the transaction with the same id with the
     * current values of transaction and the current amount of the individual indiv with the
     * current total amount in indiv
     *
     * @param context the context value
     * @param transaction The transaction to be changes
     * @param indiv the individual to whom this transaction belongs (must be updated to work correctly)
     */
    public static void setTransaction(Context context,Transaction transaction, Individual indiv){
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        moneyDB.open();

        moneyDB.updateTransaction(transaction,indiv);

        moneyDB.close();
    }

    /**
     * Adds the Transaction to the DB and sets the Transactions ID to its ID in the database
     * @param context the context value
     * @param transaction The transaction to be added
     * @return THe transaction ID of the transaction that was Just added, is already in the transaction object
     */
    public static int addTransaction(Context context,Transaction transaction){
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        moneyDB.open();

        int indexAddedTo = moneyDB.insertTransaction(transaction);
        transaction.setTransactionId(indexAddedTo);

        moneyDB.close();
        return indexAddedTo;
    }
    /**
     * Adds the individual to the database the individual must have a non null and not all whitespace name
     * @param context
     * @param indiv the individual to add
     * @return the individualIndex of the added person
     */
    public static void addIndividual(Context context, Individual indiv){//TODO return true if successful?
        if(indiv.getName() == null)
            throw new IllegalArgumentException("Must have a non null name to insert.");
        if(indiv.getName().trim().length() == 0)
            throw new IllegalArgumentException("Must have a name other than whitespace name to insert.");

        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        moneyDB.open();

        moneyDB.insertIndividual(indiv);
        moneyDB.close();
    }
    /**
     * Deletes the specified individual from the database
     * @param context
     * @param indiv the individual to delete
     * @return if the delete was successful.
     */
    public static boolean deleteIndividual(Context context, Individual indiv){
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        moneyDB.open();
        boolean result = moneyDB.deleteIndividual(indiv.getId()) > 0;
        moneyDB.close();
        return result;
    }
    /**
     * Deletes the specified transaction from the database
     * @param context
     * @param t the transaction to delete
     * @return if the delete was successful
     */
    public static boolean deleteTransaction(Context context, Transaction t){
        MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(context);
        boolean result = false;
        moneyDB.open();
        result = moneyDB.deleteTransaction(t);
        moneyDB.close();
        return result;
    }
}
