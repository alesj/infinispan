package org.infinispan.query.indexedembedded;

import java.util.Collection;

import org.apache.lucene.search.Query;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.transaction.TransactionMode;
import org.testng.annotations.Test;

/**
 * @author Ales Justin
 */
@Test(groups = "functional", testName = "query.indexedembedded.ProjectionsExampleTest")
public class ProjectionsExampleTest extends SingleCacheManagerTest {

    protected EmbeddedCacheManager createCacheManager() throws Exception {
        ConfigurationBuilder cfg = getDefaultStandaloneCacheConfig(true);
        cfg
            .transaction()
            .transactionMode(TransactionMode.TRANSACTIONAL)
            .indexing()
            .index(Index.ALL)
            .addProperty("default.directory_provider", "ram")
            .addProperty("lucene_version", "LUCENE_CURRENT");
        return TestCacheManagerFactory.createCacheManager(cfg);
    }

    @Test
    public void testUpdate() {
        SearchManager manager = Search.getSearchManager(cache);

        Projection p3 = new Projection("aa", "bb", "cc");
        cache.put("key", p3);

        assertSize(manager, 3);

        Projection p2 = new Projection("aa", "bb");
        cache.put("key", p2);

        assertSize(manager, 2);
    }

    private void assertSize(SearchManager manager, int size) {
        Query query = manager.buildQueryBuilderForClass(Projection.class).get().all().createQuery();
        CacheQuery cacheQuery = manager.getQuery(query, Projection.class);
        cacheQuery.projection("collection");
        Object[] result = (Object[]) cacheQuery.iterator().next();
        int results = Collection.class.cast(result[0]).size();
        assert (size == results);
    }
}
