package org.infinispan.query.test;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FilterCacheModeType;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.FullTextFilterDefs;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.infinispan.protostream.annotations.ProtoField;

/**
 * @author Navin Surtani
 */
@Indexed(index = "person")
@FullTextFilterDefs({
      @FullTextFilterDef(name = "personFilter", impl = PersonBlurbFilterFactory.class, cache = FilterCacheModeType.INSTANCE_AND_DOCIDSETRESULTS),
      @FullTextFilterDef(name = "personAgeFilter", impl = PersonAgeFilterFactory.class, cache = FilterCacheModeType.INSTANCE_AND_DOCIDSETRESULTS)
})
public class Person implements Serializable {

   @Field(store = Store.YES)
   private String name;

   @Field(store = Store.YES)
   private String blurb;

   @Field(store = Store.YES, analyze = Analyze.NO)
   @SortableField
   private int age;

   @Field(store = Store.YES, analyze = Analyze.NO)
   @DateBridge(resolution = Resolution.DAY)
   private Date dateOfGraduation;

   private String nonIndexedField;

   public Person() {
   }

   public Person(String name, String blurb, int age) {
      this.name = name;
      this.blurb = blurb;
      this.age = age;
      this.nonIndexedField = name != null && name.length() >= 2 ? name.substring(0, 2) : null;
   }

   public Person(String name, String blurb, int age, Date dateOfGraduation) {
      this.name = name;
      this.blurb = blurb;
      this.age = age;
      this.dateOfGraduation = dateOfGraduation;
   }

   @ProtoField(number = 1)
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @ProtoField(number = 2)
   public String getBlurb() {
      return blurb;
   }

   public void setBlurb(String blurb) {
      this.blurb = blurb;
   }

   @ProtoField(number = 3, defaultValue = "0")
   public int getAge() {
      return age;
   }

   public void setAge(int age) {
      this.age = age;
   }

   @ProtoField(number = 4)
   public String getNonIndexedField() {
      return nonIndexedField;
   }

   public void setNonIndexedField(String nonIndexedField) {
      this.nonIndexedField = nonIndexedField;
   }

   @ProtoField(number = 5)
   public Date getDateOfGraduation() {
      return dateOfGraduation;
   }

   public void setDateOfGraduation(Date dateOfGraduation) {
      this.dateOfGraduation = dateOfGraduation;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Person person = (Person) o;
      if (age != person.age) return false;
      if (blurb != null ? !blurb.equals(person.blurb) : person.blurb != null) return false;
      if (name != null ? !name.equals(person.name) : person.name != null) return false;
      if (nonIndexedField != null ? !nonIndexedField.equals(person.nonIndexedField) : person.nonIndexedField != null)
         return false;
      return dateOfGraduation != null ? dateOfGraduation.equals(person.dateOfGraduation) : person.dateOfGraduation == null;
   }

   @Override
   public int hashCode() {
      int result = (name != null ? name.hashCode() : 0);
      result = 31 * result + (blurb != null ? blurb.hashCode() : 0);
      result = 31 * result + (nonIndexedField != null ? nonIndexedField.hashCode() : 0);
      result = 31 * result + (dateOfGraduation != null ? dateOfGraduation.hashCode() : 0);
      result = 31 * result + age;
      return result;
   }

   @Override
   public String toString() {
      return "Person{name='" + name + "', blurb='" + blurb + "', age=" + age +
            ", dateOfGraduation=" + dateOfGraduation + ", nonIndexedField='" + nonIndexedField + "'}";
   }
}
