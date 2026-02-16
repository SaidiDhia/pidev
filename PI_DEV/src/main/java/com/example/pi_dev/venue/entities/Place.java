package com.example.pi_dev.venue.entities;

import java.util.ArrayList;
import java.util.List;

public class Place {
    private int id;
    private String hostId;
    private String title;
    private String description;
    private double pricePerDay;
    private int capacity;
    private int maxGuests; // Maximum number of guests allowed
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private String category;
    private Status status;
    private String imageUrl; // Main image for preview
    private List<Amenity> amenities = new ArrayList<>();

    public enum Status {
        PENDING, APPROVED, DENIED
    }

    public Place() {
    }

    public Place(int id, String hostId, String title, String description, double pricePerDay, int capacity,
            String address, String city, double latitude, double longitude, String category, Status status) {
        this.id = id;
        this.hostId = hostId;
        this.title = title;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.capacity = capacity;
        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<Amenity> amenities) {
        this.amenities = amenities;
    }
}
