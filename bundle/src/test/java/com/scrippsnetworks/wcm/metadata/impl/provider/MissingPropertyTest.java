package com.scrippsnetworks.wcm.metadata.impl.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.scrippsnetworks.wcm.metadata.MetadataProperty;
import com.scrippsnetworks.wcm.metadata.MetadataProvider;

public class MissingPropertyTest {

    @Mock BaseContentMetadataProvider baseProvider;
    @Mock BaseLocationMetadataProvider locationProvider;
    @Mock RecipeMetadataProvider recipeProvider;
    @Mock BaseSearchMetadataProvider searchProvider;
    @Mock BaseSponsorshipMetadataProvider sponsorshipProvider;
    @Mock BaseTagMetadataProvider tagProvider;
    @Mock CompanyMetadataProvider companyProvider;

    @Test
    public void test() {
        MockitoAnnotations.initMocks(this);
        List<MetadataProvider> providers = Arrays.asList(baseProvider, locationProvider, recipeProvider,
            searchProvider, sponsorshipProvider, tagProvider, companyProvider);
        List<MetadataProperty> provided = new ArrayList<MetadataProperty>();
        for (MetadataProvider provider : providers) {
            Mockito.when(provider.provides()).thenCallRealMethod();
            provided.addAll(provider.provides());
        }
        EnumSet<MetadataProperty> missingProperties = EnumSet.complementOf(EnumSet.copyOf(provided));

        // USERID property is added to the mdManager by the client. Do not expect a provider to return it.
        missingProperties.remove(MetadataProperty.USERID);

        Assert.assertThat(
                "The following MetadataProperties are missing providers: " + missingProperties, missingProperties.size(),
                Matchers.is(0));
    }

}
