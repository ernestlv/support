package com.scrippsnetworks.wcm.fnr.config;

import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.config.TemplateConfigService;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * intended to expose {@link com.scrippsnetworks.wcm.config.SiteConfigService} and
 * {@link com.scrippsnetworks.wcm.config.TemplateConfigService} config
 *
 * This class currently makes the arbitrary choice of giving precedence of TemplateConfigService over SiteConfigService,
 * in order to not expose the user to their difference. Will only access property if there is an accessor
 *
 * from a taglib function
 *
 * TODO Disadvantage: dynamic change in config is not captured here; below lazy load-and-keep perfomance advantage
 * TODO is chosen instead of dynamism
 *
 * @author Ken Shih (156223)
 * @created 6/11/13 3:08 PM
 */
public class ConfigHelper {
    private static ConfigHelper singletonTaglibConfigHelper = new ConfigHelper();
    private OsgiHelper osgiHelper;
    TemplateConfigService templateConfigService;
    SiteConfigService siteConfigService;

    private static final Logger LOG = LoggerFactory.getLogger(ConfigHelper.class);

    public static String taglibGetProperty(String propertyName){
        return singletonTaglibConfigHelper.getProperty(propertyName);
    }

    public String getProperty(String propertyName){
        //lazy load config service
        if(templateConfigService==null){
            templateConfigService = getOsgiHelper().getOsgiServiceBySite(TemplateConfigService.class.getName(),"food");
        }
        String out = callAccessorOnConfigService(propertyName,templateConfigService);
        if(out !=null){
            return out;
        }
        //lazy load config service
        if(siteConfigService==null){
            siteConfigService = getOsgiHelper().getOsgiServiceBySite(SiteConfigService.class.getName(),"food");
        }
        return callAccessorOnConfigService(propertyName,siteConfigService);
    }

    /**
     * @param propertyName TODO doesn't handle boolean properties yet
     * @param configService like TemplateConfigService and SiteConfigService
     * @return null if not found, otherwise returns property as string
     */
    private String callAccessorOnConfigService(String propertyName, Object configService){
        //construct getter method name from propertyName
        String getterName = "get"
                +propertyName.substring(0,1).toUpperCase(Locale.US)
                +propertyName.substring(1);
        try {
            Method m = configService.getClass().getMethod(getterName, new Class[]{});
            Object o = m.invoke(configService, new Object[]{});
            return o==null?null:o.toString();
        } catch (NoSuchMethodException e){
            //swallowing. this is expected
//            LOG.error("error with non-existent accessor, " +
//                    "swallowing error because can return null to caller, attempted to get: " +
//                    getterName +
//                    "on class "+
//                    configService,e);
        } catch (InvocationTargetException e) {
            LOG.error("error when calling accessor, " +
                    "swallowing error because can return null to caller, attempted to get: " +
                    getterName +
                    "on class "+
                    configService,e);
        } catch (IllegalAccessException e) {
            LOG.error("error when calling accessor, " +
                    "swallowing error because can return null to caller, attempted to get: " +
                    getterName +
                    "on class "+
                    configService,e);
        }
        return null;
    }

    /**
     * @return osgiHelper, could be mock
     */
    protected OsgiHelper getOsgiHelper() {
        if(osgiHelper==null)
            osgiHelper = new OsgiHelper();
        return osgiHelper;
    }

    /**
     * @param osgiHelper used by unit test
     */
    protected void setOsgiHelper(OsgiHelper osgiHelper) {
        this.osgiHelper = osgiHelper;
    }
}
