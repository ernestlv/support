package com.scrippsnetworks.wcm.util

import org.apache.sling.api.resource.Resource
import org.apache.sling.api.resource.ResourceResolver
import org.apache.sling.api.resource.ValueMap
import spock.lang.Specification

/**
 * @author Ken Shih (156223)
 * @created 7/23/13 5:15 PM
 */
class SniSlingUtilSpec extends Specification {

    Resource mockResource
    Resource mockSuperResource
    ResourceResolver mockResourceResolver
    ValueMap mockValueMap

    def setup(){
        mockResource = Mock(Resource)
        mockSuperResource = Mock(Resource)
        mockResourceResolver = Mock(ResourceResolver)
        mockValueMap = Mock(ValueMap)
    }

    //
    // getResourceType specs
    //
    def "try getting resource type with bad path fails"(){
        when:
        mockResourceResolver.getResource(_) >> null
        then:
        null == SniSlingUtil.getResourceWithSlingPath("sni-app/smorf", mockResourceResolver)
        null == SniSlingUtil.getResourceWithSlingPath(null, mockResourceResolver)
    }
    def "try getting resoure type with relative path found in lib"(){
        when:
        mockResourceResolver.getResource("/lib/sni-app/smorf") >> mockResource
        then:
        mockResource == SniSlingUtil.getResourceWithSlingPath("sni-app/smorf", mockResourceResolver)
        mockResource == SniSlingUtil.getResourceWithSlingPath("/lib/sni-app/smorf", mockResourceResolver)
    }
    def "try getting resoure type with relative path found in app"(){
        when:
        mockResourceResolver.getResource("/apps/sni-app/smorf") >> mockResource
        then:
        mockResource == SniSlingUtil.getResourceWithSlingPath("/apps/sni-app/smorf", mockResourceResolver)
        mockResource == SniSlingUtil.getResourceWithSlingPath("sni-app/smorf", mockResourceResolver)
    }

    //
    // isPropertyOnResourceOrAncestor specs
    //
    String PROPERTY_SOUGHT = "sni:property"
    String VALUE_SOUGHT ="component/this/or/that"


    def "check for simple match or non-match"(){
        when:
        mockResourceResolver.getResource("bad url") >> null
        mockResourceResolver.getResource("/good url") >> mockResource
        mockResource.adaptTo(ValueMap.class)>> mockValueMap
        mockValueMap.get(_,_)>>VALUE_SOUGHT
        then:
        false == SniSlingUtil.isPropertyOnResourceOrAncestor("bad url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
        true == SniSlingUtil.isPropertyOnResourceOrAncestor("/good url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
    }

    def "check for simple 2nd-level superResourceType match fails"(){
        when:
        mockResourceResolver.getResource("/good url") >> mockResource
        mockResource.getResourceSuperType()>>"not type sought"
        then:
        false == SniSlingUtil.isPropertyOnResourceOrAncestor("/good url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
    }
    def "check for simple 2nd-level superResource.property match succeeds"(){
        when:
        mockResourceResolver.getResource("/good url") >> mockResource
        mockResource.getResourceSuperType()>>"good/super"
        mockResourceResolver.getResource("/lib/good/super") >> mockSuperResource
        mockSuperResource.adaptTo(ValueMap.class)>> mockValueMap
        mockValueMap.get(PROPERTY_SOUGHT,_)>>VALUE_SOUGHT
        then:
        true == SniSlingUtil.isPropertyOnResourceOrAncestor("/good url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
    }
    def "check for 2nd-level superResource.property match succeeds without superResource"(){
        when:
        mockResourceResolver.getResource("/good url") >> mockResource
        mockResource.getResourceType()>>"good/rcType"
        mockResourceResolver.getResource("/lib/good/rcType") >> mockSuperResource
        mockSuperResource.adaptTo(ValueMap.class)>> mockValueMap
        mockValueMap.get(PROPERTY_SOUGHT,_)>>VALUE_SOUGHT

        then:
        true == SniSlingUtil.isPropertyOnResourceOrAncestor("/good url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
    }
    def "check for simple 2nd-level superResource.property match fails on different property value"(){
        when:
        mockResourceResolver.getResource("/apps/good url") >> mockResource
        mockResource.getResourceSuperType()>>"good/super"
        mockResourceResolver.getResource("/lib/good/super") >> mockSuperResource
        mockResourceResolver.adaptTo(ValueMap.class)>> mockValueMap
        mockValueMap.get(_,_)>>"differnt value"
        then:
        false == SniSlingUtil.isPropertyOnResourceOrAncestor("good url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
    }
    def "check for simple 4th-level superResource.resourceSuperType match succeeds"(){
        when:
        Resource mockSuperResource2 = Mock(Resource)
        Resource mockSuperResource3 = Mock(Resource)

        mockResourceResolver.getResource("/apps/good url") >> mockResource
        mockResource.getResourceType()>>"not type sought"
        mockResource.getResourceSuperType()>>"good/super"
        mockResourceResolver.getResource("/lib/good/super") >> mockSuperResource
        mockSuperResource.getResourceType()>>"not type sought"
        mockSuperResource.getResourceSuperType()>>"good/super2"
        //3rd level
        mockResourceResolver.getResource("/lib/good/super2") >> mockSuperResource2
        mockSuperResource2.getResourceSuperType()>>"good/super3"
        //4th level
        mockResourceResolver.getResource("/apps/good/super3")>> mockSuperResource3
        mockSuperResource3.adaptTo(ValueMap.class)>> mockValueMap
        mockValueMap.get(PROPERTY_SOUGHT,_)>>VALUE_SOUGHT
        then:
        true == SniSlingUtil.isPropertyOnResourceOrAncestor("good url",PROPERTY_SOUGHT,VALUE_SOUGHT,mockResourceResolver)
    }
}
