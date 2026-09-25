package org.tenacitycodex.renyun.component.redis;

import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.exceptions.CacheException;
import org.tenacitycodex.renyun.common.exceptions.CacheMissedException;

@Component
public interface ICacheable<T, ID> {
    void cache(T object) throws CacheException;
    T getCachedById(ID id) throws CacheMissedException;
    String getKeyPrefix();
}
