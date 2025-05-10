import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Stack, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import ApplicationList from '../../src/components/features/applications/ApplicationList';
import { TouchableOpacity } from 'react-native-gesture-handler';
import { Application } from '../../src/types/application';

export default function ApplicationsScreen() {
  const router = useRouter();

  const handleSelectApplication = (application: Application) => {
    router.push({
      pathname: `/applications/${application.id}`,
    });
  };

  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Mes candidatures',
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
        <ApplicationList onSelectApplication={handleSelectApplication} />
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
