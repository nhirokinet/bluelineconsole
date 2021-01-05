package net.nhiroki.bluelineconsole.interfaces;

import android.content.Context;

import java.util.List;

public interface CommandSearcher {
    /**
     * Reload internal data. Must be called at least once before searchCandidateEntries(String, Context).
     * Can start just a thread and keep isPrepared() false for a while after returning.
     * Must return so early that users can wait.
     */
    public void refresh(Context context);

    /**
     * Release internal resources.
     */
    public void close();

    /**
     * @return if this searcher is available for search.
     */
    public boolean isPrepared();

    /**
     * Wait until isPrepared() gets true.
     */
    public void waitUntilPrepared();

    /**
     * Search for commands. Call only after isPrepared() is true, or waitUntilPrepared() returned.
     * @param s Search query stromg
     * @param context
     * @return List of result
     */
    public List<CandidateEntry> searchCandidateEntries(String s, Context context);
}
