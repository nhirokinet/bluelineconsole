package net.nhiroki.bluelineconsole.wrapperForAndroid;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;
import java.util.List;

public class ContactsReader {
    public static class Contact {
        public String display_name = "";
        public List<String> phoneNumbers = new ArrayList<>();
        public List<String> emailAddresses = new ArrayList<>();
    }

    public static class ContactReadPermissionDenied extends Exception {}


    public static boolean appHasReadContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static List<Contact> fetchAllContacts(Context context) throws ContactReadPermissionDenied {
        if (!appHasReadContactsPermission(context)) {
            throw new ContactReadPermissionDenied();
        }

        List<Contact> ret = new ArrayList<>();
        final ContentResolver contentResolver = context.getContentResolver();

        final android.database.Cursor cursor;

        try {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[0], null, new String[0], null);

        } catch (Exception e) {
            // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all...
            return new ArrayList<>();
        }

        while (cursor.moveToNext()) {
            Contact contact = new Contact();

            try {
                // TODO: icon
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.display_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                android.database.Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{contactId}, null);
                while (phoneCursor.moveToNext()) {
                    contact.phoneNumbers.add(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA)));
                }
                phoneCursor.close();

                android.database.Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{contactId}, null);
                while(emailCursor.moveToNext()) {
                    contact.emailAddresses.add(emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
                }
                emailCursor.close();

                ret.add(contact);

            } catch (Exception e) {
                cursor.close();
                // TODO: Improve this by checking doc. This unexpected, but better than being unable to open app at all...
            }
        }

        cursor.close();
        return ret;
    }
}
