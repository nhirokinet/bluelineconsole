package net.nhiroki.bluelineconsole.wrapperForAndroid;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContactsReader {
    public static class Contact {
        public String displayName = "";
        public String phoneticName = "";
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

        Map<String, List<String>> phoneNumbers = new HashMap<>();
        Map<String, List<String>> emailAddresses = new HashMap<>();

        final android.database.Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[0], null, new String[0], null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                final int contactIdColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                final int dataColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);

                if (contactIdColumnIndex != -1 && dataColumnIndex != -1) {
                    final String contactId = phoneCursor.getString(contactIdColumnIndex);
                    if (!phoneNumbers.containsKey(contactId)) {
                        phoneNumbers.put(contactId, new ArrayList<String>());
                    }
                    final String phoneNumStr = phoneCursor.getString(dataColumnIndex);
                    if (phoneNumStr != null) {
                        phoneNumbers.get(contactId).add(phoneNumStr);
                    }
                }
            }
            phoneCursor.close();
        }

        final android.database.Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[0], null, new String[0], null);

        if (emailCursor != null) {
            while (emailCursor.moveToNext()) {
                final int contactIdColumnIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
                final int dataColumnIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                if (contactIdColumnIndex != -1 && dataColumnIndex != -1) {
                    final String contactId = emailCursor.getString(contactIdColumnIndex);
                    if (!emailAddresses.containsKey(contactId)) {
                        emailAddresses.put(contactId, new ArrayList<String>());
                    }
                    final String emailStr = emailCursor.getString(dataColumnIndex);
                    if (emailStr != null) {
                        emailAddresses.get(contactId).add(emailStr);
                    }
                }
            }
            emailCursor.close();
        }

        final android.database.Cursor contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[0], null, new String[0], null);
        if (contactsCursor != null) {
            while (contactsCursor.moveToNext()) {
                final int idColumnIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID);
                final int displayNameColumnIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int phoneticNameColumnIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.PHONETIC_NAME);

                if (idColumnIndex != -1 && displayNameColumnIndex != -1) {
                    Contact contact = new Contact();

                    final String contactId = contactsCursor.getString(idColumnIndex);
                    contact.displayName = contactsCursor.getString(displayNameColumnIndex);

                    if (contact.displayName == null) {
                        contact.displayName = "";
                    }

                    if (phoneticNameColumnIndex != -1) {
                        contact.phoneticName = contactsCursor.getString(phoneticNameColumnIndex);

                        if (contact.phoneticName == null) {
                            contact.phoneticName = "";
                        }
                    }

                    if (phoneNumbers.containsKey(contactId)) {
                        contact.phoneNumbers = phoneNumbers.get(contactId);
                    }

                    if (emailAddresses.containsKey(contactId)) {
                        contact.emailAddresses = emailAddresses.get(contactId);
                    }

                    ret.add(contact);
                }
            }

            contactsCursor.close();
        }
        return ret;
    }
}
