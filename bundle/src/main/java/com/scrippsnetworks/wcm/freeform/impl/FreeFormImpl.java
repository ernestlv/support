package com.scrippsnetworks.wcm.freeform.impl;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.freeform.FreeForm;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.DialogPropertyNames;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date: 9/19/2013
 */

public class FreeFormImpl implements FreeForm {

	private static final Logger LOG = LoggerFactory.getLogger(FreeFormImpl.class);

    private SniPage sniPage;
    
    private String body;

    private static final String FREE_FORM_RESOURCE_TYPE = "sni-food/components/modules/free-form-text";


    /** Resource for convenience, because you need a Resource from time to time. */
    private Resource resource;


    public FreeFormImpl(final SniPage page) {
        this.sniPage = page;
        resource = page.getContentResource();
    }

    @Override
	public String getBody() {
		if (body == null) {
			if(resource != null) {
				Iterator<Resource> childItr = resource.listChildren();
				if(childItr != null) {
					while (childItr.hasNext()) {
						Resource childRes = childItr.next();
						if (childRes != null && childRes.getResourceType().equals(FREE_FORM_RESOURCE_TYPE)) {
							ValueMap freeFormVm = ResourceUtil.getValueMap(childRes);
							if (freeFormVm != null && freeFormVm.containsKey(DialogPropertyNames.FREE_FORM.dialogPropertyName())) {
								body = freeFormVm.get(DialogPropertyNames.FREE_FORM.dialogPropertyName(), String.class);
								break;
							}
						}
					}
				}
			}
		}
		return body;
	}
}
