package com.example.makemyshow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "shows")
@Data
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;



    private boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "show_seat_prices",
            joinColumns = @JoinColumn(name = "show_id"))
    @MapKeyColumn(name = "seat_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "price")
    private Map<Seat.SeatType, Double> seatPrices = new HashMap<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // In Show.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private Screen screen;

    @OneToMany(mappedBy = "show", fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();
}
