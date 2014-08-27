package com.scrippsnetworks.wcm.mobile.secondarygrid;

import com.scrippsnetworks.wcm.mobile.imageicons.TransformIconClassToMobile;
import com.scrippsnetworks.wcm.mobile.secondarygrid.impl.SecondaryGridImpl;
import com.scrippsnetworks.wcm.mobile.secondarygrid.impl.SecondaryGridItemImpl;
import com.scrippsnetworks.wcm.mobile.secondarygrid.impl.SecondaryGridTabImpl;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.util.CarouselMobileHelper;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Dzmitry_Drepin on 1/29/14.
 */
public class SecondaryGridItemsFactory {
    private static final String DEFAULT_BTN_TEXT = "More";
    private static final String SECONDARY_GRID_NODE = "secondary_grid";
    //Item props
    private static final String IMAGE_ALT_TEXT_PROP = "imagealttext";//1-3
    private static final String IMAGE_LINK_PROP = "imagelink";//1-3
    private static final String STYLE_PROP = "style";//1-3
    private static final String SUB_HEADER_PROP = "subheader";//1-3
    private static final String IMAGE_COUNT_PROP = "image";//1-3
    private static final String DESCRIPTION_PROP = "description";//1-3
    private static final String IMAGE_REF = "fileReference";
    //Node name
    private static final String SECONDARY_GRID_ROW_COMPONENT = "sec-grid-row-component-";
    //Tab properties
    private static final String TAB_NAME_PROP = "tab";//0-7
    private static final String HEADER_PROP = "header";
    private static final String MORE_LINKS_PROP = "morelink";

    private Resource resource;


    public SecondaryGridItemsFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public SecondaryGrid build() {
        if (resource == null) {
            return null;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);

        SecondaryGrid secondaryGrid = new SecondaryGridImpl();
        if (vm != null) {
            //define base properties
            String header = vm.get(HEADER_PROP, "");
            secondaryGrid.setMoreLink(DEFAULT_BTN_TEXT);
            secondaryGrid.setTitleHead(header);
            String path = resource.getPath();
            int index;

            if(StringUtils.contains(path,SECONDARY_GRID_NODE)){
                index = StringUtils.indexOf(path, SECONDARY_GRID_NODE);
            }else{
                index = StringUtils.indexOf(path, SECONDARY_GRID_NODE.replace("_","-"));
            }

            if (index != -1) {
                String substring = path.substring(0, index);
                secondaryGrid.setDataUrl(substring.trim());

                if (index < path.length()) {
                    String lastPartUrl = path.substring(index, path.length());
                    secondaryGrid.setUrlTemplate(lastPartUrl.trim());
                }
            }

            List<SecondaryGridTab> tabs = secondaryGrid.getItems();
            for (int i = 1; i <= 7; i++) {
                String tabName = vm.get(TAB_NAME_PROP + i, "");

                if (StringUtils.isNotEmpty(tabName)) {
                    SecondaryGridTab tab = new SecondaryGridTabImpl();
                    List<SecondaryGridItem> items = tab.getItems();

                    tab.setTitleTab(tabName);
                    tab.setNumber(i);

                    Resource childComponent = resource.getChild(SECONDARY_GRID_ROW_COMPONENT + i);

                    if (childComponent == null) {
                        continue;
                    }

                    Iterator<Resource> resourceIterator = childComponent.listChildren();

                    int countOfItems;
                    if (resourceIterator.hasNext()) {
                        Resource next = resourceIterator.next();
                        if (next != null) {
                            getItemProperties(items, next);
                        }
                    }

                    countOfItems = items.size();

                    while (resourceIterator.hasNext()) {
                        Resource next = resourceIterator.next();
                        if (next != null) {
                            ValueMap valueMap = next.adaptTo(ValueMap.class);
                            if (valueMap != null) {
                                for (int k = 1; k < 4; k++) {
                                    String imagePath=null;
                                    String subheader = valueMap.get(SUB_HEADER_PROP + k, "");
                                    String description = valueMap.get(DESCRIPTION_PROP + k, "");

                                    Resource child = next.getChild(IMAGE_COUNT_PROP + k);

                                    if (child != null) {
                                        ValueMap imageMap = child.adaptTo(ValueMap.class);
                                        if (imageMap != null) {
                                            imagePath = imageMap.get(IMAGE_REF, "");
                                        }
                                    }

                                    if (!(StringUtils.isEmpty(subheader) && StringUtils.isEmpty(description) && !CarouselMobileHelper.isValidDamImagePath(resource.getResourceResolver(),imagePath))) {
                                        countOfItems++;
                                    }
                                }
                            }
                        }
                    }

                    tab.setSize(countOfItems - 1);

                    if (items.size() > 0) {
                        tab.setShowButton(countOfItems != tab.getItems().size());
                        tabs.add(tab);
                    }
                }
            }

            if (tabs.size() == 1) {
                secondaryGrid.setShowTabs(false);
            } else if (tabs.isEmpty()) {
                secondaryGrid = null;
            } else {
                for (SecondaryGridTab tab : tabs) {
                    if (StringUtils.containsOnly(tab.getTitleTab(), " ")) {
                        tab.setTitleTab(tab.getTitleTab().trim());
                    }
                }
            }
        }
        return secondaryGrid;
    }

