package com.scrippsnetworks.wcm.mobile.leadimagewithstack;

public interface LeadImageItem {
    public int getNum();
    public String getTitle();
    public String getTitleLink();
    public String getImageDamPath();
    public String getImageUrl();
    public String getCssClassName();
    public boolean isValid();
}
