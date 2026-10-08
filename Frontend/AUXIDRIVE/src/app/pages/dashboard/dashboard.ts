import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  userRole: string | null = '';

  get roleAffiche(): string {
    switch (this.userRole) {
      case 'ROLE_CLIENT':
      case 'CLIENT':
        return 'Assuré';
      case 'ROLE_AGENT':
      case 'AGENT':
        return 'Agent';
      case 'ROLE_ADMIN':
        return 'Administrateur';
      default:
        return this.userRole ?? '';
    }
  }

  ngOnInit(): void {
    this.userRole = this.authService.getRole();

    if (!this.authService.getToken()) {
      this.router.navigate(['/connexion']);
    }
  }

  deconnexion(): void {
    this.authService.logout();
    this.router.navigate(['/connexion']);
  }
}
