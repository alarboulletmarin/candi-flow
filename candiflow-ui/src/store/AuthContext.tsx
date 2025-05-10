import React, { createContext, useContext, ReactNode } from 'react';
import authService from '../services/authService';

// Contexte d'authentification simplifié
interface AuthContextType {
  user: any; // Utilisateur par défaut toujours disponible
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType>({
  user: authService.getDefaultUser(),
  isLoading: false
});

export function AuthProvider({ children }: { children: ReactNode }) {
  // Fournir un utilisateur par défaut sans authentification
  const defaultUser = authService.getDefaultUser();
  
  return (
    <AuthContext.Provider
      value={{
        user: defaultUser,
        isLoading: false
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
