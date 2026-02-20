import { Routes } from '@angular/router';

export const matchesRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./match-list/match-list.component').then(m => m.MatchListComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./match-detail/match-detail.component').then(m => m.MatchDetailComponent)
  },
  {
    path: ':id/seats',
    loadComponent: () => import('./seat-selection/seat-selection.component').then(m => m.SeatSelectionComponent)
  }
];