package com.example.pi_dev.venue.entities;

import java.time.LocalDate;

public class Booking {
    private int id;
    private int placeId;
    private long renterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;
    private int guestsCount;
    private Status status;

    public enum Status {
        PENDING, CONFIRMED, REJECTED, CANCELLED, COMPLETED
    }

    public Booking() {
    }

    public Booking(int id, int placeId, long renterId, LocalDate startDate, LocalDate endDate, double totalPrice,
            Status status) {
        this.id = id;
        this.placeId = placeId;
        this.renterId = renterId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public long getRenterId() {
        return renterId;
    }

    public void setRenterId(long renterId) {
        this.renterId = renterId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getGuestsCount() {
        return guestsCount;
    }

    public void setGuestsCount(int guestsCount) {
        this.guestsCount = guestsCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
