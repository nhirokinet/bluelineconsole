package net.nhiroki.bluelineconsole.wrapperForAndroid;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, List <String>> phoneNumbers = new HashMap<>();
        Map<String, List <String>> emailAddresses = new HashMap<>();

        final android.database.Cursor phoneCursor;
        try {
            phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[0], null, new String[0], null);

        } catch (Exception e) {
            // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all, and also call in non-main thread
            return new ArrayList<>();
        }

        while (phoneCursor.moveToNext()) {
            try {
                final String contactId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                if (! phoneNumbers.containsKey(contactId)) {
                    phoneNumbers.put(contactId, new ArrayList<String>());
                }
                phoneNumbers.get(contactId).add(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA)));
            } catch (Exception e) {
                // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all, and also call in non-main thread
            }
        }
        phoneCursor.close();

        final android.database.Cursor emailCursor;
        try {
            emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[0], null, new String[0], null);

        } catch (Exception e) {
            // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all, and also call in non-main thread
            return new ArrayList<>();
        }

        while (emailCursor.moveToNext()) {
            try {
                final String contactId = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                if (! emailAddresses.containsKey(contactId)) {
                    emailAddresses.put(contactId, new ArrayList<String>());
                }
                emailAddresses.get(contactId).add(emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            } catch (Exception e) {
                // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all, and also call in non-main thread
            }
        }
        emailCursor.close();

        final android.database.Cursor contactsCursor;
        try {
            contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[0], null, new String[0], null);

        } catch (Exception e) {
            // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all, and also call in non-main thread
            return new ArrayList<>();
        }

        while (contactsCursor.moveToNext()) {
            Contact contact = new Contact();

            try {
                // TODO: icon
                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.display_name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (phoneNumbers.containsKey(contactId)) {
                    contact.phoneNumbers = phoneNumbers.get(contactId);
                }

                if (emailAddresses.containsKey(contactId)) {
                    contact.emailAddresses = emailAddresses.get(contactId);
                }

                ret.add(contact);

            } catch (Exception e) {
                contactsCursor.close();
                // TODO: Improve this by checking doc. This is unexpected, but better than being unable to open app at all, and also call in non-main thread
            }
        }

        contactsCursor.close();
        return ret;
    }
}
