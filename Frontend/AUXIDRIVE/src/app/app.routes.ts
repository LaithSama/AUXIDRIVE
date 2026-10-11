import { Routes } from '@angular/router';
import { AcceuilClient } from './pages/acceuil-client/acceuil-client';
import { Connexion } from './pages/connexion/connexion';
import { Inscription } from './pages/inscription/inscription';
import { Dashboard } from './pages/dashboard/dashboard';
import { DashboardAccueil } from './pages/dashboard/accueil/accueil';
import { DashboardContrats } from './pages/dashboard/contrats/contrats';
import { DashboardSinistres } from './pages/dashboard/sinistres/sinistres';
import { DashboardFactures } from './pages/dashboard/factures/factures';
import { DashboardVehicules } from './pages/dashboard/vehicules/vehicules';
import { DashboardProfil } from './pages/dashboard/profil/profil';
import { AgentDashboard } from './pages/agent-dashboard/agent-dashboard';
import { AdminDashboard } from './pages/admin-dashboard/admin-dashboard';
import { roleGuard } from './core/guards/role-guard';
import { clientGuard } from './core/guards/client-guard';
import { adminGuard } from './core/guards/admin-guard';

export const routes: Routes = [
    {path: '', redirectTo: 'acceuil-client', pathMatch: 'full'},

    {path: 'acceuil-client', component: AcceuilClient},

    {path: 'connexion', component: Connexion},

    {path: 'inscription', component: Inscription},

    {
        path: 'dashboard',
        component: Dashboard,
        canActivate: [clientGuard],
        children: [
            {path: '', component: DashboardAccueil},
            {path: 'contrats', component: DashboardContrats},
            {path: 'sinistres', component: DashboardSinistres},
            {path: 'factures', component: DashboardFactures},
            {path: 'vehicules', component: DashboardVehicules},
            {path: 'profil', component: DashboardProfil},
        ],
    },

    {path: 'agent-dashboard', component: AgentDashboard, canActivate: [roleGuard]},

    {path: 'admin-dashboard', component: AdminDashboard, canActivate: [adminGuard]},
];
