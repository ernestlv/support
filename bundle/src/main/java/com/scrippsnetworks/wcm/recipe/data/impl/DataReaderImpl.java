package com.scrippsnetworks.wcm.recipe.data.impl;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.data.DataBlockTypes;
import com.scrippsnetworks.wcm.recipe.data.DataReader;
import com.scrippsnetworks.wcm.util.ResourceRankOrderComparator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.*;

/**
 * Implementation of Recipe data block reader.
 * @author Jason Clark
 *         Date: 6/12/13
 */
public class DataReaderImpl implements DataReader {

    /** The list of blocks we found, scrubbed and sorted. */
    private List<Resource> sortedBlocks;

    /** The text of the recipe block(s). */
    private String text;

    /**
     * Core logic for building and sorting blocks of text from recipe assets.
     * @param sniPage SniPage for recipe content page.
     * @param blockTypes Enum values for blocks you want to retrieve.
     */
    public DataReaderImpl(final SniPage sniPage, final List<DataBlockTypes> blockTypes) {
        Resource blocksResource = sniPage.getContentResource().getChild(NODE_NAME_BLOCKS);
        if (blocksResource != null) {
            List<Resource> foundBlocks = new ArrayList<Resource>();
            for (DataBlockTypes type : blockTypes) {
                Iterator<Resource> blocksIterator = blocksResource.listChildren();
                while (blocksIterator.hasNext()) {
                    Resource block = blocksIterator.next();
                    ValueMap blockProps = block.adaptTo(ValueMap.class);
                    if (blockProps != null
                            && blockProps.containsKey(JCR_TITLE)
                            && blockTitleMatches(blockProps.get(JCR_TITLE, String.class), type)) {
                        foundBlocks.add(block);
                    }
                }
            }

            /* de-dupe crosslinked blocks here. also, if a resource has no rank order, fuggitaboutit */
            Map<String, Resource> deDupeMap = new HashMap<String, Resource>();
            for (Resource block : foundBlocks) {
                ValueMap blockProps = block.adaptTo(ValueMap.class);
                if (blockProps.containsKey(RANK_ORDER)) {
                    String rankOrder = blockProps.get(RANK_ORDER, String.class);
                    if (deDupeMap.containsKey(rankOrder)) {
                        if (blockProps.containsKey(JCR_TITLE)) {
                            String blockTitle = blockProps.get(JCR_TITLE, String.class);
                            if (blockTitle.toLowerCase().matches("^" + XLINKED + ".*")) {
                                deDupeMap.put(rankOrder, block);
                            }
                        }
                    } else {
                        deDupeMap.put(rankOrder, block);
                    }
                }
            }

            sortedBlocks = new ArrayList<Resource>(deDupeMap.values());

            if (sortedBlocks.size() > 0) {
                Collections.sort(sortedBlocks, new ResourceRankOrderComparator());
            }
        }
    }

    /** Convenience method for checking block titles. */
    private boolean blockTitleMatches(String blockTitle, DataBlockTypes type) {
        if (type == DataBlockTypes.OTHER) {
            /* This is handled differently, since we're looking for titles that don't match the knowns */
            for (DataBlockTypes knownType : DataBlockTypes.KNOWN_TYPES) {
                if (blockTitle.equalsIgnoreCase(knownType.title())
                        || blockTitle.equalsIgnoreCase(knownType.crosslinkedTitle())) {
                    return false;
                }
            }
            return true;
        } else {
            /* This is a straight up match against the known type titles */
            if (blockTitle.equalsIgnoreCase(type.title())
                    || blockTitle.equalsIgnoreCase(type.crosslinkedTitle())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve the text from the recipe block(s). Can return null.
     * @return String aggregated text from requested recipe blocks.
     */
    public String toString() {
        if (text == null && sortedBlocks.size() > 0) {
            StringBuilder blockBuilder = new StringBuilder();
            for (Resource block : sortedBlocks) {
                ValueMap blockProps = block.adaptTo(ValueMap.class);
                if (blockProps.containsKey(RECIPE_BODY)) {
                    blockBuilder.append(blockProps.get(RECIPE_BODY, String.class));
                }
            }
            text = blockBuilder.toString();
        }
        return text == null ? "" : text;
    }

    /** Sorted Resources from data blocks. */
    public List<Resource> getSortedBlocks() {
        return sortedBlocks;
    }
}
