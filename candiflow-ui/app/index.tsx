import React, { useEffect } from 'react';
import { Text, View, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuth } from '../src/store/AuthContext';
import { UserRole } from '../src/types/user';

export default function Index() {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;
    
    // Utiliser un court délai pour permettre l'initialisation complète
    const redirectTimer = setTimeout(() => {
      if (!user) {
        // Si l'utilisateur n'est pas authentifié, rediriger vers login
        router.navigate('auth/login');
      } else {
        // Rediriger vers le tableau de bord approprié selon le rôle
        if (user.role === UserRole.CANDIDATE) {
          router.navigate('dashboard/candidate');
        } else if (user.role === UserRole.RECRUITER) {
          router.navigate('dashboard/recruiter');
        }
      }
    }, 100);
    
    return () => clearTimeout(redirectTimer);
  }, [user, isLoading]);

  return (
    <View
      style={{
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F3F4F6',
      }}
    >
      <ActivityIndicator size="large" color="#2563EB" />
      <Text style={{ marginTop: 16, color: '#6B7280' }}>Chargement...</Text>
    </View>
  );
}
