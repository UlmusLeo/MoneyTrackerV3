package com.ulmus.moneytracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;

import com.ulmus.database.MoneyTrackerDBEditor;
import com.ulmus.datastructures.Contacts;
import com.ulmus.datastructures.Individual;
import com.ulmus.datastructures.QuickAmountSeekbar;
import com.ulmus.datastructures.Transaction;

/**
 * Created by Jake on 5/14/15.
 */

public class TransactionViewer {
    ListFragment list;


    public TransactionViewer(ListFragment list){
        this.list = list;
    }

    public void viewTransactionsDialog(final Individual indiv, final Context context) {
        ListView view = new ListView(context);
        view.setCacheColorHint(0); //so list view does not turn white

        final TransactionAdapter adapter = new TransactionAdapter(list, MoneyTrackerDBEditor.getIndividualsTransactions(context, indiv), indiv);
        view.setAdapter(adapter);

        class onTransactionLongClick implements AdapterView.OnItemLongClickListener {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                           long arg3) {
                deleteTransactionDialog(indiv, adapter.getItem(position), adapter, position);
                return true;
            }
        }
        class onTransactionClick implements AdapterView.OnItemClickListener {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editTransactionDialog(indiv, adapter.getItem(position), adapter, position);
            }
        }

        view.setOnItemLongClickListener(new onTransactionLongClick());
        view.setOnItemClickListener(new onTransactionClick());

        new AlertDialog.Builder(context)
                .setTitle(indiv.getName())
                .setView(view)
                .show();
    }

    private void deleteTransactionDialog(final Individual indiv, final Transaction tran, final TransactionAdapter adapter, final int position) {
        Resources appResources = list.getResources();
        new AlertDialog.Builder(list.getActivity())
                .setTitle(appResources.getString(R.string.delete))
                .setMessage(appResources.getString(R.string.remove_transaction_warning))
                .setCancelable(false)
                .setPositiveButton(appResources.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        indiv.removeTransaction(tran);
                        MoneyTrackerDBEditor.deleteTransaction(list.getActivity(), tran);
                        adapter.deleteItem(position);
                        list.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(appResources.getString(R.string.no), null)
                .show();
    }

    private void editTransactionDialog(final Individual indiv, final Transaction tran, final TransactionAdapter adapter, final int position) {

        //Build the dialog
        final AutoCompleteTextView name_dynamic_field;
        final EditText note_field;
        final EditText amount_field;
        final SeekBar seek_bar;
        final Button more_button;
        final Button less_button;
        final RadioButton i_owe_button;
        final RadioButton you_owe_button;


        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(list.getActivity());
        LayoutInflater inflater = list.getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.activity_add_new, null);

        name_dynamic_field = (AutoCompleteTextView) v.findViewById(R.id.who_dynamic);
        note_field = (EditText) v.findViewById(R.id.note_edit_text);
        amount_field = (EditText) v.findViewById(R.id.amount_value);
        seek_bar = (SeekBar) v.findViewById(R.id.quick_amount_seek_bar);
        less_button = (Button) v.findViewById(R.id.button_less);
        more_button = (Button) v.findViewById(R.id.button_more);
        i_owe_button = (RadioButton) v.findViewById(R.id.i_owe_radioButton);
        you_owe_button = (RadioButton) v.findViewById(R.id.you_owe_radioButton);

        //Ties Quick amount functionality between quick_amount_seek_bar and amount_field
        QuickAmountSeekbar quick_amount = new QuickAmountSeekbar(seek_bar, amount_field, less_button, more_button);

        //set known values

        builder.setTitle(list.getResources().getString(R.string.edit_transaction)); //set name
        name_dynamic_field.setVisibility(View.GONE); //remove editable name
        note_field.setText(tran.getNote());
        quick_amount.setAmount(Math.abs(tran.getAmount()));
        if(tran.isDebt())
            i_owe_button.setChecked(true);
        else
            you_owe_button.setChecked(true);

        //Set the functionality of the dialog

        DialogInterface.OnClickListener CompleteTransaction = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Make the transaction
                double amount = Transaction.parseDollarAmount(amount_field.getText().toString());

                switch(which){
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Delete the transaction
                        indiv.removeTransaction(tran);
                        MoneyTrackerDBEditor.deleteTransaction(list.getActivity(), tran);
                        adapter.deleteItem(position);
                        break;

                    case DialogInterface.BUTTON_POSITIVE:
                        //we are updating this transaction so remove it form the individual
                        if(tran.isOpenTransaction()){
                            indiv.removeTransaction(tran);
                        }

                        if(i_owe_button.isChecked())
                            amount = -amount;
                        String note = note_field.getText().toString();


                        tran.setAmount(amount);
                        tran.setNote(note);

                        //we have updated the transacion so we can add it back to the individual
                        if(tran.isOpenTransaction()){
                            indiv.addTransaction(tran);
                        }
                        MoneyTrackerDBEditor.setTransaction(list.getActivity(), tran, indiv);
                        break;
                }

                //Make the view update
                list.notifyDataSetChanged();
                adapter.notifyDataSetChanged();


            }
        };

        //show the dialog

        builder.setView(v)
                .setCancelable(false)
                .setPositiveButton(list.getResources().getString(R.string.done), CompleteTransaction)
                .setNeutralButton(list.getResources().getString(R.string.cancel), null)
                .setNegativeButton(list.getResources().getString(R.string.delete), CompleteTransaction);
        builder.show();
    }



    public void newTransactionDialog(final Individual indiv) {
        //Build the dialog
        final AutoCompleteTextView name_dynamic_field;
        final EditText note_field;
        final EditText amount_field;
        final SeekBar seek_bar;
        final Button more_button;
        final Button less_button;

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(list.getActivity());
        LayoutInflater inflater = list.getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.activity_add_new, null);

        name_dynamic_field = (AutoCompleteTextView) v.findViewById(R.id.who_dynamic);
        note_field = (EditText) v.findViewById(R.id.note_edit_text);
        amount_field = (EditText) v.findViewById(R.id.amount_value);
        seek_bar = (SeekBar) v.findViewById(R.id.quick_amount_seek_bar);
        less_button = (Button) v.findViewById(R.id.button_less);
        more_button = (Button) v.findViewById(R.id.button_more);

        v.findViewById(R.id.  i_owe_radioButton).setVisibility(View.GONE);
        v.findViewById(R.id.you_owe_radioButton).setVisibility(View.GONE);

        //Ties Quick amount functionality between quick_amount_seek_bar and amount_field
        new QuickAmountSeekbar(seek_bar, amount_field, less_button, more_button);


        if (indiv != null) {
            builder.setTitle(indiv.getName());
            name_dynamic_field.setVisibility(View.GONE);
        } else {
            name_dynamic_field.setAdapter(new ContactAdapter(list.getActivity(), Contacts.getContacts(list.getActivity())));
        }

        //Set the functionality of the dialog

        DialogInterface.OnClickListener CompleteTransaction = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Get the individual
                Individual newIndiv;

                if (indiv == null) {
                    //If individual doesn't exist make a new one
                    newIndiv = new Individual(name_dynamic_field.getText().toString().trim());
                    MoneyTrackerDBEditor.addIndividual(list.getActivity(), newIndiv); //Add it to the database
                    list.add(newIndiv); //Add it to the view
                } else
                    newIndiv = indiv;

                //Make the transaction
                double amount = Transaction.parseDollarAmount(amount_field.getText().toString());

                //If it is the positive button we don't do anything but if it is the negative button we have a negative transaction;
                if (which == DialogInterface.BUTTON_NEGATIVE)
                    amount = -amount;

                String note = note_field.getText().toString();
                Transaction newTransaction = new Transaction(newIndiv.getId(), amount, note, System.currentTimeMillis(), Transaction.TRANSACTION_OPEN);

                //Add the transaction
                MoneyTrackerDBEditor.addTransaction(list.getActivity(), newTransaction);
                newIndiv.addTransaction(newTransaction);

                //Make the view update
                list.notifyDataSetChanged();

            }
        };

        //show the dialog

        builder.setView(v)
                .setCancelable(false)
                .setPositiveButton(list.getResources().getString(R.string.you_owe_me), CompleteTransaction)
                .setNeutralButton(list.getResources().getString(R.string.cancel), null)
                .setNegativeButton(list.getResources().getString(R.string.i_owe_you), CompleteTransaction);
        builder.show();
    }
}
