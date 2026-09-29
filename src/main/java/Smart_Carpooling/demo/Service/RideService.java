package Smart_Carpooling.demo.Service;

import Smart_Carpooling.demo.Entity.Booking;
import Smart_Carpooling.demo.Entity.CoRiderDTO;
import Smart_Carpooling.demo.Entity.Ride;
import Smart_Carpooling.demo.Entity.User;
import Smart_Carpooling.demo.Repository.BookingRepo;
import Smart_Carpooling.demo.Repository.RideRepository;
import Smart_Carpooling.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RideService {
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private GeocodingService geocodingService;
    @Autowired
    private BookingServic bookingServic;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private UserRepository userRepository;
    public void saveRide(Ride ride){
        rideRepository.save(ride);
    }
    public Optional<Ride> getRide(String rideId){
        return rideRepository.findById(rideId);
    }
    public void deleteById(String rideId){
        rideRepository.deleteById(rideId);
    }
    public List<Ride> getRides(){
        return rideRepository.findAll();
    }
    public double[] getLatLngFromAddress(String address) {
        double[] ans=geocodingService.getCoordinates(address);
        return ans;
    }
    public List<Ride> findRidesWithinRadius(double userLat, double userLng, double radiusKm) {
        List<Ride> allRides = rideRepository.findAll();
        List<Ride> nearbyRides = new ArrayList<>();

        for (Ride ride : allRides) {
            double distance = haversine(userLat, userLng, ride.getStartLatitude(), ride.getStartLongitude());
            if (distance <= radiusKm) {
                nearbyRides.add(ride);
            }
        }
        return nearbyRides;
    }
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

        private static final double EMISSION_FACTOR_KG_PER_KM = 0.12; // avg car emission

    public double calculateCo2(double lat1, double lon1, double lat2, double lon2, int passengersSharing) {
        double dis=haversine(lat1,lon1,lat2,lon2);
        if (passengersSharing<=1) return 0.0;
        double soloEmissions=dis*EMISSION_FACTOR_KG_PER_KM * passengersSharing;
        double sharedEmissions=dis*EMISSION_FACTOR_KG_PER_KM;
        return soloEmissions-sharedEmissions;
    }
    public List<CoRiderDTO> getRecentCoRiders(String userId) {
        List<Booking> userBookings = bookingRepo.findByPassengerId(userId);
        if (userBookings.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> rideIds = userBookings.stream()
                .map(b -> b.getRide().getId())
                .distinct()
                .collect(Collectors.toList());

        List<Booking> allBookingsOnTheseRides = bookingRepo.findByRideIdIn(rideIds);

        Map<String, List<Booking>> bookingsByRideId = allBookingsOnTheseRides.stream()
                .collect(Collectors.groupingBy(b -> b.getRide().getId()));

        Map<String, Integer> tripCountMap = new HashMap<>();

        for (String rideId : rideIds) {
            List<Booking> bookingsOnRide = bookingsByRideId.getOrDefault(rideId, List.of());
            for (Booking booking : bookingsOnRide) {
                String otherUserId = booking.getPassenger().getId();
                if (!otherUserId.equals(userId)) {
                    tripCountMap.merge(otherUserId, 1, Integer::sum);
                }
            }
        }

        if (tripCountMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map.Entry<String, Integer>> sortedEntries = tripCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .collect(Collectors.toList());

        List<String> topCoRiderIds = sortedEntries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<User> users = userRepository.findByIdIn(topCoRiderIds);
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CoRiderDTO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            User user = userMap.get(entry.getKey());
            if (user == null) continue;
            result.add(new CoRiderDTO(user, entry.getValue()));
        }

        return result;
    }
}
