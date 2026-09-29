package Smart_Carpooling.demo.Entity;

public class CoRiderDTO {
    private User user;
    private int tripCount;

    public CoRiderDTO(User user, int tripCount) {
        this.user = user;
        this.tripCount = tripCount;
    }

    public User getUser() { return user; }
    public int getTripCount() { return tripCount; }
}