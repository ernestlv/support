package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.ArrayList;

import com.day.cq.wcm.api.Page;
import com.day.cq.tagging.TagConstants;
import com.scrippsnetworks.wcm.metadata.SponsorshipManager;
import com.scrippsnetworks.wcm.metadata.SponsorshipProvider;
import org.apache.commons.lang.StringUtils;
import java.lang.String;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.IllegalArgumentException;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;
import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import static com.scrippsnetworks.wcm.metadata.MetadataProperty.*;

public class BaseTagMetadataProvider implements MetadataProvider {

    public static final Pattern tagNamespacePattern = Pattern.compile("^[a-z]+-(?:tags|sources|adkeys):(.+)");

    private SniPage page;
    private String[] adKeyArray;
    private String source = null;
    private String contentTag1 = null;
    private String contentTag2 = null;
    private String allTags = null;

    public BaseTagMetadataProvider(SniPage page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }
        this.page = page;

        String adKeyProp = page.getProperties().get(PagePropertyConstants.PROP_SNI_ADKEY, String.class);
        SponsorshipManager sm = page.getSponsorshipManager();
        if (sm != null) {
            SponsorshipProvider provider = sm.getEffectiveSponsorshipProvider();
            if (provider != null) {
                SniPage providerPage = provider.getProvider();
                if (providerPage != null) {
                    String spAdKeyProp = providerPage.getProperties().get(PagePropertyConstants.PROP_SNI_ADKEY, String.class);
                    if (spAdKeyProp != null) {
                        adKeyProp = spAdKeyProp;
                    }
                }
            }
        }

        adKeyArray = getTagStrings(adKeyProp);
        String pTagProp = page.getProperties().get(PagePropertyConstants.PROP_SNI_PRIMARYTAG, String.class);
        contentTag1 = getTagString(pTagProp);
        String sTagProp = page.getProperties().get(PagePropertyConstants.PROP_SNI_SECONDARYTAG, String.class);
        contentTag2 = getTagString(sTagProp);
        String sourceProp = page.getProperties().get(PagePropertyConstants.PROP_SNI_SOURCE, String.class);
        source = getTagString(sourceProp);
        allTags = getAllTags();
    }

    public String getProperty(MetadataProperty prop) {
        if (prop == null) {
            return null;
        }

        String retVal = null;
        switch (prop) {
            case ADKEY1:
                if (adKeyArray.length > 0) {
                    retVal = adKeyArray[0];
                }
                break;
            case ADKEY2:
                if (adKeyArray.length > 1) {
                    retVal = adKeyArray[1];
                }
                break;
            case SOURCE:
                retVal = source;
                break;
            case ALLTAGS:
                retVal = allTags;
                break;
            case CONTENTTAG1:
                retVal = contentTag1;
                break;
            case CONTENTTAG2:
                retVal = contentTag2;
                break;
            default:
                throw new IllegalArgumentException("invalid property");
        }
        return retVal;
    }
    
    public List<MetadataProperty> provides() {
        return Arrays.asList(ADKEY1, ADKEY2, SOURCE, ALLTAGS, CONTENTTAG1, CONTENTTAG2);
    }

    protected String[] getTagStrings(String string) {
        if (string == null || string.isEmpty()) {
            return new String[0];
        }

        String stripped = stripNamespace(string);
        if (stripped != null) {
            return stripped.split("/");
        } else {
            return new String[0];
        }
    }

    protected String getTagString(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        String retVal = null;

        String noNamespace = stripNamespace(string);
        if (noNamespace != null) {
            int lastIndex = noNamespace.lastIndexOf('/');
            if (lastIndex >= 0) {
                retVal = noNamespace.substring(lastIndex + 1);
            } else {
                retVal = noNamespace;
            }
        }

        return retVal;
    }

    protected String stripNamespace(String string) {
        if (string == null) {
            return null;
        }

        String retVal = null;

        Matcher m = tagNamespacePattern.matcher(string);

        if (m.matches()) {
            retVal = m.group(1);
        } else {
            retVal = string;
        }

        return retVal;
    }
    
    protected String getAllTags() {
        ArrayList<String> allTagsList = new ArrayList<String>();
        allTagsList.addAll(Arrays.asList(page.getProperties().get(TagConstants.PN_TAGS, new String[0])));
        for (int i = 0; i < allTagsList.size(); i++) {
            allTagsList.set(i, getTagString(allTagsList.get(i)));
        }
        if (contentTag1 != null) {
            allTagsList.add(contentTag1);
        }
        if (contentTag2 != null) {
            allTagsList.add(contentTag2);
        }
        return StringUtils.join(allTagsList, ",");
    }
}
