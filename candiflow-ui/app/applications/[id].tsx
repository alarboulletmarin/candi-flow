import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Stack, useRouter, useGlobalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { TouchableOpacity } from 'react-native-gesture-handler';
import ApplicationDetail from '../../src/components/features/applications/ApplicationDetail';

export default function ApplicationDetailScreen() {
  const router = useRouter();
  const { id } = useGlobalSearchParams();
  
  const applicationId = id ? parseInt(id as string) : 0;
  
  if (!applicationId) {
    router.replace('/applications');
    return null;
  }

  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Détails de la candidature',
          headerStyle: {
            backgroundColor: '#2563EB',
          },
          headerTintColor: '#fff',
          headerTitleStyle: {
            fontWeight: 'bold',
          },
          headerLeft: () => (
            <TouchableOpacity 
              style={styles.headerButton} 
              onPress={() => router.back()}
            >
              <Ionicons name="arrow-back" size={24} color="white" />
            </TouchableOpacity>
          ),
        }} 
      />
      <View style={styles.container}>
        <ApplicationDetail applicationId={applicationId} />
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3F4F6',
  },
  headerButton: {
    marginLeft: 8,
  },
});
