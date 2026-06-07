package com.frauddetection.analyzer.store;

import java.util.List;

public interface RedisStore {
    Long incrementWindowCount(String key, long timestamp, int windowSeconds);
    Boolean isInSet(String setKey, String value);
    void saveLocation(String cardLast4, Double lat, Double lon, long timestamp);
    GeoLocation getLastLocation(String cardLast4);
    List<Double> getRecentAmounts(String key, int limit);
    void addAmount(String key, double amount);
    Long countUniqueCardsForDevice(String key, long now, long windowSeconds);
    void addCardToDevice(String key, String cardLast4, long timestamp, long windowSeconds);
    Long countUniqueMerchantsForCard(String key, long now, long windowSeconds);
    void addMerchantToCard(String key, String merchantId, long timestamp, long windowSeconds);
}