    public List<SecondaryGridItem> buildItems(int tabIndex, int startFrom) {
        if (resource == null) {
            return null;
        }

        List<SecondaryGridItem> items = new ArrayList<SecondaryGridItem>();

        Resource childComponent = null;
        Iterator<Resource> iterator = resource.listChildren();

        while(iterator.hasNext()){
            childComponent = iterator.next();
            if(StringUtils.containsIgnoreCase(childComponent.getPath(),String.valueOf(tabIndex))){
                break;
            }
        }

        if (childComponent != null && startFrom >= 0) {
            Iterator<Resource> resourceIterator = childComponent.listChildren();
            int index = (startFrom - 3) / 3;
            if (index <= 0) {
                index = 0;
            }
            int counter = 0;
            while (resourceIterator.hasNext()) {
                Resource next = resourceIterator.next();
                counter++;
                if (next != null) {
                    if (counter == index + 2) {
                        items = getItemProperties(items, next);
                    }
                }
            }
        }
        return items;
    }

    private List<SecondaryGridItem> getItemProperties(List<SecondaryGridItem> items, Resource resource) {
        ValueMap valueMap = resource.adaptTo(ValueMap.class);
        if (valueMap != null) {
            for (int k = 1; k < 4; k++) {
                String altText = valueMap.get(IMAGE_ALT_TEXT_PROP + k, "");
                String imageLInk = valueMap.get(IMAGE_LINK_PROP + k, "");
                String style = valueMap.get(STYLE_PROP + k, "");
                String subheader = valueMap.get(SUB_HEADER_PROP + k, "");
                String description = valueMap.get(DESCRIPTION_PROP + k, "");

                //imageLInk = CarouselMobileHelper.getLink(imageLInk,resource.getResourceResolver(), subheader, description);
                subheader = StringUtil.cleanToPlainText(subheader);
                description = StringUtil.cleanToPlainText(description);

                Resource child = resource.getChild(IMAGE_COUNT_PROP + k);
                SecondaryGridItem secondaryGridItem = new SecondaryGridItemImpl().setTitle(subheader).setAltText(altText).
                        setUrl(imageLInk).setCssClassName(TransformIconClassToMobile.modify(style)).setDescription(description);

                if (child != null) {
                    ValueMap imageMap = child.adaptTo(ValueMap.class);
                    if (imageMap != null) {
                        String ref = imageMap.get(IMAGE_REF, "");
                        secondaryGridItem.setImageDamPath(ref);
                    }
                }

                if (!(StringUtils.isEmpty(subheader) && StringUtils.isEmpty(description) && !CarouselMobileHelper.isValidDamImagePath(resource.getResourceResolver(), secondaryGridItem.getImageDamPath()))) {
                    items.add(secondaryGridItem);
                }
            }
        }
        return items;
    }
}
