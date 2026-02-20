package com.ipl.ticketbooking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;
    
    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;
    
    @Column(nullable = false)
    private String matchType; // LEAGUE, QUALIFIER, ELIMINATOR, FINAL
    
    @Column(nullable = false)
    private String season; // IPL 2024, IPL 2025
    
    @Column(name = "ticket_sale_start", nullable = false)
    private LocalDateTime ticketSaleStart;
    
    @Column(name = "ticket_sale_end", nullable = false)
    private LocalDateTime ticketSaleEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Match() {}
    
    public Match(Team homeTeam, Team awayTeam, Stadium stadium, LocalDateTime matchDate, 
                 String matchType, String season, LocalDateTime ticketSaleStart, 
                 LocalDateTime ticketSaleEnd, BigDecimal basePrice) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.matchDate = matchDate;
        this.matchType = matchType;
        this.season = season;
        this.ticketSaleStart = ticketSaleStart;
        this.ticketSaleEnd = ticketSaleEnd;
        this.basePrice = basePrice;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    
    public Stadium getStadium() { return stadium; }
    public void setStadium(Stadium stadium) { this.stadium = stadium; }
    
    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public LocalDateTime getTicketSaleStart() { return ticketSaleStart; }
    public void setTicketSaleStart(LocalDateTime ticketSaleStart) { this.ticketSaleStart = ticketSaleStart; }
    
    public LocalDateTime getTicketSaleEnd() { return ticketSaleEnd; }
    public void setTicketSaleEnd(LocalDateTime ticketSaleEnd) { this.ticketSaleEnd = ticketSaleEnd; }
    
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
    
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}

enum MatchStatus {
    SCHEDULED, LIVE, COMPLETED, CANCELLED, POSTPONED
}