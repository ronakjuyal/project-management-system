import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import Navigation from './Navigation';
import api from '../services/api';
import { Plus, Edit, Shield, User, Users as UsersIcon } from 'lucide-react';

const UserManagement = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await api.get('/users');
      setUsers(response.data);
    } catch (error) {
      console.error('Error fetching users:', error);
    } finally {
      setLoading(false);
    }
  };

  const getRoleIcon = (role) => {
    switch (role) {
      case 'ADMIN':
        return <Shield size={16} className="role-icon admin" />;
      case 'PROJECT_LEAD':
        return <UsersIcon size={16} className="role-icon lead" />;
      case 'DEVELOPER':
        return <User size={16} className="role-icon developer" />;
      default:
        return <User size={16} />;
    }
  };

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'ADMIN':
        return 'role-badge admin';
      case 'PROJECT_LEAD':
        return 'role-badge lead';
      case 'DEVELOPER':
        return 'role-badge developer';
      default:
        return 'role-badge';
    }
  };

  if (user?.role !== 'ADMIN') {
    return (
      <div className="dashboard-container">
        <Navigation />
        <div className="dashboard-content">
          <div className="empty-state">
            <Shield size={48} />
            <h3>Access Denied</h3>
            <p>You don't have permission to access user management.</p>
          </div>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="dashboard-container">
        <Navigation />
        <div className="dashboard-content loading">
          <div className="loading-spinner">Loading users...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Navigation />
      
      <div className="dashboard-content">
        <div className="page-header">
          <div>
            <h1>User Management</h1>
            <p>Manage system users and their roles</p>
          </div>
          <button 
            className="btn btn-primary"
            onClick={() => setShowCreateModal(true)}
          >
            <Plus size={16} />
            Add User
          </button>
        </div>

        <div className="users-grid">
          {users.map(userItem => (
            <div key={userItem.id} className="user-card">
              <div className="user-header">
                <div className="user-avatar">
                  {userItem.firstName[0]}{userItem.lastName[0]}
                </div>
                <div className="user-info">
                  <h3>{userItem.firstName} {userItem.lastName}</h3>
                  <p>{userItem.email}</p>
                  <span className="username">@{userItem.username}</span>
                </div>
              </div>
              
              <div className="user-role">
                {getRoleIcon(userItem.role)}
                <span className={getRoleBadgeClass(userItem.role)}>
                  {userItem.role.replace('_', ' ')}
                </span>
              </div>

              <div className="user-status">
                <span className={`status-indicator ${userItem.enabled ? 'active' : 'inactive'}`}>
                  {userItem.enabled ? 'Active' : 'Inactive'}
                </span>
              </div>

              <div className="user-actions">
                <button className="btn-icon" title="Edit User">
                  <Edit size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>

        {users.length === 0 && (
          <div className="empty-state">
            <UsersIcon size={48} />
            <h3>No users found</h3>
            <p>Add your first user to get started.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserManagement;