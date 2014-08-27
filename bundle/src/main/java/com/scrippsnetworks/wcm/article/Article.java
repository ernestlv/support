package com.scrippsnetworks.wcm.article;

import com.scrippsnetworks.wcm.image.SniImage;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 * @updated Ken Shih 7/5/13
 * @updated Venkata Naga Sudheer Donaboina 9/12/13
 */
public interface Article {
    /** Article Image. */
    public SniImage getFirstImage();
    
    /** Byline of article. */
    public String getByLine();
    
    /** Article Body. */
    public String getBody();
    
}
