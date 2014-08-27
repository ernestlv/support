package com.scrippsnetworks.wcm.util;



import java.util.HashMap;
import java.util.Map;



/**
 * Mapping of windows-1252 to Utf-8
 * This class  is used to convert some characters  of window-1252  (which are not valid in ISO-8859-1)to Unicode  .
 * Refer COOKING-4932 for more information
 *
 * http://en.wikipedia.org/wiki/ISO-8859-1 
 *  * http://en.wikipedia.org/wiki/Windows-1252
 *  
 * @author veerinaiduj
 *
 */
public  class CharsetConversion {

	public  static final Map <String ,String> charactersMap=new HashMap<String, String>(){
		{
		put("&#151;", "\\\u2014");   // converting EM DASH
		put("&#128;", "\\\u20AC");   // converting EURO SIGN 
		put("&#130;", "\\\u201A");   // converting SINGLE LOW-9 QUOTATION MARK
		put("&#131;", "\\\u0192");   // converting LATIN SMALL LETTER F WITH HOOK 
		put("&#132;", "\\\u201E");   // DOUBLE LOW-9 QUOTATION MARK 
        put("&#133;", "\\\u2026");   // converting HORIZONTAL ELLIPSIS 
		put("&#134;", "\\\u2020");   // converting DAGGER 
		put("&#135;", "\\\u2021");   // converting DOUBLE DAGGER 
		put("&#136;", "\\\u02C6");   // converting MODIFIER LETTER CIRCUMFLEX ACCENT 
		put("&#137;", "\\\u2030");   // converting PER MILLE SIGN 
		put("&#138;", "\\\u0160");   // converting LATIN CAPITAL LETTER S WITH CARON 
		put("&#139;", "\\\u2039");   // converting SINGLE LEFT-POINTING ANGLE QUOTATION MARK (<) 
		put("&#140;", "\\\u0152");   // converting LATIN CAPITAL LIGATURE OE 
		put("&#142;", "\\\u017D");   // converting LATIN CAPITAL LETTER Z WITH CARON 
		put("&#145;", "\\\u2018");   // converting LEFT SINGLE QUOTATION MARK 
		put("&#146;", "\\\u2019");   // converting RIGHT SINGLE QUOTATION MARK 
		put("&#147;", "\\\u201C");   // converting LEFT DOUBLE QUOTATION MARK 
		put("&#148;", "\\\u201D");   // converting RIGHT DOUBLE QUOTATION MARK 
		put("&#150;", "\\\u2013");   // converting EN Dash 
		put("&#152;", "\\\u02DC");   // converting 	SMALL TILDE 
		put("&#153;", "\\\u2122");   // converting TRADE MARK SIGN 
		put("&#154;", "\\\u0161");   // converting LATIN SMALL LETTER S WITH CARON 
		put("&#155;", "\\\u203A");   // converting SINGLE RIGHT-POINTING ANGLE QUOTATION  
		put("&#156;", "\\\u0153");   // converting LATIN SMALL LIGATURE OE 
		put("&#158;", "\\\u017E");   // converting LATIN SMALL LETTER Z WITH CARON 
		put("&#159;", "\\u0178");   // converting LATIN CAPITAL LETTER Y WITH DIAERESIS 
	}
	};

	public    static  String convertToUniCode(String caption ) {

    	    for (Map.Entry<String, String> entry : charactersMap.entrySet()) {
	    	caption=caption.replaceAll(entry.getKey(), entry.getValue());
	    }
		return caption;
		
		}
}
