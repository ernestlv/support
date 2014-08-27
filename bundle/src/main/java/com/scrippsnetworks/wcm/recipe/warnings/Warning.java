package com.scrippsnetworks.wcm.recipe.warnings;

import com.scrippsnetworks.wcm.snitag.SniTag;
import java.util.List;

/**
 * @author Jason Clark
 *         Date: 7/16/13
 */
public interface Warning {

    /** Warning tags associated to this Recipe. */
    public List<SniTag> getWarnings();

}
