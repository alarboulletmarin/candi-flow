import React, { useState } from 'react';
import { StyleSheet, View, Text, TouchableOpacity, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter, Link } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

import TextInput from '../../ui/TextInput';
import Button from '../../ui/Button';
import { useAuth } from '../../../store/AuthContext';
import { UserRole } from '../../../types/user';

// Register form schema with Zod
const registerSchema = z.object({
  firstName: z.string().min(1, 'Prénom requis'),
  lastName: z.string().min(1, 'Nom requis'),
  email: z.string().email('Adresse email invalide'),
  password: z.string().min(6, 'Le mot de passe doit contenir au moins 6 caractères'),
  confirmPassword: z.string().min(6, 'La confirmation du mot de passe est requise'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Les mots de passe ne correspondent pas',
  path: ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function RegisterScreen() {
  const { register, error, isLoading, clearError } = useAuth();
  const router = useRouter();
  const [userRole, setUserRole] = useState<UserRole>(UserRole.CANDIDATE);
  
  const { control, handleSubmit, formState: { errors } } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  });

  const onSubmit = async (data: RegisterFormData) => {
    try {
      await register(
        data.email, 
        data.password, 
        data.firstName, 
        data.lastName, 
        userRole
      );
      router.replace('/');
    } catch (error) {
      console.error('Register error:', error);
    }
  };

  return (
    <KeyboardAvoidingView 
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 64 : 0}
    >
      <ScrollView 
        contentContainerStyle={styles.scrollContainer}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.headerContainer}>
          <Text style={styles.title}>CandiFlow</Text>
          <Text style={styles.subtitle}>Créez votre compte</Text>
        </View>

        {error && (
          <View style={styles.errorContainer}>
            <Ionicons name="alert-circle-outline" size={20} color="#EF4444" />
            <Text style={styles.errorText}>{error}</Text>
            <TouchableOpacity onPress={clearError}>
              <Ionicons name="close-outline" size={20} color="#EF4444" />
            </TouchableOpacity>
          </View>
        )}

        <View style={styles.formContainer}>
          <View style={styles.nameContainer}>
            <View style={styles.nameField}>
              <TextInput
                control={control}
                name="firstName"
                label="Prénom"
                placeholder="Prénom"
                error={errors.firstName?.message}
                leftIcon={<Ionicons name="person-outline" size={20} color="#6B7280" />}
              />
            </View>
            <View style={styles.nameField}>
              <TextInput
                control={control}
                name="lastName"
                label="Nom"
                placeholder="Nom"
                error={errors.lastName?.message}
                leftIcon={<Ionicons name="person-outline" size={20} color="#6B7280" />}
              />
            </View>
          </View>
          
          <TextInput
            control={control}
            name="email"
            label="Email"
            placeholder="votre@email.com"
            keyboardType="email-address"
            autoCapitalize="none"
            error={errors.email?.message}
            leftIcon={<Ionicons name="mail-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="password"
            label="Mot de passe"
            placeholder="Votre mot de passe"
            isPassword
            error={errors.password?.message}
            leftIcon={<Ionicons name="lock-closed-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="confirmPassword"
            label="Confirmer le mot de passe"
            placeholder="Confirmez votre mot de passe"
            isPassword
            error={errors.confirmPassword?.message}
            leftIcon={<Ionicons name="lock-closed-outline" size={20} color="#6B7280" />}
          />
          
          <View style={styles.roleContainer}>
            <Text style={styles.roleLabel}>Je suis :</Text>
            <View style={styles.roleButtons}>
              <TouchableOpacity
                style={[
                  styles.roleButton,
                  userRole === UserRole.CANDIDATE && styles.selectedRoleButton
                ]}
                onPress={() => setUserRole(UserRole.CANDIDATE)}
              >
                <Text style={[
                  styles.roleButtonText,
                  userRole === UserRole.CANDIDATE && styles.selectedRoleButtonText
                ]}>
                  Candidat
                </Text>
              </TouchableOpacity>
              
              <TouchableOpacity
                style={[
                  styles.roleButton,
                  userRole === UserRole.RECRUITER && styles.selectedRoleButton
                ]}
                onPress={() => setUserRole(UserRole.RECRUITER)}
              >
                <Text style={[
                  styles.roleButtonText,
                  userRole === UserRole.RECRUITER && styles.selectedRoleButtonText
                ]}>
                  Recruteur
                </Text>
              </TouchableOpacity>
            </View>
          </View>
          
          <Button
            title="S'inscrire"
            onPress={handleSubmit(onSubmit)}
            isLoading={isLoading}
            fullWidth
          />
          
          <View style={styles.loginContainer}>
            <Text style={styles.loginText}>Vous avez déjà un compte ?</Text>
            <Link href="../login" asChild>
              <TouchableOpacity>
                <Text style={styles.loginLink}>Se connecter</Text>
              </TouchableOpacity>
            </Link>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3F4F6',
  },
  scrollContainer: {
    flexGrow: 1,
    padding: 20,
  },
  headerContainer: {
    alignItems: 'center',
    marginBottom: 24,
    marginTop: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#2563EB',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6B7280',
  },
  formContainer: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  nameContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  nameField: {
    width: '48%',
  },
  errorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FEE2E2',
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },
  errorText: {
    color: '#EF4444',
    flex: 1,
    marginHorizontal: 8,
  },
  roleContainer: {
    marginBottom: 16,
  },
  roleLabel: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 8,
    color: '#374151',
  },
  roleButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  roleButton: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    marginHorizontal: 4,
    alignItems: 'center',
  },
  selectedRoleButton: {
    backgroundColor: '#2563EB',
    borderColor: '#2563EB',
  },
  roleButtonText: {
    color: '#6B7280',
    fontWeight: '500',
  },
  selectedRoleButtonText: {
    color: 'white',
  },
  loginContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 16,
  },
  loginText: {
    color: '#6B7280',
  },
  loginLink: {
    color: '#2563EB',
    fontWeight: '600',
    marginLeft: 4,
  },
});
