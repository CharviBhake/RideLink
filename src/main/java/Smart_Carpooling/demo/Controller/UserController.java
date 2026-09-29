package Smart_Carpooling.demo.Controller;

import Smart_Carpooling.demo.Entity.Booking;
import Smart_Carpooling.demo.Entity.CoRiderDTO;
import Smart_Carpooling.demo.Entity.Ride;
import Smart_Carpooling.demo.Entity.User;
import Smart_Carpooling.demo.Repository.BookingRepo;
import Smart_Carpooling.demo.Repository.RideRepository;
import Smart_Carpooling.demo.Repository.UserRepository;
import Smart_Carpooling.demo.Service.JwtBlacklistService;
import Smart_Carpooling.demo.Service.RideService;
import Smart_Carpooling.demo.Service.UserService;
import Smart_Carpooling.demo.Utitly.JWTUTIL;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUTIL jwtutil;
    @Autowired
    private JwtBlacklistService jwtBlacklistService;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private RideService rideService;
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        try{
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userId=authentication.getName();
            User user=userService.findById(userId);
            Map<String,Object> profile=new HashMap<>();
            profile.put("id", user.getId());
         //   profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("name", user.getDisplayUsername());
            profile.put("phone", user.getPhone());
            profile.put("location", user.getLocation());
            profile.put("city", user.getCity());
            profile.put("bio", user.getBio());
            profile.put("carModel", user.getCarModel());
            profile.put("carNumber", user.getCarNumber());
            profile.put("carColor", user.getCarColor());
           return ResponseEntity.ok(profile);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody Map<String,Object> updates){
        try{
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            User user = userService.findById(userId);
            if (user != null) {
                if (updates.containsKey("name")) {
                    String name= (String) updates.get("name");
                    if(name!=null && !name.isBlank()){
                        user.setDisplayUsername(name);
                    }
                }
              /*  if (updates.containsKey("email")) {
                    user.setEmail((String) updates.get("email"));
                    user.setUsername((String) updates.get("email"));
                } */
                if (updates.containsKey("phone")) {
                    String phone = (String) updates.get("phone");
                    if (phone != null && !phone.isBlank()) {
                        user.setPhone(phone);
                    }
                }
                if (updates.containsKey("location")) {
                    String Location= ((String) updates.get("location"));
                    if(Location!=null && !Location.isBlank()){
                        user.setLocation(Location);
                    }
                }
                if (updates.containsKey("city")) {
                    String city= (String) updates.get("city");
                    if(city!=null && !city.isBlank()){
                        user.setLocation(city);
                    }
                }
                if (updates.containsKey("bio")) {
                    String bio= (String) updates.get("bio");
                    if(bio!=null && !bio.isBlank()){
                        user.setLocation(bio);
                    }
                }
                if (updates.containsKey("carModel")) {
                    String carModel= (String) updates.get("carModel");
                    if(carModel!=null && !carModel.isBlank()){
                        user.setLocation(carModel);
                    }
                }
                if (updates.containsKey("carNumber")) {
                    String carNo= (String) updates.get("carNumber");
                    if(carNo!=null && !carNo.isBlank()){
                        user.setLocation(carNo);
                    }
                }
                if (updates.containsKey("carColor")) {
                    user.setCarColor((String) updates.get("carColor"));
                }
                User updatedUser=userService.updateUser(user);
                Map<String, Object> profile = new HashMap<>();
                profile.put("id", updatedUser.getId());
                profile.put("username", updatedUser.getUsername());
                profile.put("email", updatedUser.getEmail());
                profile.put("name", updatedUser.getDisplayUsername());
                profile.put("phone", updatedUser.getPhone());
                profile.put("location", updatedUser.getLocation());
                profile.put("city", updatedUser.getCity());
                profile.put("bio", updatedUser.getBio());
                profile.put("carModel", updatedUser.getCarModel());
                profile.put("carNumber", updatedUser.getCarNumber());
                profile.put("carColor", updatedUser.getCarColor());
                return ResponseEntity.ok(profile);
            }
        }catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserByUserName(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        userRepository.deleteByUsername(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // because username = email
        User user = userService.findById(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse request){
        String header=request.getHeader("Authorization");
        if(header==null || !header.startsWith("Bearer")){
            return ResponseEntity.badRequest().body("no token");
        }
        String token=header.substring(7);
        long ttl=jwtutil.getRemainingTime(token);
        jwtBlacklistService.blacklistToken(token,ttl);
        return ResponseEntity.ok("logged out successfully");
    }
    @GetMapping("/total_trips")
    public ResponseEntity<?> getTrips(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findById(email);
        return ResponseEntity.ok(user.getTotalRides());
    }

    @GetMapping("/co2")
    public ResponseEntity<?> getCo2(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findById(email);
        List<Ride> rides=rideRepository.findByDriverId(user.getId());
        int total=0;
        for(int i=0;i<rides.size();i++){
            total+=rides.get(i).getCo2Saved();
        }
        return  ResponseEntity.ok(total);
    }

    @GetMapping("/savings")
    public ResponseEntity<Double> calculateSavings(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findById(email);
        List<Booking> bookings=bookingRepo.findByPassengerId(user.getId());
        System.out.println("Total bookings found: " + bookings.size());
        bookings.forEach(b -> System.out.println("Ride date: " + b.getRide().getRideDate() + ", isBefore now: " + b.getRide().getRideDate().isBefore(LocalDate.now())));
        double totalSavings = bookings.stream()
                .filter(b -> b.getRide().getRideDate().isBefore(LocalDate.now()))
                .mapToDouble(b -> {
                    Ride r = b.getRide();
                    int passengers = r.getTotalSeats() - r.getAvailableSeats();
                    double solo = r.getDistance() * 10;
                    System.out.println("Ride: totalSeats=" + r.getTotalSeats()
                            + ", availableSeats=" + r.getAvailableSeats()
                            + ", passengers=" + passengers
                            + ", distance=" + r.getDistance()
                            + ", solo=" + solo);
                    if (passengers <= 1) return 0;
                    return solo - (solo / passengers);
                })
                .sum();
        return ResponseEntity.ok(totalSavings);
    }
    @GetMapping("/coRiders")
    public ResponseEntity<List<CoRiderDTO>> getCoRiders(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findById(email);
        List<CoRiderDTO> ans=rideService.getRecentCoRiders(user.getId());
        System.out.println("coRiders are:"+ ans);
        return ResponseEntity.ok(ans);

    }


}
