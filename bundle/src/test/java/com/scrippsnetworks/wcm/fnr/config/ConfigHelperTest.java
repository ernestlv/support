package com.scrippsnetworks.wcm.fnr.config;

import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.config.TemplateConfigService;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;

/**
 * @author Ken Shih (156223)
 * @created 6/11/13 4:02 PM
 */
public class ConfigHelperTest {
    ConfigHelper configHelper;
    @Mock
    TemplateConfigService mockTemplateConfigService;
    @Mock
    SiteConfigService mockSiteConfigService;
    @Mock
    OsgiHelper mockOsgiHelper;

    @Test
    public void getProperty_nullIfPropDoesNotExist(){
        //test
        String out = configHelper.getProperty("testProp");
        //validate
        assertThat(out).isNull();
    }

    @Test
    public void getProperty_fromTCS(){
        //setup
        when(mockTemplateConfigService.getArticleSimpleRegionsPath()).thenReturn("test-return");
        //test
        String out = configHelper.getProperty("articleSimpleRegionsPath");
        //validate
        assertThat(out).isNotEmpty().isEqualTo("test-return");
    }

    @Test
    public void getProperty_fromSCS(){
        //setup
        when(mockSiteConfigService.getAdServerUrl()).thenReturn("test-return");
        //test
        String out = configHelper.getProperty("adServerUrl");
        //validate
        assertThat(out).isNotEmpty().isEqualTo("test-return");
    }

    @Test
    public void getProperty_fromSCSLong(){
        //setup
        when(mockSiteConfigService.getAnimationSpeed()).thenReturn(77L);
        //test
        String out = configHelper.getProperty("animationSpeed");
        //validate
        assertThat(out).isNotEmpty().isEqualTo("77");
    }

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        configHelper = new ConfigHelper();
        configHelper.setOsgiHelper(mockOsgiHelper);
        when(mockOsgiHelper.getOsgiServiceBySite(contains("TemplateConfigService"),anyString())).thenReturn(mockTemplateConfigService);
        when(mockOsgiHelper.getOsgiServiceBySite(contains("SiteConfigService"),anyString())).thenReturn(mockSiteConfigService);

    }
}
