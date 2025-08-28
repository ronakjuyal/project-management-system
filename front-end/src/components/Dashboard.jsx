import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import Navigation from './Navigation';
import ProjectCard from './ProjectCard';
import CreateProjectModal from './CreateProjectModal';
import api from '../services/api';
import { Plus, Calendar, Users, FolderOpen, AlertTriangle } from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [stats, setStats] = useState({
    totalProjects: 0,
    activeProjects: 0,
    overdueProjects: 0,
    assignedProjects: 0
  });

  useEffect(() => {
    fetchProjects();
    if (user?.role === 'ADMIN') {
      fetchStats();
    }
  }, [user]);

  const fetchProjects = async () => {
    try {
      const response = await api.get('/projects');
      setProjects(response.data);
      
      // Calculate user-specific stats
      const active = response.data.filter(p => p.status === 'ACTIVE').length;
      const overdue = response.data.filter(p => p.overdue).length;
      
      setStats(prev => ({
        ...prev,
        totalProjects: response.data.length,
        activeProjects: active,
        overdueProjects: overdue,
        assignedProjects: response.data.length
      }));
    } catch (error) {
      console.error('Error fetching projects:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      const [allProjects, overdueProjects] = await Promise.all([
        api.get('/projects/active'),
        api.get('/projects/overdue')
      ]);
      
      setStats(prev => ({
        ...prev,
        overdueProjects: overdueProjects.data.length
      }));
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const handleProjectCreated = (newProject) => {
    setProjects(prev => [newProject, ...prev]);
    setShowCreateModal(false);
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    let greeting = 'Good evening';
    if (hour < 12) greeting = 'Good morning';
    else if (hour < 18) greeting = 'Good afternoon';
    
    return `${greeting}, ${user?.firstName || user?.username}!`;
  };

  const getRoleBasedMessage = () => {
    switch (user?.role) {
      case 'ADMIN':
        return 'You have full system access. Manage projects, users, and system settings.';
      case 'PROJECT_LEAD':
        return 'Manage your projects and assign team members to tasks.';
      case 'DEVELOPER':
        return 'View your assigned projects and access project documents.';
      default:
        return 'Welcome to PixelForge Nexus!';
    }
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <Navigation />
        <div className="dashboard-content loading">
          <div className="loading-spinner">Loading projects...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Navigation />
      
      <div className="dashboard-content">
        <div className="dashboard-header">
          <div className="welcome-section">
            <h1>{getGreeting()}</h1>
            <p className="role-message">{getRoleBasedMessage()}</p>
          </div>
          
          {user?.role === 'ADMIN' && (
            <button 
              className="create-project-btn"
              onClick={() => setShowCreateModal(true)}
            >
              <Plus size={20} />
              New Project
            </button>
          )}
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">
              <FolderOpen size={24} />
            </div>
            <div className="stat-info">
              <h3>{stats.totalProjects}</h3>
              <p>Total Projects</p>
            </div>
          </div>
          
          <div className="stat-card">
            <div className="stat-icon active">
              <Calendar size={24} />
            </div>
            <div className="stat-info">
              <h3>{stats.activeProjects}</h3>
              <p>Active Projects</p>
            </div>
          </div>
          
          {stats.overdueProjects > 0 && (
            <div className="stat-card">
              <div className="stat-icon warning">
                <AlertTriangle size={24} />
              </div>
              <div className="stat-info">
                <h3>{stats.overdueProjects}</h3>
                <p>Overdue Projects</p>
              </div>
            </div>
          )}
          
          {user?.role === 'DEVELOPER' && (
            <div className="stat-card">
              <div className="stat-icon">
                <Users size={24} />
              </div>
              <div className="stat-info">
                <h3>{stats.assignedProjects}</h3>
                <p>Assigned to You</p>
              </div>
            </div>
          )}
        </div>

        <div className="projects-section">
          <div className="section-header">
            <h2>
              {user?.role === 'ADMIN' ? 'All Projects' : 
               user?.role === 'PROJECT_LEAD' ? 'Your Projects' : 'Assigned Projects'}
            </h2>
            <div className="project-filters">
              <select className="filter-select">
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="completed">Completed</option>
              </select>
            </div>
          </div>

          {projects.length === 0 ? (
            <div className="empty-state">
              <FolderOpen size={48} />
              <h3>No projects found</h3>
              <p>
                {user?.role === 'ADMIN' 
                  ? 'Create your first project to get started.'
                  : 'You haven\'t been assigned to any projects yet.'
                }
              </p>
            </div>
          ) : (
            <div className="projects-grid">
              {projects.map(project => (
                <ProjectCard 
                  key={project.id} 
                  project={project}
                  onClick={() => navigate(`/projects/${project.id}`)}
                />
              ))}
            </div>
          )}
        </div>

        {showCreateModal && (
          <CreateProjectModal 
            onClose={() => setShowCreateModal(false)}
            onProjectCreated={handleProjectCreated}
          />
        )}
      </div>
    </div>
  );
};

export default Dashboard;