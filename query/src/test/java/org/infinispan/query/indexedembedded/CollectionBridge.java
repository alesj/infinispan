package org.infinispan.query.indexedembedded;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

public class CollectionBridge implements TwoWayFieldBridge {
   public Object get(String name, Document document) {
      List<String> list = new ArrayList<>();
      for (IndexableField field : document.getFields(name)) {
         list.add(field.stringValue());
      }
      return list;
   }

   public String objectToString(Object object) {
      return object.toString();
   }

   public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
      Collection collection = Collection.class.cast(value);
      for (Object v : collection) {
         luceneOptions.addFieldToDocument(name, v.toString(), document);
      }
   }
}
