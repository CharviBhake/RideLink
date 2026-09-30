package Smart_Carpooling.demo.Controller;

import Smart_Carpooling.demo.Entity.Ride;
import Smart_Carpooling.demo.Entity.SearchRide;
import Smart_Carpooling.demo.Repository.RideRepository;
import Smart_Carpooling.demo.Service.GeocodingService;
import Smart_Carpooling.demo.Service.NearByRideCacheService;
import Smart_Carpooling.demo.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Controller
@RequestMapping("SearchRides")
public class SearchRidesController {
    @Autowired
    private GeocodingService geocodingService;
    @Autowired
    private RideService rideService;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private NearByRideCacheService nearByRideCacheService;
    @GetMapping("/nearby")
    public ResponseEntity<List<Ride>> getNearbyRides(@RequestParam String address) {
        try {
          
            double[] latLng = rideService.getLatLngFromAddress(address);
            double userLat = latLng[0];
            double userLng = latLng[1];

            List<Ride> nearbyRides = rideService.findRidesWithinRadius(userLat, userLng, 1.0);

            return ResponseEntity.ok(nearbyRides);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    @PostMapping("/search")
    public ResponseEntity<List<Ride>> searchRides(@RequestBody SearchRide req) {
        if (req.getStartLocation() == null || req.getStartLocation().isBlank()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        double[] userStartLatLng;
        try {
            userStartLatLng = rideService.getLatLngFromAddress(req.getStartLocation());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(List.of());
        }
        if (userStartLatLng == null) {
            return ResponseEntity.badRequest().body(List.of());
        }

        System.out.println("USER LAT = " + userStartLatLng[0]);
        System.out.println("USER LNG = " + userStartLatLng[1]);

        LocalDate today = LocalDate.now(ZONE);
        LocalTime now = LocalTime.now(ZONE);

        double radiusKm = 50;

        // date is part of the key so each day gets its own cache entry
        String cacheKey = nearByRideCacheService.buildKey(
                userStartLatLng[0], userStartLatLng[1], radiusKm) + ":" + today;

        List<Ride> cached = nearByRideCacheService.get(cacheKey);
        if (cached != null) {
            System.out.println("Redis cache hit");
            // re-filter on every hit so rides that departed since caching disappear
            return ResponseEntity.ok(
                    cached.stream().filter(r -> isUpcoming(r, today, now)).toList());
        }

        Point userStart = new Point(
                userStartLatLng[1], // lng
                userStartLatLng[0]  // lat
        );
        Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);

        List<Ride> nearbyRides = rideRepository
                .findByStartPointNearAndRideDateGreaterThanEqual(userStart, radius, today);

        System.out.println("REDIS MISS, FOUND IN MONGODB = " + nearbyRides.size());
        nearByRideCacheService.set(cacheKey, nearbyRides);

        return ResponseEntity.ok(
                nearbyRides.stream().filter(r -> isUpcoming(r, today, now)).toList());
    }

    private boolean isUpcoming(Ride r, LocalDate today, LocalTime now) {
        if (r.getRideDate() == null) return false;
        if (r.getAvailableSeats() <= 0) return false;
        // if (r.getStatus() != RideStatus.ACTIVE) return false;  // uncomment, use your enum value

        if (r.getRideDate().isAfter(today)) return true;

        // today's rides: only those that haven't departed yet
        return r.getRideDate().isEqual(today)
                && r.getDepartureTime() != null
                && r.getDepartureTime().isAfter(now);
    }
}
