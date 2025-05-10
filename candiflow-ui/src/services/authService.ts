import api from './api';

// Service simplifié sans authentification
export const authService = {
  /**
   * Fonction factice qui simule un utilisateur connecté
   * pour maintenir la compatibilité avec le code existant
   */
  getDefaultUser() {
    return {
      id: 1,
      email: 'user@example.com',
      name: 'Utilisateur',
      role: 'CANDIDATE'
    };
  }
};

export default authService;
