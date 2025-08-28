import React from 'react';
import { Calendar, Users, FileText, AlertTriangle, CheckCircle } from 'lucide-react';

const ProjectCard = ({ project, onClick }) => {
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStatusBadge = () => {
    if (project.status === 'COMPLETED') {
      return <span className="status-badge completed"><CheckCircle size={14} />Completed</span>;
    }
    if (project.overdue) {
      return <span className="status-badge overdue"><AlertTriangle size={14} />Overdue</span>;
    }
    return <span className="status-badge active">Active</span>;
  };

  return (
    <div className="project-card" onClick={onClick}>
      <div className="project-header">
        <h3 className="project-title">{project.name}</h3>
        {getStatusBadge()}
      </div>
      
      <p className="project-description">{project.description}</p>
      
      <div className="project-meta">
        <div className="meta-item">
          <Calendar size={16} />
          <span>Due: {formatDate(project.deadline)}</span>
        </div>
        
        {project.lead && (
          <div className="meta-item">
            <Users size={16} />
            <span>Lead: {project.lead.firstName} {project.lead.lastName}</span>
          </div>
        )}
        
        <div className="meta-item">
          <FileText size={16} />
          <span>{project.documentCount} documents</span>
        </div>
      </div>
      
      {project.assignedDevelopers && project.assignedDevelopers.length > 0 && (
        <div className="project-team">
          <div className="team-label">Team:</div>
          <div className="team-avatars">
            {project.assignedDevelopers.slice(0, 3).map(dev => (
              <div key={dev.id} className="team-avatar" title={`${dev.firstName} ${dev.lastName}`}>
                {dev.firstName[0]}{dev.lastName[0]}
              </div>
            ))}
            {project.assignedDevelopers.length > 3 && (
              <div className="team-avatar more">
                +{project.assignedDevelopers.length - 3}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default ProjectCard;