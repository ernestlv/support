package com.scrippsnetworks.wcm.rewriter;

/** Contains constant values tied to the RTE link dialog override.
 */
public class AnchorTagRewriterConstants {

    /** Marker attribute the selection is saved to by the RTE.
     *
     * This is not exposed for changing because it's tied to the RTE override implementation.
     */
    public static final String ATTRIBUTE_NAME = "class";


    /** Marker values.
     *
     * These are not exposed for changing because they are tied to the RTE override implementation.
     */
    public enum AnchorAttributeValue {
        VIDEO("video-link"),
        GALLERY("gallery-link");

        private String attributeValue;

        AnchorAttributeValue(String attributeValue) {
            this.attributeValue = attributeValue;
        }

        public String getAttributeValue() {
            return attributeValue;
        }
    }
}
