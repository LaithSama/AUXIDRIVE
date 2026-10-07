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
