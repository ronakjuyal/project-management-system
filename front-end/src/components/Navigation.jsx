import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { 
  Shield, 
  Home, 
  Users, 
  Settings, 
  LogOut, 
  User,
  ChevronDown,
  Menu,
  X
} from 'lucide-react';

const Navigation = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showMobileMenu, setShowMobileMenu] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navigationItems = [
    { path: '/dashboard', label: 'Dashboard', icon: Home, roles: ['ADMIN', 'PROJECT_LEAD', 'DEVELOPER'] },
    { path: '/admin/users', label: 'User Management', icon: Users, roles: ['ADMIN'] },
    { path: '/settings', label: 'Settings', icon: Settings, roles: ['ADMIN', 'PROJECT_LEAD', 'DEVELOPER'] }
  ];

  const visibleItems = navigationItems.filter(item => 
    item.roles.includes(user?.role)
  );

  return (
    <nav className="navigation">
      <div className="nav-container">
        <div className="nav-brand">
          <Shield className="brand-icon" />
          <span className="brand-text">PixelForge Nexus</span>
        </div>

        <div className="nav-mobile-toggle">
          <button 
            onClick={() => setShowMobileMenu(!showMobileMenu)}
            className="mobile-menu-btn"
          >
            {showMobileMenu ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        <div className={`nav-links ${showMobileMenu ? 'mobile-open' : ''}`}>
          {visibleItems.map(item => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            
            return (
              <button
                key={item.path}
                onClick={() => {
                  navigate(item.path);
                  setShowMobileMenu(false);
                }}
                className={`nav-link ${isActive ? 'active' : ''}`}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>

        <div className="nav-user">
          <div className="user-menu-container">
            <button 
              className="user-menu-trigger"
              onClick={() => setShowUserMenu(!showUserMenu)}
            >
              <div className="user-avatar">
                <User size={18} />
              </div>
              <div className="user-info">
                <span className="user-name">{user?.firstName} {user?.lastName}</span>
                <span className="user-role">{user?.role?.replace('_', ' ')}</span>
              </div>
              <ChevronDown size={16} className={`chevron ${showUserMenu ? 'open' : ''}`} />
            </button>

            {showUserMenu && (
              <div className="user-dropdown">
                <div className="dropdown-header">
                  <div className="user-details">
                    <strong>{user?.firstName} {user?.lastName}</strong>
                    <span>{user?.email}</span>
                    <span className="role-badge">{user?.role?.replace('_', ' ')}</span>
                  </div>
                </div>
                
                <div className="dropdown-divider"></div>
                
                <button className="dropdown-item" onClick={() => navigate('/profile')}>
                  <User size={16} />
                  Profile Settings
                </button>
                
                <button className="dropdown-item" onClick={() => navigate('/change-password')}>
                  <Settings size={16} />
                  Change Password
                </button>
                
                <div className="dropdown-divider"></div>
                
                <button className="dropdown-item logout" onClick={handleLogout}>
                  <LogOut size={16} />
                  Sign Out
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {showUserMenu && (
        <div 
          className="overlay" 
          onClick={() => setShowUserMenu(false)}
        ></div>
      )}
    </nav>
  );
};

export default Navigation;