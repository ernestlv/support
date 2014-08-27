package com.scrippsnetworks.wcm.util;

import java.lang.String;

public class SelectionOption {

    private String name = null;
    private String text = null;
    private String qtip = null;

    public SelectionOption(String name, String text, String qtip) {
        this.name = name;
        this.text = text;
        this.qtip = qtip;
    }

    public String getName() { return name; }
    public String getText() { return text; }
    public String getQtip() { return qtip; }
}
