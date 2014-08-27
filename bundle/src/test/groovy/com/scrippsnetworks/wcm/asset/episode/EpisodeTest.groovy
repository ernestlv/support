package com.scrippsnetworks.wcm.asset.episode

import com.scrippsnetworks.wcm.asset.DataUtil
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for Episode class
 * @author Jason Clark
 * Date: 7/31/12
 */
class EpisodeTest extends Specification {

    /*@Unroll
    def "tuneInTimeStamp"() {
        when:
        Map<String, String> dateMap = DataUtil.dateMapFromSchedulePath(schedulePath);
        String results = Episode.tuneInTimeStamp(dateMap)

        then:
        results == expectedResults

        where:
        schedulePath << ["/etc/sni-asset/schedules/food/2012/6/1/12/jcr:content",
                         "/etc/sni-asset/schedules/food/2011/7/11/12/jcr:content"]

        expectedResults << ["June 01, 2012 12:00 PM",
                            "July 11, 2011 12:00 PM"]
    }*/

}
