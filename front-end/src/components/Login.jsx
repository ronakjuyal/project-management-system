import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Shield, Users, Folder } from 'lucide-react';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const result = await login(username, password);
    
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
    
    setLoading(false);
  };

  const testCredentials = [
    { role: 'Admin', username: 'admin', password: 'admin123', description: 'Full system access' },
    { role: 'Project Lead', username: 'lead1', password: 'lead123', description: 'Alice Johnson - Manage projects' },
    { role: 'Developer', username: 'dev1', password: 'dev123', description: 'Charlie Brown - View assigned projects' }
  ];

  return (
    <div className="login-container">
      <div className="login-background">
        <div className="login-overlay"></div>
      </div>
      
      <div className="login-content">
        <div className="login-card">
          <div className="login-header">
            <div className="logo">
              <Shield className="logo-icon" />
              <h1>PixelForge Nexus</h1>
            </div>
            <p className="subtitle">Secure Project Management System</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                placeholder="Enter your username"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <div className="password-input">
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="Enter your password"
                  disabled={loading}
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={loading}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            {error && <div className="error-message">{error}</div>}

            <button 
              type="submit" 
              className="login-button"
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="test-credentials">
            <h3>Test Credentials</h3>
            <div className="credentials-grid">
              {testCredentials.map((cred, index) => (
                <div key={index} className="credential-card" onClick={() => {
                  setUsername(cred.username);
                  setPassword(cred.password);
                }}>
                  <div className="credential-role">{cred.role}</div>
                  <div className="credential-username">{cred.username}</div>
                  <div className="credential-description">{cred.description}</div>
                </div>
              ))}
            </div>
          </div>

          <div className="features-preview">
            <div className="feature">
              <Shield size={20} />
              <span>Secure Authentication</span>
            </div>
            <div className="feature">
              <Users size={20} />
              <span>Role-Based Access</span>
            </div>
            <div className="feature">
              <Folder size={20} />
              <span>Project Management</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;