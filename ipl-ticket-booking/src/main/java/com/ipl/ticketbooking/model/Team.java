package com.ipl.ticketbooking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String shortName;
    
    @Column(nullable = false)
    private String city;
    
    @Column
    private String logoUrl;
    
    @Column
    private String primaryColor;
    
    @Column
    private String secondaryColor;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "homeTeam", fetch = FetchType.LAZY)
    private List<Match> homeMatches;
    
    @OneToMany(mappedBy = "awayTeam", fetch = FetchType.LAZY)
    private List<Match> awayMatches;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Team() {}
    
    public Team(String name, String shortName, String city) {
        this.name = name;
        this.shortName = shortName;
        this.city = city;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Match> getHomeMatches() { return homeMatches; }
    public void setHomeMatches(List<Match> homeMatches) { this.homeMatches = homeMatches; }
    
    public List<Match> getAwayMatches() { return awayMatches; }
    public void setAwayMatches(List<Match> awayMatches) { this.awayMatches = awayMatches; }
}