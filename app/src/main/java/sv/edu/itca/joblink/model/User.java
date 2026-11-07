package sv.edu.itca.joblink.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public enum Role {
        WORKER,
        CLIENT
    }

    private String id;
    private String name;
    private String email;
    private Role role;
    private String profession;
    private String category;
    private String location;
    private String bio;
    private double rating;
    private int reviewsCount;
    private boolean isOnline;
    private String phone;
    private List<String> portfolioPhotos;

    public User() {
        this.portfolioPhotos = new ArrayList<>();
    }

    public User(String id, String name, String email, Role role, String profession, String category, String location, String bio, double rating, int reviewsCount, boolean isOnline, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profession = profession;
        this.category = category;
        this.location = location;
        this.bio = bio;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.isOnline = isOnline;
        this.phone = phone;
        this.portfolioPhotos = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getPortfolioPhotos() { return portfolioPhotos; }
    public void setPortfolioPhotos(List<String> portfolioPhotos) { this.portfolioPhotos = portfolioPhotos; }
}