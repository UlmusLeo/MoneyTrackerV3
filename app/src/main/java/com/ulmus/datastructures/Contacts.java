package com.ulmus.datastructures;


import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class Contacts {
    /**
     * @param context
     * @return An ArrayList<String> of all the contact names in the users device
     */
    public static ArrayList<String> getContacts(Context context) {
        String[] projection = {"display_name"};

        ArrayList<String> contacts = new ArrayList<String>();

        Cursor idCursor = context.getContentResolver().query(Phone.CONTENT_URI, projection, null, null, null);
        String tempContact;
        while (idCursor.moveToNext()) {
            tempContact = idCursor.getString(0);
            if(!contacts.contains(tempContact))
                contacts.add(tempContact);
            //phoneNumber = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.CONTENT_TYPE + "=2" ));
        }
        idCursor.close();
        //	System.out.println(phoneNumber);
        return contacts;
    }
}
