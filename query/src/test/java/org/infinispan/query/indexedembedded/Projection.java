package org.infinispan.query.indexedembedded;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Indexed
public class Projection implements Serializable {

   @Field(bridge = @FieldBridge(impl = CollectionBridge.class), store = Store.YES)
   Collection<String> collection;

   public Projection(String... array) {
      this.collection = Arrays.asList(array);
   }

}
