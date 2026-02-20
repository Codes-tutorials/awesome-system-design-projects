export interface Team {
  id: number;
  name: string;
  shortName: string;
  logo: string;
  color: string;
}

export interface Stadium {
  id: number;
  name: string;
  city: string;
  capacity: number;
  address: string;
}

export interface Match {
  id: number;
  homeTeam: Team;
  awayTeam: Team;
  stadium: Stadium;
  matchDate: Date;
  matchTime: string;
  matchType: string;
  status: MatchStatus;
  ticketSaleStart: Date;
  ticketSaleEnd: Date;
  availableSeats: number;
  totalSeats: number;
  minPrice: number;
  maxPrice: number;
}

export enum MatchStatus {
  UPCOMING = 'UPCOMING',
  LIVE = 'LIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface SeatCategory {
  id: number;
  categoryName: string;
  price: number;
  description: string;
  color: string;
  availableSeats: number;
  totalSeats: number;
}

export interface Seat {
  id: number;
  seatNumber: string;
  rowNumber: string;
  seatCategory: SeatCategory;
  status: SeatStatus;
  price: number;
  isSelected?: boolean;
}

export enum SeatStatus {
  AVAILABLE = 'AVAILABLE',
  BOOKED = 'BOOKED',
  LOCKED = 'LOCKED'
}