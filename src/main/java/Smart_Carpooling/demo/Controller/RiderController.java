package Smart_Carpooling.demo.Controller;

import Smart_Carpooling.demo.Entity.*;
import Smart_Carpooling.demo.Repository.BookingRepo;
import Smart_Carpooling.demo.Repository.RideRepository;
import Smart_Carpooling.demo.Service.ChatRoomService;
import Smart_Carpooling.demo.Service.DistanceUtil;
import Smart_Carpooling.demo.Service.RideService;
import Smart_Carpooling.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

@CrossOrigin(origins = {"http://localhost:3000", "https://ride-link-frontend.vercel.app"})
@Controller
@RequestMapping("/ride")
public class RiderController {
    @Autowired
    private RideService rideService;
    @Autowired
    private UserService userService;
    @Autowired
    private ChatRoomService chatService;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private DistanceUtil distanceUtil;
    @Autowired
    private BookingRepo bookingRepo;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point startPoint;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point endPoint;

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides(){
        List<Ride> rides=rideService.getRides();
        if(rides.size()!=0) {
            System.out.println(rides);
            return new ResponseEntity<>(rides, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GetMapping("{rideId}")
    public ResponseEntity<Ride> getRideWithID(@PathVariable String rideId){
        return rideService.getRide(rideId)
                .map(ride -> new ResponseEntity<>(ride, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
   /*
    @PutMapping("/add")
    public ResponseEntity<String> addRide(@RequestBody Ride ride) {
        System.out.println("ADD RIDE CONTROLLER HIT");
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User userInDb = userService.findByUsername(username);
        if (userInDb == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }
        double[] latLngStart =
                rideService.getLatLngFromAddress(ride.getStartlocation());
        double[] latLngEnd =
                rideService.getLatLngFromAddress(ride.getEndLocation());
        Ride rideToSave = Ride.builder()
                .driver(userInDb)                  // ✅ set driver
                .availableSeats(ride.getAvailableSeats())
                .rideDate(ride.getRideDate())
                .departureTime(ride.getDepartureTime())
                .pricePerSeat(ride.getPricePerSeat())
                .startlocation(ride.getStartlocation())
                .endLocation(ride.getEndLocation())
                .status(ride.getStatus())
                .startLatitude(latLngStart[0])
                .startLongitude(latLngStart[1])
                .endLatitude(latLngEnd[0])
                .endLongitude(latLngEnd[1])
                .bookings(new ArrayList<>())
                .build();
        rideService.saveRide(rideToSave);   // ✅ ONLY save ride
        return ResponseEntity.ok("Ride added successfully");
    }
*/
    @PutMapping("/add")
    public ResponseEntity<?> addRide(@RequestBody AddRideRequest req) {
        System.out.println("API is HT");
        boolean lock=false;
         if(lock==false)  {
             lock=true;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findById(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            double[] startLatLng = rideService.getLatLngFromAddress(req.getStartLocation());
            double[] endLatLng = rideService.getLatLngFromAddress(req.getEndLocation());

            Ride ride = Ride.builder()
                    .driver(user)
                    .startlocation(req.getStartLocation())
                    .endLocation(req.getEndLocation())
                    // numeric (optional but useful)
                    .startLatitude(startLatLng[0])
                    .startLongitude(startLatLng[1])
                    .endLatitude(endLatLng[0])
                    .endLongitude(endLatLng[1])
                    // geo points (IMPORTANT)
                    .startPoint(new Point(startLatLng[1], startLatLng[0])) // lng, lat
                    .endPoint(new Point(endLatLng[1], endLatLng[0]))
                    .rideDate(req.getRideDate())
                    .departureTime(req.getDepartureTime())
                    .availableSeats(req.getAvailableSeats())
                    .pricePerSeat(req.getPricePerSeat())
                    .status(RideStatus.AVAILABLE)
                    .distance(distanceUtil.calculateDistance(startLatLng[0], startLatLng[1], endLatLng[0], endLatLng[1]))
                    .build();

            rideService.saveRide(ride);
            user.setTotalRides(user.getTotalRides() + 1);
            userService.updateUser(user);
            lock=false;
        }
        return ResponseEntity.ok(Map.of("message","Ride added successfully"));
    }

    @PutMapping("/{RideId}/update")
    public ResponseEntity<Ride> updateRide(@RequestBody Ride ride, @PathVariable String RideId){
        Optional<Ride> ride1=rideService.getRide(RideId);
        Ride ride2=ride1.orElse(null);
        System.out.println(ride2);
        if(ride2!=null){
            ride2.setAvailableSeats(ride.getAvailableSeats());
            ride2.setStatus(ride.getStatus());
            ride2.setDepartureTime(ride.getDepartureTime());
            ride2.setPricePerSeat(ride.getPricePerSeat());
            rideService.saveRide(ride2);
            return new ResponseEntity<>(ride2,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @DeleteMapping("/{rideId}/delete")
    public ResponseEntity<?> deleteRide(@PathVariable String rideId ){
       Optional<Ride> ride=rideService.getRide(rideId);
       Ride ride1=ride.orElse(null);
       if(ride1!=null){
           rideService.deleteById(rideId);
           return new ResponseEntity<>(HttpStatus.OK);
       }
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
 /*   @GetMapping("/{rideId}/users")
    public ResponseEntity<List<User>> findConnectedUsers(@PathVariable String rideId){
        return ResponseEntity.ok(chatService.getUsersInRide(rideId));
    }*/

    @GetMapping("/getList")
    public ResponseEntity<List<Ride>> getListOfRidesForUser() {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // email in your case
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Ride> rides = rideRepository.findByDriverId(user.getId());
        List<Ride> ans=rides.stream().filter(r->r.getRideDate().isBefore(LocalDate.now())).toList();
        return ResponseEntity.ok(ans);
    }

    @GetMapping("/history/driver")
    public ResponseEntity<List<Ride>> getRideHistory(){
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // email in your case
        User user = userService.findById(userId);
        List<Ride> history=rideRepository.findByDriverId(user.getId());
        List<Ride> history1= history.stream().filter(h->h.getRideDate().isBefore(LocalDate.now())).toList();
        return ResponseEntity.ok(history1);
    }

    @GetMapping("/history/passenger")
    public ResponseEntity<List<Ride>> getRidePassengerHistory(){
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // email in your case
        User user = userService.findById(userId);
        List<Booking> bookings=bookingRepo.findByPassengerId(user.getId());
        List<Ride> rideHistory=new ArrayList<>();
        for(int i=0;i<bookings.size();i++){
            rideHistory.add(bookings.get(i).getRide());
        }
        return ResponseEntity.ok(rideHistory);
    }

    @GetMapping("{rideId}/savings")
    public ResponseEntity<?> getSavings(@PathVariable String rideId){

        Optional<Ride> ride =rideService.getRide(rideId);
        Ride ride1=ride.orElse(null);
        double t=ride1.getPricePerSeat()*(ride1.getTotalSeats()) - ride1.getPricePerSeat();
        return ResponseEntity.ok(t);

    }

}
