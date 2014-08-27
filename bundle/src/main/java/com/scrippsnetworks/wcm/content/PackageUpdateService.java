package com.scrippsnetworks.wcm.content;

import java.util.List;

import org.apache.sling.api.resource.Resource;

import com.scrippsnetworks.wcm.content.model.ContentPageBean;

public interface PackageUpdateService {
	public List<ContentPageBean> getContentPagesFromSponsorshipCode(Resource resource,String sponsorshipCode, String rootPath);
}
