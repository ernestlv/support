package com.scrippsnetworks.wcm.taglib;

import com.day.text.Text;
import java.util.List;
import java.util.ArrayList;

public final class TagUtils {

    private TagUtils() {}

	// Lifted from ImageResource
	public static String completeHREF(String input) {
        String href = input;
		if ( ((href != null) && (href.length() > 0)) &&
                ((href.charAt(0) == '/') || (href.charAt(0) == '#')) ) {
            int anchorPos = href.indexOf('#');
            if (anchorPos == 0) {
                return href;
            }
            if (href.equals("/")) {
                return href;
            }
            String anchor = "";
            if (anchorPos > 0) {
                anchor = href.substring(anchorPos, href.length());
                href = href.substring(0, anchorPos);
            }

            int extSepPos = href.lastIndexOf('.');
            int slashPos = href.lastIndexOf('/');
            if ((extSepPos <= 0) || (extSepPos < slashPos)) {
                href = Text.escape(href, '%', true) + ".html" + anchor;
            }

		}
		return href;
	}


    public static <T> List<List<T>> splitList(List<T> list, int chunks) {

        if (list == null) {
            return new ArrayList<List<T>>();
        }

        int size = list.size();

        if (size == 0 || chunks <= 0) {
            return new ArrayList<List<T>>();
        }

        int chunkSize = (int)Math.ceil((double)size / (double)chunks);
        return chunkList(list, chunkSize);
    }

    public static <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        ArrayList lol = new ArrayList<List<T>>();

        if (list == null) {
            return lol;
        }

        int size = list.size();

        if (size == 0 || chunkSize <= 0) {
            return lol;
        }

        for (int start = 0, end = chunkSize < size ? chunkSize : size;
                start < size;
                start = end, end = start + chunkSize < size ? start + chunkSize : size) {
            lol.add(list.subList(start, end));
        }

        return lol;
    }

}
