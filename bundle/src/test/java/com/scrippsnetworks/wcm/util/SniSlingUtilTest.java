package com.scrippsnetworks.wcm.util;

import com.scrippsnetworks.wcm.test.mock.jcr.MockSlingRepositoryFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Session;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Shih (156223)
 * @created 7/22/13 1:47 PM
 */
public class SniSlingUtilTest {
    private static final MockSlingRepositoryFactory FACTORY = MockSlingRepositoryFactory.getFactory();

    @Test
    public void isSuperOrResourceType() throws Exception {
        SlingRepository slingRepository = FACTORY.createMockRepository(getClass().getClassLoader().getResourceAsStream("SniSlingUtilTest1.xml"));
        //NodeType[] nodeTypes = CndImporter.registerNodeTypes( new InputStreamReader( new FileInputStream("src/test/resources/all.cnd"))
        Session session = slingRepository.loginAdministrative(null);
        Node pg = session.getNode("/jcr_root/content/food/test-page/jcr:content");
        assertThat(pg.getProperty("jcr:title").getString()).isEqualToIgnoringCase("Sample Article");
        session.logout();
    }

}
