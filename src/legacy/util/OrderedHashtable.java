package legacy.util;

/*
* The contents of this file are subject to the BT "ZEUS" Open Source 
* Licence (L77741), Version 1.0 (the "Licence"); you may not use this file 
* except in compliance with the Licence. You may obtain a copy of the Licence
* from $ZEUS_INSTALL/licence.html or alternatively from
* http://www.labs.bt.com/projects/agents/zeus/licence.htm
* 
* Except as stated in Clause 7 of the Licence, software distributed under the
* Licence is distributed WITHOUT WARRANTY OF ANY KIND, either express or 
* implied. See the Licence for the specific language governing rights and 
* limitations under the Licence.
* 
* The Original Code is within the package zeus.*.
* The Initial Developer of the Original Code is British Telecommunications
* public limited company, whose registered office is at 81 Newgate Street, 
* London, EC1A 7AJ, England. Portions created by British Telecommunications 
* public limited company are Copyright 1996-9. All Rights Reserved.
* 
* THIS NOTICE MUST BE INCLUDED ON ANY COPY OF THIS FILE
*/



import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class OrderedHashtable <K,V> extends Hashtable <K,V> {
   protected Vector<K> keys = new Vector<K>();

   public OrderedHashtable() {
      super();
   }
   public OrderedHashtable(int capacity) {
      super(capacity);
   }
   public OrderedHashtable(int capacity, float loadFactor) {
      super(capacity, loadFactor);
   }
   public synchronized Object getKeyAt(int position) {
      return keys.elementAt(position);
   }
   public synchronized Enumeration keys() {
      return keys.elements();
   }
   public synchronized Enumeration elements() {
      Vector data = new Vector();
      for(int i = 0; i < keys.size(); i++ )
         data.addElement(this.get(keys.elementAt(i)));
      return data.elements();
   }
   public synchronized V put(K key, V value) {
      V result = super.put(key,value);
      if ( result == null )
         keys.addElement(key);
      return result;
   }

   public synchronized V remove(Object key) {
      keys.removeElement(key);
      return super.remove(key);
   }

   public synchronized void clear() {
      keys.removeAllElements();
      super.clear();
   }
   public synchronized Object clone() {
      return null;
   }
   public synchronized void reKey(K previousKey, K currentKey,
                                  V value) {
      int index = keys.indexOf(previousKey);
      keys.setElementAt(currentKey,index);
      super.remove(previousKey);
      super.put(currentKey,value);
   }
 //----------------16.11.98---GO---------------------------------------
   public synchronized Object getObjectAt(int index){
        return keys.elementAt(index);
   }   
}