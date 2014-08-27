/**
 * 
 */
package com.scrippsnetworks.wcm.util;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date: 9/12/13
 *
 */
public enum DialogPropertyNames {

    SNI_RECIPE1("sni:recipe1"),
    SNI_RECIPE2("sni:recipe2"),
    SNI_RECIPE3("sni:recipe3"),
    SNI_MEALTYPE1("sni:mealType1"),
    SNI_MEALTYPE2("sni:mealType2"),
    SNI_MEALTYPE3("sni:mealType3"),
    ARTICLE_BYLINE_PREFACE("byline-preface"),
    FREE_FORM("sni:freeform"),
    ARTICLE_TEXT("text");
    
    private String dialogPropertyName;

    private DialogPropertyNames(final String dialogPropertyName) {
        this.dialogPropertyName = dialogPropertyName;
    }

    public String dialogPropertyName() {
        return this.dialogPropertyName;
    }

}
