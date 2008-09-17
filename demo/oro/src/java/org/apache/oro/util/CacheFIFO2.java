/* 
 * $Id: CacheFIFO2.java 124053 2005-01-04 01:24:35Z dfs $
 *
 * Copyright 2000-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oro.util;

import java.util.*;

/**
 * This class is a GenericCache subclass implementing a second
 * chance FIFO (First In First Out) cache replacement policy.  In other
 * words, values are added to the cache until the cache becomes full.
 * Once the cache is full, when a new value is added to the cache, it 
 * replaces the first of the current values in the cache to have been
 * added, unless that value has been used recently (generally
 * between the last cache replacement and now).
 * If the value to be replaced has been used, it is given
 * a second chance, and the next value in the cache is tested for
 * replacement in the same manner.  If all the values are given a
 * second chance, then the original pattern selected for replacement is
 * replaced.
 *
 * @version @version@
 * @since 1.0
 * @see GenericCache
 */
public final class CacheFIFO2 extends GenericCache {
  private int __current = 0;
  private boolean[] __tryAgain;

  /**
   * Creates a CacheFIFO2 instance with a given cache capacity.
   * <p>
   * @param capacity  The capacity of the cache.
   */
  public CacheFIFO2(int capacity) { 
    super(capacity);

    __tryAgain = new boolean[_cache.length];
  }


  /**
   * Same as:
   * <blockquote><pre>
   * CacheFIFO2(GenericCache.DEFAULT_CAPACITY);
   * </pre></blockquote>
   */
  public CacheFIFO2(){
    this(GenericCache.DEFAULT_CAPACITY);
  }


  public synchronized Object getElement(Object key) { 
    Object obj;

    obj = _table.get(key);

    if(obj != null) {
      GenericCacheEntry entry;

      entry = (GenericCacheEntry)obj;

      __tryAgain[entry._index] = true;
      return entry._value;
    }

    return null;
  }


  /**
   * Adds a value to the cache.  If the cache is full, when a new value
   * is added to the cache, it replaces the first of the current values
   * in the cache to have been added (i.e., FIFO2).
   * <p>
   * @param key   The key referencing the value added to the cache.
   * @param value The value to add to the cache.
   */
  public final synchronized void addElement(Object key, Object value) {
    int index;
    Object obj;

    obj = _table.get(key);

    if(obj != null) {
      GenericCacheEntry entry;

      // Just replace the value.  Technically this upsets the FIFO2 ordering,
      // but it's expedient.
      entry = (GenericCacheEntry)obj;
      entry._value = value;
      entry._key   = key;

      // Set the try again value to compensate.
      __tryAgain[entry._index] = true;

      return;
    }

    // If we haven't filled the cache yet, put it at the end.
    if(!isFull()) {
      index = _numEntries;
      ++_numEntries;
    } else {
      // Otherwise, find the next slot that doesn't have a second chance.
      index = __current;
      
      while(__tryAgain[index]) {
	__tryAgain[index] = false;
	if(++index >= __tryAgain.length)
	  index = 0;
      }

      __current = index + 1;
      if(__current >= _cache.length)
	__current = 0;

      _table.remove(_cache[index]._key);
    }

    _cache[index]._value = value;
    _cache[index]._key   = key;
    _table.put(key, _cache[index]);
  }

}

