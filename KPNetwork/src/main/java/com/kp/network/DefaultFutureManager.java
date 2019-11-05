package com.kp.network;

import com.kp.common.data.message.IMessage;
import com.kp.common.log.Loggable;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.event.CacheEntryExpiredListener;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DefaultFutureManager<T extends IMessage> implements FutureManager<String, T>, Loggable {
    long messageTimeout = 20000;
    private Cache<String, Future> futures;

    @Override
    public void add(String id, Future<T> object) throws Exception {
        futures.put(id, object);
    }

    @Override
    public Future<T> get(String id) throws Exception {
        return futures.get(id);
    }

    @Override
    public boolean contains(String id) throws Exception {
        return futures.containsKey(id);
    }

    @Override
    public boolean containsAndRemove(String id) {
        return false;
    }

    @Override
    public Future<T> remove(String id) throws Exception {


        return futures.peekAndRemove(id);

    }

    @Override
    public void destroy() throws Exception {
        if (futures != null) {
            removeAll();
            futures = null;
        }
    }

    @Override
    public void init() throws Exception {
        futures = new Cache2kBuilder<String, Future>() {
        }
                .name("future" + hashCode())
                .expireAfterWrite(messageTimeout + 5000, TimeUnit.MILLISECONDS).disableStatistics(true).keepDataAfterExpired(false)
                .entryCapacity(Integer.MAX_VALUE).addAsyncListener(new CacheEntryExpiredListener<String, Future>() {
                    @Override
                    public void onEntryExpired(Cache<String, Future> cache, CacheEntry<String, Future> cacheEntry) {


                        Future future = cacheEntry.getValue();
                        future.cancel(true);
                        getLogger().error(" future expired key {} ", cacheEntry.getKey());


                    }
                })
                .build();
    }

    @Override
    public void removeAll() throws Exception {

        futures.entries().forEach(stringFutureCacheEntry -> {
            try {

                stringFutureCacheEntry.getValue().cancel(true);
            } catch (Exception e) {
                getLogger().error("removeAll {}", e);
            }
        });
        futures.clear();
    }

    @Override
    public void setMessageTimeout(long messageTimeout) {
        this.messageTimeout = messageTimeout;
    }
}
