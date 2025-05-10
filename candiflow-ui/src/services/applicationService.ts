import api from './api';
import { Application, ApplicationFormData, ApplicationStatus, StatusUpdate, StatusUpdateFormData } from '../types/application';

export const applicationService = {
  /**
   * Récupère toutes les candidatures de l'utilisateur
   */
  async getApplications(): Promise<Application[]> {
    try {
      const response = await api.get('/api/applications');
      
      // Vérifier si la réponse est une réponse paginée (avec content, pageable, etc.)
      if (response.data && response.data.content && Array.isArray(response.data.content)) {
        return response.data.content;
      } 
      // Vérifier si la réponse est directement un tableau
      else if (Array.isArray(response.data)) {
        return response.data;
      } 
      // Ni l'un ni l'autre, on log l'erreur et on retourne un tableau vide
      else {
        console.error('La réponse API n\'est pas un tableau ni une réponse paginée:', response.data);
        return [];
      }
    } catch (error) {
      console.error('Erreur lors de la récupération des candidatures:', error);
      return [];
    }
  },
  
  /**
   * Alias pour getApplications - pour compatibilité avec le tableau de bord
   */
  async getUserApplications(): Promise<Application[]> {
    // Utiliser directement getApplications qui gère déjà les réponses paginées
    return this.getApplications();
  },

  /**
   * Récupère une candidature spécifique par son ID
   */
  async getApplicationById(id: string): Promise<Application> {
    const response = await api.get<Application>(`/api/applications/${id}`);
    return response.data;
  },

  /**
   * Convertit les données du formulaire au format attendu par l'API
   */
  convertFormDataToApiRequest(data: ApplicationFormData): any {
    // Création d'un objet Date à partir de la chaîne de date du formulaire
    const dateObj = new Date(data.applicationDate);
    
    return {
      companyName: data.company,
      jobTitle: data.position,
      jobUrl: data.url || '',
      dateApplied: dateObj.toISOString(), // Format ISO pour Instant
      generalNotes: data.notes || '',
      // Ajout d'autres champs si nécessaire
      initialStatus: 'APPLIED', // Statut par défaut
    };
  },

  /**
   * Crée une nouvelle candidature
   */
  async createApplication(data: ApplicationFormData): Promise<Application> {
    const apiRequest = this.convertFormDataToApiRequest(data);
    const response = await api.post<Application>('/api/applications', apiRequest);
    return response.data;
  },

  /**
   * Met à jour une candidature existante
   */
  async updateApplication(id: string, data: ApplicationFormData): Promise<Application> {
    const apiData = this.convertFormDataToApiRequest(data);
    const response = await api.put<Application>(`/api/applications/${id}`, apiData);
    return response.data;
  },

  /**
   * Supprime une candidature
   */
  async deleteApplication(id: string): Promise<void> {
    await api.delete(`/api/applications/${id}`);
  },

  /**
   * Récupère tous les statuts de candidature disponibles
   */
  async getApplicationStatuses(): Promise<ApplicationStatus[]> {
    const response = await api.get<ApplicationStatus[]>('/api/application-statuses');
    return response.data;
  },

  /**
   * Récupère toutes les mises à jour de statut pour une candidature
   */
  async getStatusUpdates(applicationId: string): Promise<StatusUpdate[]> {
    const response = await api.get<StatusUpdate[]>(`/api/applications/${applicationId}/status-updates`);
    return response.data;
  },

  /**
   * Crée une nouvelle mise à jour de statut pour une candidature
   */
  async createStatusUpdate(applicationId: string, data: StatusUpdateFormData): Promise<StatusUpdate> {
    const response = await api.post<StatusUpdate>(`/api/applications/${applicationId}/status-updates`, data);
    return response.data;
  },

  /**
   * Met à jour une mise à jour de statut existante
   */
  async updateStatusUpdate(applicationId: string, statusUpdateId: string, data: StatusUpdateFormData): Promise<StatusUpdate> {
    const response = await api.put<StatusUpdate>(`/api/applications/${applicationId}/status-updates/${statusUpdateId}`, data);
    return response.data;
  },

  /**
   * Supprime une mise à jour de statut
   */
  async deleteStatusUpdate(applicationId: string, statusUpdateId: string): Promise<void> {
    await api.delete(`/api/applications/${applicationId}/status-updates/${statusUpdateId}`);
  }
};

export default applicationService;
