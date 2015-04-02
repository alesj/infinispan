package org.infinispan.query.indexedembedded;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class CollectionBridge implements FieldBridge {
   public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
      Collection collection = Collection.class.cast(value);
      for (Object v : collection) {
         luceneOptions.addFieldToDocument(name, v.toString(), document);
      }
   }
}
