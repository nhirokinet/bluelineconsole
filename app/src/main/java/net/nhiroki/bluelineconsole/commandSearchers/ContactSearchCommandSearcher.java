package net.nhiroki.bluelineconsole.commandSearchers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.applicationMain.BaseWindowActivity;
import net.nhiroki.bluelineconsole.commandSearchers.lib.StringMatchStrategy;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;
import net.nhiroki.bluelineconsole.wrapperForAndroid.ContactsReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactSearchCommandSearcher implements CommandSearcher {
    public static final String PREF_CONTACT_SEARCH_ENABLED_KEY = "pref_contact_search_enabled";

    private List <ContactsReader.Contact> contactList = null;

    @Override
    public void refresh(Context context) {
        try {
            this.contactList = ContactsReader.fetchAllContacts(context);

        } catch (ContactsReader.ContactReadPermissionDenied e) {
            this.contactList = new ArrayList<>();

            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEdit.putBoolean(ContactSearchCommandSearcher.PREF_CONTACT_SEARCH_ENABLED_KEY, false);
            prefEdit.apply();
        }
    }

    @Override
    public void close() {
        this.contactList = null;
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {
    }

    @NonNull
    @Override
    public List<CandidateEntry> searchCandidateEntries(String s, Context context) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_CONTACT_SEARCH_ENABLED_KEY, false)) {
            return new ArrayList<>();
        }

        List<Pair<Integer, ContactsReader.Contact>> resultList = new ArrayList<>();

        for (ContactsReader.Contact contact: contactList) {
            int match = judgeQueryForContact(context, s, contact);

            if (match >= 0) {
                resultList.add(new Pair<Integer, ContactsReader.Contact>(match, contact));
            }
        }

        Collections.sort(resultList, new Comparator<Pair<Integer, ContactsReader.Contact>>() {
            @Override
            public int compare(Pair<Integer, ContactsReader.Contact> o1, Pair<Integer, ContactsReader.Contact> o2) {
                return o1.first.compareTo(o2.first);
            }
        });


        List<CandidateEntry> ret = new ArrayList<>();
        for (Pair<Integer, ContactsReader.Contact> contactPair: resultList) {
            // TODO: icon

            ContactsReader.Contact contact = contactPair.second;

            ret.add(new ContactCandidateEntry(contact));

            for (String phoneNumber: contact.phoneNumbers) {
                ret.add(new PhoneNumberCandidateEntry(contact, phoneNumber, context));
            }

            for (String emailAddress: contact.emailAddresses) {
                ret.add(new EmailCandidateEntry(contact, emailAddress, context));
            }
        }
        return ret;
    }

    private static int judgeQueryForContact(Context context, String query, ContactsReader.Contact contact) {
        int displayNameMatch = StringMatchStrategy.match(context, query, contact.display_name, false);
        if (displayNameMatch >= 0) {
            return displayNameMatch;
        }

        for (String emailAddress: contact.emailAddresses) {
            int match = StringMatchStrategy.match(context, query, emailAddress, false);
            if (match >= 0) {
                return match + 1000000;
            }
        }
        for (String phoneNumber: contact.phoneNumbers) {
            int match = StringMatchStrategy.match(context, query, phoneNumber, false);
            if (match >= 0) {
                return match + 2000000;
            }
        }

        return -1;
    }

    private static class ContactCandidateEntry implements CandidateEntry {
        private final ContactsReader.Contact contact;

        private ContactCandidateEntry(ContactsReader.Contact contact) {
            this.contact = contact;
        }

        @NonNull
        @Override
        public String getTitle() {
            return this.contact.display_name;
        }

        @Override
        public View getView(Context context) {
            return null;
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return null;
        }

        @Override
        public Drawable getIcon(Context context) {
            // TODO
            return null;
        }

        @Override
        public boolean hasEvent() {
            return false;
        }

        @Override
        public boolean isSubItem() {
            return false;
        }
    }

    private static class PhoneNumberCandidateEntry implements CandidateEntry {
        private final ContactsReader.Contact contact;
        private final String phoneNumber;
        private final String title;

        private PhoneNumberCandidateEntry(ContactsReader.Contact contact, String phoneNumber, Context context) {
            this.contact = contact;
            this.phoneNumber = phoneNumber;
            this.title = String.format(context.getString(R.string.contacts_action_dial_phone_number), this.phoneNumber);
        }

        @NonNull
        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public View getView(Context context) {
            return null;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return new EventLauncher() {
                @Override
                public void launch(BaseWindowActivity activity) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(PhoneNumberCandidateEntry.this.phoneNumber)));
                    activity.startActivity(intent);
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return true;
        }

        @Override
        public boolean isSubItem() {
            return true;
        }
    }

    private static class EmailCandidateEntry implements CandidateEntry {
        private final ContactsReader.Contact contact;
        private final String emailAddresss;
        private final String title;

        private EmailCandidateEntry(ContactsReader.Contact contact, String emailAddress, Context context) {
            this.contact = contact;
            this.emailAddresss = emailAddress;
            this.title = String.format(context.getString(R.string.contacts_action_email), this.emailAddresss);
        }

        @NonNull
        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public View getView(Context context) {
            return null;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return new EventLauncher() {
                @Override
                public void launch(BaseWindowActivity activity) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Uri.encode(EmailCandidateEntry.this.emailAddresss)));
                    activity.startActivity(intent);
                }
            };
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return true;
        }

        @Override
        public boolean isSubItem() {
            return true;
        }
    }
}
