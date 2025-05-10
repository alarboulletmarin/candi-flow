import React, { useEffect } from 'react';
import { Stack, useRouter, useSegments } from 'expo-router';
import { useAuth } from '../../src/store/AuthContext';

// Composant pour gérer la protection des routes
export function ProtectedRoutesProvider({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return; // Attendre que la vérification d'authentification soit terminée

    const inAuthGroup = segments[0] === '(auth)';
    const inAppGroup = segments[0] === '(app)';

    // Si l'utilisateur n'est pas connecté et tente d'accéder à une route protégée
    if (!user && !inAuthGroup) {
      // Rediriger vers la page de connexion
      router.replace('/(auth)/login');
    } else if (user && inAuthGroup) {
      // Si l'utilisateur est connecté mais tente d'accéder à une route d'authentification
      // Rediriger vers sa page d'accueil
      router.replace('/');
    }
  }, [user, isLoading, segments]);

  return <>{children}</>;
}

export default function AppLayout() {
  return (
    <Stack
      screenOptions={{
        headerShown: true,
        headerStyle: {
          backgroundColor: '#2563EB',
        },
        headerTintColor: 'white',
        headerTitleStyle: {
          fontWeight: 'bold',
        },
        contentStyle: {
          backgroundColor: '#F3F4F6',
        },
      }}
    />
  );
}
