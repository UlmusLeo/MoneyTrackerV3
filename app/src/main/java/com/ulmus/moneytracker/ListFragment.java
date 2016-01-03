package com.ulmus.moneytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ulmus.database.MoneyTrackerDBAdapter;
import com.ulmus.database.MoneyTrackerDBEditor;
import com.ulmus.datastructures.Contacts;
import com.ulmus.datastructures.Individual;
import com.ulmus.datastructures.QuickAmountSeekbar;
import com.ulmus.datastructures.Transaction;


public class ListFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener {
    PeopleAdapter peopleAdapter;
    PopulateList listPopulator;
    TextView noPeopleTextView;
    String[] LongClickOptions = new String[2];
    TransactionViewer transactionViewer;

    public void onAttach(Activity activity) {

        super.onAttach(activity);

        LongClickOptions[1] = this.getResources().getString(R.string.delete);
        LongClickOptions[0] = this.getResources().getString(R.string.edit_name);

        peopleAdapter = new PeopleAdapter(this.getActivity(), this);

        listPopulator = new PopulateList();
        listPopulator.execute((Void) null);
    }

    public ListFragment() {

    }

    public void onDestroy() {
        super.onDestroy();
        if (listPopulator != null && !listPopulator.getStatus().equals(AsyncTask.Status.FINISHED))
            listPopulator.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_base,
                container, false);

        noPeopleTextView = (TextView) rootView.findViewById(R.id.no_items_text);
        noPeopleTextView.setVisibility(View.GONE);

        ListView list = (ListView) rootView.findViewById(R.id.people_list_view);
        list.setOnItemLongClickListener(this);
        list.setOnItemClickListener(this);
        list.setAdapter(peopleAdapter);
        list.requestFocus();

        transactionViewer = new TransactionViewer(this);
        return rootView;
    }



    private class PopulateList extends AsyncTask<Void, Individual, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            MoneyTrackerDBAdapter moneyDB = new MoneyTrackerDBAdapter(ListFragment.this.getActivity());
            moneyDB.open();
            Cursor indivudualCur = moneyDB.getAllIndividuals();

            Individual temp;
            while (indivudualCur.moveToNext() && !isCancelled()) {
                temp = new Individual(indivudualCur.getInt(0), indivudualCur.getString(1), indivudualCur.getDouble(2), indivudualCur.getLong(3));
                this.publishProgress(temp);
            }
            indivudualCur.close();
            moneyDB.close();

            return null;
        }

        @Override
        protected void onProgressUpdate(Individual... progress) {
            if (!isCancelled()) {
                peopleAdapter.add(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(Void result) {

            if (!isCancelled()) {
                if(peopleAdapter.getCount() == 0)
                    noPeopleTextView.setVisibility(View.VISIBLE);
            }
        }

    }

    public void notifyDataSetChanged() {
        peopleAdapter.notifyDataSetChanged();
        if(peopleAdapter.getCount() > 0) {
            noPeopleTextView.setVisibility(View.GONE);
        }
        else{
            noPeopleTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        transactionViewer.viewTransactionsDialog(peopleAdapter.getItem(position), ListFragment.this.getActivity());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                   long arg3) {
        longClickOptionsDialog(peopleAdapter.getItem(position), position);

        return true;
    }

    public void add(Individual indiv){
        peopleAdapter.add(indiv); //Add it to the view
    }

    public void newTransactionDialog(Individual indiv){
        transactionViewer.newTransactionDialog(indiv);
    }

    private void editNameDialog(final Individual indiv) {
        final AutoCompleteTextView input = new AutoCompleteTextView(this.getActivity());
        input.setThreshold(1);
        input.setText(indiv.getName());
        input.setAdapter(new ContactAdapter(this.getActivity(), Contacts.getContacts(ListFragment.this.getActivity())));
        input.setSelection(indiv.getName().length());
        input.setHint(this.getResources().getString(R.string.who_hint));

        new AlertDialog.Builder(this.getActivity())
                .setTitle(this.getResources().getString(R.string.edit_name))
                .setView(input)
                .setPositiveButton(this.getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = input.getText().toString();
                        MoneyTrackerDBEditor.setIndividualName(ListFragment.this.getActivity(), indiv, newName); //update database
                        peopleAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(this.getResources().getString(R.string.cancel), null)
                .show();
    }

    private void deleteIndividualDialog(final Individual indiv, final int position) {
        new AlertDialog.Builder(this.getActivity())
                .setTitle(this.getResources().getString(R.string.delete))
                .setMessage(this.getResources().getString(R.string.remove_individual_warning))
                .setCancelable(false)
                .setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MoneyTrackerDBEditor.deleteIndividual(ListFragment.this.getActivity(), indiv);
                        peopleAdapter.remove(position);
                        if (peopleAdapter.getCount() == 0) {
                            noPeopleTextView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .setNegativeButton(this.getResources().getString(R.string.no), null)
                .show();
    }

    private void longClickOptionsDialog(final Individual indiv, final int position) {
        new AlertDialog.Builder(this.getActivity())
                .setItems(LongClickOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch (id) {
                            case 1:
                                deleteIndividualDialog(indiv, position);
                                break;
                            case 0:
                                editNameDialog(indiv);
                                break;
                        }

                    }
                })
                .show();
    }

}