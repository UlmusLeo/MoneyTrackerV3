package com.ulmus.moneytracker;


import java.util.ArrayList;
import java.util.Collections;

import com.ulmus.datastructures.Individual;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PeopleAdapter extends BaseAdapter implements ListAdapter {
    public static ArrayList<Individual> individuals;
    private LayoutInflater mInflater;
    private final ListFragment parentList;
    private String iOwe, theyOwe;
    /**
     *
     * @param context
     */
    public PeopleAdapter(Context context,ListFragment parnetList) {
        this.parentList = parnetList;
        if(context == null)
            throw new IllegalArgumentException("Context can not be null thank you muchly");
        individuals = new ArrayList<Individual>();
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iOwe = context.getResources().getString(R.string.i_owe_you);
        theyOwe = context.getResources().getString(R.string.you_owe_me);
    }

    /**
     * Adds individual to the display
     * @param individual The new Individual
     */
    public void add(Individual individual) {
        individuals.add(individual);
        orderIndividuals();
        notifyDataSetChanged();
    }

    /**
     * removes the individual at the given position
     * @param position
     */
    public void remove(int position) {
        individuals.remove(position);
        notifyDataSetChanged();
    }

    /**
     * Removes all individuals from the display
     */
    public void removeAll(){
        individuals.clear();
        notifyDataSetChanged();
    }

    /**
     * Orders the Individuals based on the latest date
     */
    private void orderIndividuals() {
        Collections.sort(individuals);
    }

    @Override
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.person_entry,
                    null);
            holder.individual = (TextView) convertView
                    .findViewById(R.id.individual_name);
            holder.amount = (TextView) convertView
                    .findViewById(R.id.amount);
            holder.whoOwesString = (TextView) convertView
                    .findViewById(R.id.who_owes_string);
            holder.newTransButton = (ImageButton) convertView.findViewById(R.id.button_new_transaction);
            holder.newTransButton.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    int position = (Integer) arg0.getTag();
                    parentList.newTransactionDialog(individuals.get(position));
                }
            });
            holder.newTransButton.setFocusable(false);
            holder.newTransButton.setFocusableInTouchMode(false);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.individual.setTextSize(18);
        holder.individual.setText(individuals.get(position).getName());
        holder.amount
                .setText(individuals.get(position).getTotalAmountFormatted());
        holder.amount.setTypeface(null,Typeface.BOLD);
        if(individuals.get(position).getTotalAmount()<0)
            holder.amount.setTextColor(Color.RED);
        else
            holder.amount.setTextColor(Color.BLACK);

        holder.whoOwesString.setText(makeWhoOwesString(individuals.get(position)));
        holder.newTransButton.setTag(position);
        return convertView;
    }
    /*
     * @return returns a String based on whether they owe the user or the user owes the individual
     */
    private String makeWhoOwesString(Individual indiv) {
        return indiv.getTotalAmount()>=0 ? theyOwe:iOwe;
    }

    private static class ViewHolder {
        public ImageButton newTransButton;
        public TextView individual;
        public TextView whoOwesString;
        public TextView amount;
    }

    @Override
    public int getCount() {
        return individuals.size();
    }

    @Override
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
    public Individual getItem(int position) {
        return individuals.get(position);
    }

    @Override
	/*
	 * Can be implemented to get the Individual ID for the database perhaps its is mostly a filler method.
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
    public long getItemId(int position) {
        return 0;
    }

}
