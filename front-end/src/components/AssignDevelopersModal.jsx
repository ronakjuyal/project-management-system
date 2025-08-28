import React, { useState, useEffect } from 'react';
import { X, Users } from 'lucide-react';
import api from '../services/api';

const AssignDevelopersModal = ({ project, onClose, onAssigned }) => {
  const [developers, setDevelopers] = useState([]);
  const [selectedDeveloperIds, setSelectedDeveloperIds] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDevelopers();
    initializeSelectedDevelopers();
  }, []);

  const fetchDevelopers = async () => {
    try {
      const response = await api.get('/users/developers');
      setDevelopers(response.data);
    } catch (error) {
      console.error('Error fetching developers:', error);
    }
  };

  const initializeSelectedDevelopers = () => {
    if (project.assignedDevelopers) {
      const currentIds = new Set(project.assignedDevelopers.map(dev => dev.id));
      setSelectedDeveloperIds(currentIds);
    }
  };

  const handleDeveloperToggle = (developerId) => {
    const newSelected = new Set(selectedDeveloperIds);
    if (newSelected.has(developerId)) {
      newSelected.delete(developerId);
    } else {
      newSelected.add(developerId);
    }
    setSelectedDeveloperIds(newSelected);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (selectedDeveloperIds.size === 0) {
      setError('Please select at least one developer');
      setLoading(false);
      return;
    }

    try {
      const response = await api.put(`/projects/${project.id}/assign`, {
        developerIds: Array.from(selectedDeveloperIds)
      });
      onAssigned(response.data);
    } catch (error) {
      setError(error.response?.data?.message || 'Failed to assign developers');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Assign Developers</h2>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {error && (
              <div className="error-message">
                {error}
              </div>
            )}

            <div className="form-group">
              <label>Select Developers</label>
              <div className="checkbox-group">
                {developers.map(developer => (
                  <div 
                    key={developer.id} 
                    className={`checkbox-item ${selectedDeveloperIds.has(developer.id) ? 'selected' : ''}`}
                    onClick={() => handleDeveloperToggle(developer.id)}
                  >
                    <input
                      type="checkbox"
                      checked={selectedDeveloperIds.has(developer.id)}
                      onChange={() => handleDeveloperToggle(developer.id)}
                      disabled={loading}
                    />
                    <div className="user-info">
                      <div className="user-name">
                        {developer.firstName} {developer.lastName}
                      </div>
                      <div className="user-role">
                        {developer.username}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              {developers.length === 0 && (
                <p className="no-developers">No developers available</p>
              )}
            </div>

            <div className="assignment-summary">
              <Users size={16} />
              <span>{selectedDeveloperIds.size} developer(s) selected</span>
            </div>
          </div>

          <div className="modal-footer">
            <button 
              type="button" 
              className="btn btn-secondary" 
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={loading || selectedDeveloperIds.size === 0}
            >
              {loading ? 'Assigning...' : 'Assign Developers'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AssignDevelopersModal;
