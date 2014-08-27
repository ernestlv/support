package com.scrippsnetworks.wcm.hub.button;

import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;

public interface HubButton {

	/** returns one of a set known names (which could an enum), holding known contain-able assets */
	public HubPageTypeKeys getKey();

	/** returns url associated with the asset page */
	public String getHref();

	/** returns the text of the button label */
	public String getButtonLabel();

	/** returns number of items, if the button represents a collection; returns null if it is not a collection */
	public Integer getCount();

    /** The path of the page used to construct this button, if any. */
    public String getPagePath();
}
