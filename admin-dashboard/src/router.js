import AdminDashboard from './dashboard/AdminDashboard';
import UserManagement from './components/UserManagement';

const routes = [
    { path: '/dashboard', component: AdminDashboard },
    { path: '/user-management', component: UserManagement },
    // Các đường dẫn khác
];

export default routes;
