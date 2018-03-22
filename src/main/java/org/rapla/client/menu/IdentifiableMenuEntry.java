package org.rapla.client.menu;

import org.rapla.client.RaplaWidget;

/** Adds an id to the standard Swing Menu ServerComponent as JSeperator, JMenuItem and JMenu*/
public interface IdentifiableMenuEntry extends RaplaWidget
{
    String getId();

    IdentifiableMenuEntry[] EMPTY_ARRAY  = new IdentifiableMenuEntry[]{
    };
}
