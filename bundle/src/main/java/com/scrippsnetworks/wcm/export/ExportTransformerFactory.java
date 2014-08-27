package  com.scrippsnetworks.wcm.export;

import java.io.IOException;
import com.day.cq.wcm.api.WCMMode;
import com.day.text.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.apache.cocoon.xml.sax.AbstractSAXPipe;
import org.apache.cocoon.xml.sax.AttributesImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.scr.annotations.*;

@Component(label="SNI WCM Export Transformer Service", metatype=false)
@Service(value=TransformerFactory.class)
@Properties({
    @Property(name="pipeline.type", value="exportable", propertyPrivate=true)
})
public class ExportTransformerFactory
    implements TransformerFactory
{

    private static final Logger log = LoggerFactory.getLogger(ExportTransformerFactory.class);

    public Transformer createTransformer() {
        log.debug("factory creating ExportTransformer");
        return new ExportTransformer();
    }

    static final class ExportTransformer extends AbstractSAXPipe implements Transformer {

        static final String SELECTOR = "exportable";

        private boolean hasSelector = false;
        private String baseURL;

        public void init(ProcessingContext context, ProcessingComponentConfiguration config)
            throws IOException
        {
            if (WCMMode.fromRequest(context.getRequest()) == WCMMode.DISABLED) {
                String[] selectors = context.getRequest().getRequestPathInfo().getSelectors();
                if (selectors != null) {
                    for (String sel : selectors) {
                        if (SELECTOR.equals(sel)) {
                            hasSelector = true;
                            SlingHttpServletRequest request = context.getRequest();
                            String siteLevel = Text.getAbsoluteParent(request.getRequestPathInfo().getResourcePath(), 1) + "/";
                            String resolverBase = request.getResourceResolver().map(siteLevel);
                            if (resolverBase.startsWith("http")) {
                                baseURL = resolverBase.substring(0, resolverBase.length() - 1);
                            }
                            break;
                        }
                    }
                }
            }
            log.debug("initializing ExportTransformer with hasSelector = {}, baseURL = {}", hasSelector, baseURL);
        }

        public void startElement(String uri, String loc, String raw, Attributes a)
            throws SAXException
        {
            if (hasSelector && baseURL != null) {
                if ("a".equalsIgnoreCase(loc)) {
                    a = replace(a, "href", baseURL);
                } else if ("img".equalsIgnoreCase(loc)) {
                    a = replace(a, "src", baseURL);
                } else if ("form".equalsIgnoreCase(loc)) {
                    a = replace(a, "action", baseURL);
                }
            }

            super.startElement(uri, loc, raw, a);
        }

        public void dispose() {
            // nothing to do here
        }

        private Attributes replace (Attributes a, String attrName, String baseURL) {
            String src = a.getValue(attrName);
            Attributes retVal = a;
            if (src != null && src.charAt(0) == '/') {
                AttributesImpl ai = new AttributesImpl(a);
                ai.updateCDATAAttribute(attrName, baseURL + src);
                retVal = ai;
            }
            return retVal;
        }
    }
}
