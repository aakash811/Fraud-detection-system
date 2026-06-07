package com.frauddetection.analyzer.rules;

import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.store.GeoLocation;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeolocationRule implements DetectionRule {

    private static final double WEIGHT = 0.20;
    private static final double MAX_SPEED_KMH = 1000.0; // Impossible travel threshold

    private final RedisStore redisStore;

    @Override
    public String getName() {
        return "geolocation";
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleResult evaluate(TransactionEvent event) {
        if (event.getLatitude() == null || event.getLongitude() == null) {
            return RuleResult.of(getName(), 0.0, WEIGHT, "Location not provided");
        }

        GeoLocation currentLocation = redisStore.getLastLocation(event.getCardLast4());
        
        if (currentLocation == null) {
            redisStore.saveLocation(event.getCardLast4(), event.getLatitude(), event.getLongitude(), event.getOccurredAt().toEpochMilli());
            return RuleResult.of(getName(), 0.0, WEIGHT, "First location recorded");
        }

        double distance = haversineDistance(currentLocation.lat(), currentLocation.lon(), 
                                          event.getLatitude(), event.getLongitude());
        double timeDiffHours = (event.getOccurredAt().toEpochMilli() - currentLocation.timestamp()) / (1000.0 * 60.0 * 60.0);
        
        if (timeDiffHours > 0) {
            double speed = distance / timeDiffHours;
            if (speed > MAX_SPEED_KMH) {
                double score = Math.min(1.0, speed / (MAX_SPEED_KMH * 2));
                return RuleResult.of(getName(), score, WEIGHT, 
                    "Impossible travel speed: " + speed + " km/h");
            }
        }

        return RuleResult.of(getName(), 0.0, WEIGHT, "Location travel is normal");
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}