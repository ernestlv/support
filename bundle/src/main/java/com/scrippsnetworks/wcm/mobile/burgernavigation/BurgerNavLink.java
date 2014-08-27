package com.scrippsnetworks.wcm.mobile.burgernavigation;

public interface BurgerNavLink {
    public String getTitle();
    public String getUrl();
    public boolean isPrimary();
    public String getActiveClass();
}
