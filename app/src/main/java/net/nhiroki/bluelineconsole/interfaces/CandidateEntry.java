package net.nhiroki.bluelineconsole.interfaces;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

public interface CandidateEntry {
    /**
     * @return The title to display.
     */
    public String getTitle();

    /**
     *
     * @param context
     * @return Detail view, or null if nothing to show.
     */
    public View getView(Context context);

    /**
     * Whether getView() may return vertically long view or not.
     * This is used to determine the layout.
     */
    public boolean hasLongView();

    /**
     * @param context
     * @return EventLauncher correspoinding to event, or null if nothing to do.
     */
    public EventLauncher getEventLauncher(Context context);

    /**
     * Return icon for this entry, or null if nothing to show.
     * Returning null here reserves a little bit wider area for detail view.
     */
    public Drawable getIcon(Context context);

    /**
     * Return whether this CandidateEntry has an event to launch.
     * @return must exactly same as (getEventLauncher(context) != null).
     */
    public boolean hasEvent();

}
