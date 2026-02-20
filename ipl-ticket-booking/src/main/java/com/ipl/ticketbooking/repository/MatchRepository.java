package com.ipl.ticketbooking.repository;

import com.ipl.ticketbooking.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    /**
     * Finds upcoming matches
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate > :currentTime ORDER BY m.matchDate ASC")
    List<Match> findUpcomingMatches(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Finds matches by team
     */
    @Query("SELECT m FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId " +
           "ORDER BY m.matchDate ASC")
    List<Match> findMatchesByTeam(@Param("teamId") Long teamId);
    
    /**
     * Finds matches by stadium
     */
    List<Match> findByStadiumIdOrderByMatchDateAsc(Long stadiumId);
    
    /**
     * Finds matches by season
     */
    List<Match> findBySeasonOrderByMatchDateAsc(String season);
    
    /**
     * Finds matches with active ticket sales
     */
    @Query("SELECT m FROM Match m WHERE :currentTime BETWEEN m.ticketSaleStart AND m.ticketSaleEnd " +
           "ORDER BY m.matchDate ASC")
    List<Match> findMatchesWithActiveTicketSales(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Finds matches by date range
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.matchDate ASC")
    List<Match> findMatchesByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
}