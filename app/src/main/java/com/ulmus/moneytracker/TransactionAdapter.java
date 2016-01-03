package com.ulmus.moneytracker;

import java.util.ArrayList;

import com.ulmus.database.MoneyTrackerDBEditor;
import com.ulmus.datastructures.Individual;
import com.ulmus.datastructures.Transaction;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class TransactionAdapter extends BaseAdapter {
    private final ArrayList<Transaction> transactions;

    private LayoutInflater mInflater;

    private String iPaid, theyPaid;
    private final Individual individual;
    private final ListFragment listFragment;
    /**
     * Creates the transaction adapter
     * @param transactions the list of transactions can be null
     */
    public TransactionAdapter(ListFragment listFragment, ArrayList<Transaction> transactions, Individual individual) {
        this.individual = individual;
        if(transactions != null)
            this.transactions = transactions;
        else
            this.transactions = new ArrayList<Transaction>();
        this.listFragment = listFragment;
        mInflater = (LayoutInflater)listFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iPaid = listFragment.getResources().getString(R.string.i_paid);
        theyPaid = listFragment.getResources().getString(R.string.they_paid);

    }

    /**
     * Clears the adapter
     */
    public void clear(){
        transactions.clear();
        notifyDataSetChanged();
    }
    /**
     * deletes and item from the list
     * @param position
     */
    public void deleteItem(int position){
        transactions.remove(position);
        notifyDataSetChanged();
    }

    public void add(Transaction trans){
        transactions.add(trans);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position){
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        //	int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            holder.position = position;

            convertView = mInflater.inflate(R.layout.transaction_view, null);
            holder.owesWhom = (TextView) convertView.findViewById(R.id.owes_whome_text);
            holder.amount = (TextView) convertView.findViewById(R.id.dollar_amount);
            holder.note = (TextView) convertView.findViewById(R.id.note_textView);
            holder.date = (TextView) convertView.findViewById(R.id.date_view);
            holder.open = (CheckBox) convertView.findViewById(R.id.check_box_open_t);
            holder.open.setOnClickListener( new CheckBox.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Transaction t = transactions.get((Integer)v.getTag());
                    t.setOpen(!((CheckBox) v).isChecked());
                    individual.setTotalAmount(individual.getTotalAmount() + (t.isOpenTransaction() ? t.getAmount() : -t.getAmount()));
                    listFragment.notifyDataSetChanged();
                    TransactionAdapter.this.notifyDataSetChanged();
                    MoneyTrackerDBEditor.setTransaction(listFragment.getActivity(), t,individual);
                }
            });
            holder.open.setFocusable(false);
            holder.open.setFocusableInTouchMode(false);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.owesWhom.setText(makeToWhom(transactions.get(position)));
        holder.amount.setText(transactions.get(position).getAmountFormatted());
        if(transactions.get(position).getAmount() < 0)
            holder.amount.setTextColor(Color.RED);
        else
            holder.amount.setTextColor(Color.BLACK);

        if(transactions.get(position).isOpenTransaction()){
            holder.amount.setPaintFlags(holder.amount.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.note.setPaintFlags(holder.note.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.owesWhom.setPaintFlags(holder.owesWhom.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.date.setPaintFlags(holder.date.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        }
        else{
            holder.amount.setPaintFlags(holder.amount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.note.setPaintFlags(holder.note.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.owesWhom.setPaintFlags(holder.owesWhom.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.date.setPaintFlags(holder.date.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        holder.note.setText(transactions.get(position).getNote());
        holder.date.setText(transactions.get(position).getStringDate());
        holder.open.setChecked(!transactions.get(position).isOpenTransaction());
        holder.open.setTag(position);
        return convertView;
    }

    private String makeToWhom(Transaction trans) {
        return trans.getAmount()>0 ? iPaid : theyPaid;
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public Transaction getItem(int position) {
        return transactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public int position=-1;
        public TextView owesWhom;
        public TextView note;
        public TextView amount;
        public TextView date;
        public CheckBox open;

        public String toString(){
            return position+"";
        }
    }
}



