/* 
 * $Id: CacheRandom.java 124053 2005-01-04 01:24:35Z dfs $
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
 * This class is a GenericCache subclass implementing a random
 * cache replacement policy.  In other words,
 * values are added to the cache until it becomes full.  Once the
 * cache is full, when a new value is added to the cache, it replaces
 * a randomly selected value in the cache.
 *
 * @version @version@
 * @since 1.0
 * @see GenericCache
 */
public final class CacheRandom extends GenericCache {
  private Random __random;

  /**
   * Creates a CacheRandom instance with a given cache capacity.
   * <p>
   * @param capacity  The capacity of the cache.
   */
  public CacheRandom(int capacity) { 
    super(capacity);
    __random = new Random(System.currentTimeMillis());
  }

  /**
   * Same as:
   * <blockquote><pre>
   * CacheRandom(GenericCache.DEFAULT_CAPACITY);
   * </pre></blockquote>
   */
  public CacheRandom(){
    this(GenericCache.DEFAULT_CAPACITY);
  }

  /**
   * Adds a value to the cache.  If the cache is full, when a new value
   * is added to the cache, it replaces the first of the current values
   * in the cache to have been added (i.e., Random).
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

      // Just replace the value.
      entry = (GenericCacheEntry)obj;
      entry._value = value;
      entry._key   = key;

      return;
    }

    // Expression is not in cache.

    // If we haven't filled the cache yet, put it at the end.
    if(!isFull()) {
      index = _numEntries;
      ++_numEntries;
    } else {
      // Otherwise, replace a random entry.
      index = (int)(_cache.length*__random.nextFloat());
      _table.remove(_cache[index]._key);
    }

    _cache[index]._value = value;
    _cache[index]._key   = key;
    _table.put(key, _cache[index]);
  }
}
