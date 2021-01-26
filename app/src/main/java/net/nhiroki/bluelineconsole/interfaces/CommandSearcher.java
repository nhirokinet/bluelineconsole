package net.nhiroki.bluelineconsole.interfaces;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

public interface CommandSearcher {
    /**
     * Reload internal data. Must be called at least once before searchCandidateEntries(String, Context).
     * Can start just a thread and keep isPrepared() false for a while after returning.
     * Must return so early that users can wait.
     */
    void refresh(Context context);

    /**
     * Release internal resources.
     */
    void close();

    /**
     * @return if this searcher is available for search.
     */
    boolean isPrepared();

    /**
     * Wait until isPrepared() gets true.
     */
    void waitUntilPrepared();

    /**
     * Search for commands. Call only after isPrepared() is true, or waitUntilPrepared() returned.
     * @param s Search query stromg
     * @param context Android Context for the corresponding Activity
     * @return List of result
     */
    @NonNull
    List<CandidateEntry> searchCandidateEntries(String s, Context context);
}
