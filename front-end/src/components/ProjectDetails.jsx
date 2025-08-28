import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import Navigation from './Navigation';
import AssignDevelopersModal from './AssignDevelopersModal';
import DocumentUploadModal from './DocumentUploadModal';
import api from '../services/api';
import { 
  ArrowLeft, 
  Calendar, 
  Users, 
  FileText, 
  Upload, 
  UserPlus,
  CheckCircle,
  AlertTriangle,
  Download,
  Trash2
} from 'lucide-react';

const ProjectDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [project, setProject] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);

  useEffect(() => {
    fetchProjectDetails();
    fetchDocuments();
  }, [id]);

  const fetchProjectDetails = async () => {
    try {
      const response = await api.get(`/projects/${id}`);
      setProject(response.data);
    } catch (error) {
      console.error('Error fetching project:', error);
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const fetchDocuments = async () => {
    try {
      const response = await api.get(`/documents/projects/${id}`);
      setDocuments(response.data);
    } catch (error) {
      console.error('Error fetching documents:', error);
    }
  };

  const handleAssignDevelopers = (updatedProject) => {
    setProject(updatedProject);
    setShowAssignModal(false);
  };

  const handleDocumentUploaded = (newDocument) => {
    setDocuments(prev => [newDocument, ...prev]);
    setShowUploadModal(false);
  };

  const handleDownloadDocument = async (documentId, fileName) => {
    try {
      const response = await api.get(`/documents/${documentId}/download`, {
        responseType: 'blob'
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading document:', error);
    }
  };

  const handleDeleteDocument = async (documentId) => {
    if (window.confirm('Are you sure you want to delete this document?')) {
      try {
        await api.delete(`/documents/${documentId}`);
        setDocuments(prev => prev.filter(doc => doc.id !== documentId));
      } catch (error) {
        console.error('Error deleting document:', error);
      }
    }
  };

  const canUploadDocuments = () => {
    return user?.role === 'ADMIN' || 
           (user?.role === 'PROJECT_LEAD' && project?.lead?.id === user?.id);
  };

  const canAssignDevelopers = () => {
    return user?.role === 'ADMIN' || 
           (user?.role === 'PROJECT_LEAD' && project?.lead?.id === user?.id);
  };

  const canDeleteDocument = (document) => {
    return user?.role === 'ADMIN' || 
           document.uploadedBy?.id === user?.id ||
           (user?.role === 'PROJECT_LEAD' && project?.lead?.id === user?.id);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <Navigation />
        <div className="dashboard-content loading">
          <div className="loading-spinner">Loading project details...</div>
        </div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="dashboard-container">
        <Navigation />
        <div className="dashboard-content">
          <div className="empty-state">
            <h3>Project not found</h3>
            <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Navigation />
      
      <div className="dashboard-content">
        <div className="project-details-header">
          <button 
            className="back-btn"
            onClick={() => navigate('/dashboard')}
          >
            <ArrowLeft size={20} />
            Back to Dashboard
          </button>

          <div className="project-title-section">
            <div className="title-row">
              <h1>{project.name}</h1>
              <div className="project-status">
                {project.status === 'COMPLETED' ? (
                  <span className="status-badge completed">
                    <CheckCircle size={16} />
                    Completed
                  </span>
                ) : project.overdue ? (
                  <span className="status-badge overdue">
                    <AlertTriangle size={16} />
                    Overdue
                  </span>
                ) : (
                  <span className="status-badge active">Active</span>
                )}
              </div>
            </div>
            <p className="project-description">{project.description}</p>
          </div>

          <div className="project-actions">
            {canAssignDevelopers() && (
              <button 
                className="btn btn-secondary"
                onClick={() => setShowAssignModal(true)}
              >
                <UserPlus size={16} />
                Assign Developers
              </button>
            )}
            {canUploadDocuments() && (
              <button 
                className="btn btn-primary"
                onClick={() => setShowUploadModal(true)}
              >
                <Upload size={16} />
                Upload Document
              </button>
            )}
          </div>
        </div>

        <div className="project-details-grid">
          <div className="project-info-card">
            <h3>Project Information</h3>
            <div className="info-grid">
              <div className="info-item">
                <Calendar size={18} />
                <div>
                  <strong>Deadline</strong>
                  <p>{formatDate(project.deadline)}</p>
                </div>
              </div>
              
              {project.lead && (
                <div className="info-item">
                  <Users size={18} />
                  <div>
                    <strong>Project Lead</strong>
                    <p>{project.lead.firstName} {project.lead.lastName}</p>
                  </div>
                </div>
              )}
              
              <div className="info-item">
                <FileText size={18} />
                <div>
                  <strong>Documents</strong>
                  <p>{documents.length} files</p>
                </div>
              </div>
            </div>
          </div>

          <div className="team-card">
            <h3>Assigned Team</h3>
            {project.assignedDevelopers && project.assignedDevelopers.length > 0 ? (
              <div className="team-list">
                {project.assignedDevelopers.map(developer => (
                  <div key={developer.id} className="team-member">
                    <div className="member-avatar">
                      {developer.firstName[0]}{developer.lastName[0]}
                    </div>
                    <div className="member-info">
                      <strong>{developer.firstName} {developer.lastName}</strong>
                      <span>{developer.role.replace('_', ' ')}</span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="no-team">No developers assigned yet</p>
            )}
          </div>
        </div>

        <div className="documents-section">
          <div className="section-header">
            <h3>Project Documents</h3>
            <span className="document-count">{documents.length} files</span>
          </div>

          {documents.length === 0 ? (
            <div className="empty-documents">
              <FileText size={48} />
              <h4>No documents uploaded</h4>
              <p>Upload project documents to share with the team</p>
            </div>
          ) : (
            <div className="documents-grid">
              {documents.map(document => (
                <div key={document.id} className="document-card">
                  <div className="document-info">
                    <div className="document-icon">
                      <FileText size={20} />
                    </div>
                    <div className="document-details">
                      <h4>{document.originalFileName}</h4>
                      <p>
                        {formatFileSize(document.fileSize)} • 
                        Uploaded by {document.uploadedBy.firstName} {document.uploadedBy.lastName} • 
                        {formatDate(document.uploadedAt)}
                      </p>
                      {document.description && (
                        <p className="document-description">{document.description}</p>
                      )}
                    </div>
                  </div>
                  <div className="document-actions">
                    <button 
                      className="btn-icon"
                      onClick={() => handleDownloadDocument(document.id, document.originalFileName)}
                      title="Download"
                    >
                      <Download size={16} />
                    </button>
                    {canDeleteDocument(document) && (
                      <button 
                        className="btn-icon danger"
                        onClick={() => handleDeleteDocument(document.id)}
                        title="Delete"
                      >
                        <Trash2 size={16} />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {showAssignModal && (
          <AssignDevelopersModal 
            project={project}
            onClose={() => setShowAssignModal(false)}
            onAssigned={handleAssignDevelopers}
          />
        )}

        {showUploadModal && (
          <DocumentUploadModal 
            projectId={project.id}
            onClose={() => setShowUploadModal(false)}
            onUploaded={handleDocumentUploaded}
          />
        )}
      </div>
    </div>
  );
};

export default ProjectDetails;