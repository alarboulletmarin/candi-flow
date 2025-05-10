import React from 'react';
import { View } from 'react-native';
import LoginScreen from '../../src/components/features/auth/LoginScreen';
import { Stack } from 'expo-router';

export default function LoginPage() {
  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Connexion',
          headerShown: false,
        }} 
      />
      <View style={{ flex: 1 }}>
        <LoginScreen />
      </View>
    </>
  );
}
