import { Match, Seat } from './match.model';
import { User } from './user.model';

export interface BookingRequest {
  userId: number;
  matchId: number;
  seatIds: number[];
}

export interface BookingResponse {
  bookingId: number;
  bookingReference: string;
  status: string;
  totalAmount: number;
  bookingFee: number;
  totalSeats: number;
  bookingDate: Date;
  seats: SeatInfo[];
}

export interface SeatInfo {
  seatId: number;
  seatNumber: string;
  rowNumber: string;
  category: string;
  price: number;
}

export interface Booking {
  id: number;
  user: User;
  match: Match;
  bookingReference: string;
  totalAmount: number;
  bookingFee: number;
  totalSeats: number;
  status: BookingStatus;
  bookingDate: Date;
  seats: BookingSeat[];
  paymentStatus: PaymentStatus;
}

export interface BookingSeat {
  id: number;
  booking: Booking;
  seat: Seat;
  seatPrice: number;
}

export enum BookingStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export interface BookingSummary {
  selectedSeats: Seat[];
  totalAmount: number;
  bookingFee: number;
  finalAmount: number;
  match: Match;
}