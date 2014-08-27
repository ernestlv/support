package com.scrippsnetworks.wcm.asset

import spock.lang.Specification
import spock.lang.Unroll
import org.apache.sling.commons.testing.sling.MockResource
import org.apache.sling.commons.testing.sling.MockResourceResolver

/**
 * Spock unit tests for DataUtil class
 * @author Jason Clark
 * Date: 7/21/12
 */
class DataUtilGTest extends Specification {
/*
    @Unroll
    def "dateMapFromSchedulePath( #path ) returns year=#year, month=#month, day=#day, time=#time"() {
        when:
        Map<String, String> results = DataUtil.dateMapFromSchedulePath(path)

        then:
        results.get("year")  == year
        results.get("month") == month
        results.get("day")   == day
        results.get("time")  == time

        where:
        path  << ["/etc/sni-asset/schedules/cook/2012/6/1/12/jcr:content",
                  "http://fooness:4502/barness/bazness/etc/sni-asset/schedules/cook/2011/1/2/1/jcr:content"]
        year  << ["2012", "2011"]
        month << ["06", "01"]
        day   << ["01", "02"]
        time  << ["12:00", "6:30"]
    }
*/
/*
    @Unroll
    def "showPageUrlFromShowAssetPath"() {
        when:
        String results = DataUtil.showPageUrlFromShowAssetPath(path)

        then:
        results == expectedValue

        where:
        path << ["/etc/sni-asset/shows/foo", "http://foo.com/blah/etc/sni-asset/shows/baz"]
        expectedValue << ["/content/cook/shows/foo.html", "http://foo.com/blah/content/cook/shows/baz"]
    }
*/
/*
    @Unroll
    def "convertMilTimeToStandard" () {
        when:
        Map<String, String> results = DataUtil.convertMilTimeToStandard(value)

        then:
        results.get("time") == time
        results.get("period") == period

        where:
        time << ["9:00", "12:30", "2:00", "12:00", "12:30", "11:00"]
        period << ["AM", "PM", "PM", "AM", "AM", "PM"]
        value << ["9:00", "12:30", "14:00", "00:00", "00:30", "23:00"]
    }
*/
/*
    @Unroll
    def "findSchedulesFromTodayForward"() {
        given:
        MockResourceResolver mockResourceResolver = new MockResourceResolver();
        MockResource mockResource = new MockResource(mockResourceResolver,
                "/content/foo/programguidedaily",
                "sni-wcm/components/pagetypes/program-guide-daily")

        when:
        List<Map<String, Object>> results = DataUtil.findSchedulesFromTodayForward(mockResource)

        then:
        results != null
    }
*/
/*
    @Unroll
    def "sectionNameFromContentPath"() {
        when:
        String result = DataUtil.sectionNameFromContentPath(path)

        then:
        result == expected

        where:
        path << ["/content/cook/foo/some/stuff",
                 "/content/cook/baz",
                 "/content/cook",
                 "/content/cook-mobile/foo/bar/baz",
                 "/content/cook-mobile/baz",
                 "/content/cook-mobile/section/something"]
        expected << ["foo", "home", null, "foo", "home", "section"]
    }
*/
/*
    @Unroll
    def "sectionNameFromResource"() {
        given:
        MockResourceResolver mockResourceResolver = new MockResourceResolver()
        MockResource mockResource1 = new MockResource(mockResourceResolver,
                "/content/cook/section/programguidedaily",
                "sni-wcm/components/pagetypes/program-guide-daily")
        MockResource mockResource2 = new MockResource(mockResourceResolver,
                "/content/cook/section/foo-section",
                "sni-wcm/components/pagetypes/foo-section")
        MockResource mockResource3 = new MockResource(mockResourceResolver,
                "/content/cook/homearticle",
                "sni-wcm/components/pagetypes/article-simple")
        MockResource mockResource4 = new MockResource(mockResourceResolver,
                "/content/cook/homepage",
                "sni-wcm/components/pagetypes/cook-home-section")

        when:
        String result1 = DataUtil.sectionNameFromResource(mockResource1)
        String result2 = DataUtil.sectionNameFromResource(mockResource2)
        String result3 = DataUtil.sectionNameFromResource(mockResource3)
        String result4 = DataUtil.sectionNameFromResource(mockResource4)

        then:
        result1 == "section"
        result2 == "foo"
        result3 == "home"
        result4 == "home"
    }
*/
/*
    @Unroll
    def "timeCodeFromCalendar"() {
        given:
        Calendar calendar = Calendar.getInstance()

        when:
        calendar.set(2012, 7, 16, hour, min)
        Integer result = DataUtil.timeCodeFromCalendar(calendar)

        then:
        result == expected

        where:
        hour << [11, 12, 0, 9, 9, 23, 6]
        min << [45, 0, 15, 8, 30, 50, 14]
        expected << [12, 12, 37, 6, 7, 36, 48]
    }
*/
/*
    @Unroll
    def "timeStampFromCalendar"() {
        given:
        Calendar calendar = Calendar.getInstance()

        when:
        calendar.set(2012, 7, 16, hour, min, sec)
        String result = DataUtil.timeStampFromCalendar(calendar)

        then:
        result == expected

        where:
        hour << [9, 12, 23]
        min  << [30, 0, 59]
        sec  << [0, 15, 59]
        expected << ["09:30:00", "12:00:15", "23:59:59"]
    }
*/
/*
    @Unroll
    def "personPagePathFromAssetPath"() {
        when:
        String result = DataUtil.personPagePathFromAssetPath(assetPath)

        then:
        result == expected

        where:
        assetPath << ["/etc/sni-asset/people/chef/dave-lieberman"]
        expected << ["/content/cook/chefs/dave-lieberman.html"]
    }
*/
/*
    @Unroll
    def "schedulePathFromCalendar"() {
        given:
        Calendar calendar = Calendar.getInstance()

        when:
        calendar.set(2012, 7, 16, hour, min)
        String result = DataUtil.schedulePathFromCalendar(calendar)

        then:
        result == expected

        where:
        hour << [12]
        min  << [30]
        expected << ["/etc/sni-asset/schedules/cook/2012/8/16"]
    }
*/

/*    @Unroll
    def "termFromTag"() {
        when:
        String result = DataUtil.termFromTag(tag)

        then:
        result == expected

        where:
        tag << ["cook-tag:main-ingredient/chicken-feet",
                "cook-tags:main-ingredient/vegetables/squash",
                "cook-tags:foo",
                "cook-tags:blah"]
        expected << ["chicken-feet",
                     "squash",
                     "foo",
                     "blah"]
    }
*/
/*
    @Unroll
    def "prettyPrintTagTerm"() {
        when:
        String result = DataUtil.prettyPrintTagTerm(tag)

        then:
        result == expected

        where:
        tag << ["cook-tag:main-ingredient/chicken-feet",
                "cook-tags:main-ingredient/vegetables/squash",
                "cook-tags:foo",
                "cook-tags:blah"]
        expected << ["Chicken Feet",
                     "Squash",
                     "Foo",
                     "Blah"]
    }
    */
}
