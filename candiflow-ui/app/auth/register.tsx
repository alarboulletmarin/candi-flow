import React from 'react';
import { View } from 'react-native';
import RegisterScreen from '../../src/components/features/auth/RegisterScreen';
import { Stack } from 'expo-router';

export default function RegisterPage() {
  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Inscription',
          headerShown: false,
        }} 
      />
      <View style={{ flex: 1 }}>
        <RegisterScreen />
      </View>
    </>
  );
}
