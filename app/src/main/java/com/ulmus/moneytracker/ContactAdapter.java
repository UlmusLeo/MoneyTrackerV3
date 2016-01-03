package com.ulmus.moneytracker;


import java.util.ArrayList;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * This is a Filterable adapter that holds the list of names that the user might be interested in.
 * @author Jake Ullman
 *
 */
@SuppressLint("DefaultLocale")
public class ContactAdapter extends BaseAdapter implements Filterable{
    public ArrayList<String> contacts = new ArrayList<String>();
    private LayoutInflater mInflater;
    ContactFilter filter;

    /**
     * Creates the adapter
     * @param context
     * @param contacts the list of contacts that the adapter is to display and filter
     */
    public ContactAdapter(Context context,ArrayList<String> contacts) {
        filter = new ContactFilter();
        this.contacts = contacts;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    /**
     * This is the method that gets the proper views to display for the user called by the Android API
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.contact_entry_layout,
                    null);
            holder.name = (TextView) convertView
                    .findViewById(R.id.contact_entry_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(contacts.get(position));
        //System.out.println(convertView == null);
        return convertView;
    }
    /*
     * gets the contact based on where in the list it is
     */
    private String getContactByListPosition(int position) {
        if (contacts.size() > 0)
            return contacts.get(position);
        else
            return null;
    }

    /*
     * A Tempotary holder for the view
     */
    private static class ViewHolder {
        public TextView name;
    }

    @Override
    /**
     * returns how big the list of contacts is (variable based on the filtered content)
     */
    public int getCount() {
        if(contacts != null)
            return contacts.size();
        else return 0;
    }

    @Override
    /**
     * returns the contact based on the list position
     * @return String object of that is the name of the contact
     */
    public String getItem(int position) {
        return getContactByListPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    /**
     *@return the filter that is used when input in entered into the autocomplete text box
     */
    public Filter getFilter() {
        return filter;
    }
    /**
     * The filter class that updates the contacts array list when there is new input.
     * @author Jake Ullman
     *
     */
    private class ContactFilter extends Filter {
        ArrayList<String> original;
        @Override
        /**
         * Performs the filtering necessary a narrowing search is significantly faster than a widening search
         */
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults returnedResults = new FilterResults();
            ArrayList<String> results = new ArrayList<String>();
            if (original == null)
                original = contacts;

            if (constraint != null)
            {
                results.add(constraint.toString());
                if (original != null && original.size() > 0) {
                    for (String contact : original) {
                        if (contact.toLowerCase(Locale.US).contains(constraint.toString().toLowerCase(Locale.US)))
                            results.add(contact);
                    }
                }
                returnedResults.count = results.size()+1;
                returnedResults.values = results;
            }
            return returnedResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        /**
         * pushes the results to the  listview
         */
        protected void publishResults(CharSequence arg0, FilterResults results) {
            contacts = (ArrayList<String>)results.values;
            notifyDataSetChanged();
        }

    }

}
