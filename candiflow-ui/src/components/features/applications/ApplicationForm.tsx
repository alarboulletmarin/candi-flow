import React, { useState, useEffect } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  ScrollView, 
  TouchableOpacity, 
  KeyboardAvoidingView, 
  Platform,
  ActivityIndicator,
  Alert,
  Animated
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useForm, Controller } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'expo-router';

import DateTimePicker from '@react-native-community/datetimepicker';

import TextInput from '../../ui/TextInput';
import Button from '../../ui/Button';
import { ApplicationFormData, Application } from '../../../types/application';
import applicationService from '../../../services/applicationService';

// Schéma de validation du formulaire avec Zod
const applicationSchema = z.object({
  company: z.string().min(1, 'Nom de l\'entreprise requis'),
  position: z.string().min(1, 'Intitulé du poste requis'),
  location: z.string().optional(),
  description: z.string().optional(),
  applicationDate: z.string().min(1, 'Date de candidature requise'),
  contactPerson: z.string().optional(),
  contactEmail: z.string().email('Email invalide').optional().or(z.literal('')),
  salary: z.string().optional(),
  notes: z.string().optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  source: z.string().optional(),
  url: z.string().url('URL invalide').optional().or(z.literal(''))
});

interface ApplicationFormProps {
  applicationId?: string;
  onSuccess?: () => void;
};

export default function ApplicationForm({ applicationId, onSuccess }: ApplicationFormProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(!!applicationId);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [dateValue, setDateValue] = useState(new Date());

  const { control, handleSubmit, setValue, reset, formState: { errors } } = useForm<ApplicationFormData>({
    resolver: zodResolver(applicationSchema),
    defaultValues: {
      company: '',
      position: '',
      location: '',
      description: '',
      applicationDate: new Date().toISOString().split('T')[0],
      contactPerson: '',
      contactEmail: '',
      salary: '',
      notes: '',
      priority: 'MEDIUM',
      source: '',
      url: ''
    }
  });

  // Charger les données de la candidature si on est en mode édition
  useEffect(() => {
    if (applicationId) {
      const loadApplication = async () => {
        try {
          setInitialLoading(true);
          const application = await applicationService.getApplicationById(applicationId);
          
          // Formater la date pour le formulaire
          const formattedDate = new Date(application.applicationDate).toISOString().split('T')[0];
          setDateValue(new Date(application.applicationDate));
          
          // Pré-remplir le formulaire avec les données existantes
          reset({
            company: application.company,
            position: application.position,
            location: application.location || '',
            description: application.description || '',
            applicationDate: formattedDate,
            contactPerson: application.contactPerson || '',
            contactEmail: application.contactEmail || '',
            salary: application.salary || '',
            notes: application.notes || '',
            priority: application.priority,
            source: application.source || '',
            url: application.url || ''
          });
        } catch (error) {
          console.error('Erreur lors du chargement de la candidature:', error);
          Alert.alert(
            'Erreur',
            'Impossible de charger les données de la candidature'
          );
          router.back();
        } finally {
          setInitialLoading(false);
        }
      };

      loadApplication();
    }
  }, [applicationId]);

  const onSubmit = async (data: ApplicationFormData) => {
    try {
      setLoading(true);
      
      if (applicationId) {
        // Mode édition
        await applicationService.updateApplication(applicationId, data);
        Alert.alert('Succès', 'Candidature mise à jour avec succès');
      } else {
        // Mode création
        await applicationService.createApplication(data);
        Alert.alert('Succès', 'Candidature créée avec succès');
      }
      
      if (onSuccess) {
        onSuccess();
      } else {
        router.back();
      }
    } catch (error) {
      console.error('Erreur lors de la sauvegarde:', error);
      Alert.alert(
        'Erreur',
        'Une erreur est survenue lors de la sauvegarde de la candidature'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleDateChange = (event: any, selectedDate?: Date) => {
    setShowDatePicker(false);
    if (selectedDate) {
      const formattedDate = selectedDate.toISOString().split('T')[0];
      setValue('applicationDate', formattedDate);
      setDateValue(selectedDate);
    }
  };

  if (initialLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#2563EB" />
        <Text style={styles.loadingText}>Chargement des données...</Text>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={100}
    >
      <ScrollView 
        contentContainerStyle={styles.scrollContainer}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.formContainer}>
          <TextInput
            control={control}
            name="company"
            label="Entreprise"
            placeholder="Nom de l'entreprise"
            error={errors.company?.message}
            leftIcon={<Ionicons name="business-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="position"
            label="Poste"
            placeholder="Intitulé du poste"
            error={errors.position?.message}
            leftIcon={<Ionicons name="briefcase-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="location"
            label="Lieu"
            placeholder="Ville, Pays ou Remote"
            error={errors.location?.message}
            leftIcon={<Ionicons name="location-outline" size={20} color="#6B7280" />}
          />

          <Controller
            control={control}
            name="applicationDate"
            render={({ field: { onChange, value } }) => (
              <View style={styles.datePickerContainer}>
                <Text style={styles.label}>Date de candidature</Text>
                <TouchableOpacity
                  style={styles.datePickerButton}
                  onPress={() => setShowDatePicker(true)}
                >
                  <Ionicons name="calendar-outline" size={20} color="#6B7280" />
                  <Text style={styles.datePickerText}>
                    {value ? new Date(value).toLocaleDateString() : 'Sélectionner une date'}
                  </Text>
                </TouchableOpacity>
                {errors.applicationDate && (
                  <Text style={styles.errorText}>{errors.applicationDate.message}</Text>
                )}
                {showDatePicker && (
                  <DateTimePicker
                    value={dateValue}
                    mode="date"
                    display="default"
                    onChange={handleDateChange}
                  />
                )}
              </View>
            )}
          />

          <TextInput
            control={control}
            name="contactPerson"
            label="Contact"
            placeholder="Nom du contact"
            error={errors.contactPerson?.message}
            leftIcon={<Ionicons name="person-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="contactEmail"
            label="Email du contact"
            placeholder="email@exemple.com"
            keyboardType="email-address"
            error={errors.contactEmail?.message}
            leftIcon={<Ionicons name="mail-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="salary"
            label="Salaire"
            placeholder="Ex: 45K€ - 55K€"
            error={errors.salary?.message}
            leftIcon={<Ionicons name="cash-outline" size={20} color="#6B7280" />}
          />
          
          <Controller
            control={control}
            name="priority"
            render={({ field: { onChange, value } }) => (
              <View style={styles.priorityContainer}>
                <Text style={styles.label}>Priorité</Text>
                <View style={styles.priorityButtons}>
                  <TouchableOpacity
                    style={[
                      styles.priorityButton,
                      value === 'LOW' && styles.priorityButtonLowSelected
                    ]}
                    onPress={() => onChange('LOW')}
                  >
                    <Text
                      style={[
                        styles.priorityButtonText,
                        value === 'LOW' && styles.priorityButtonTextSelected
                      ]}
                    >
                      Basse
                    </Text>
                  </TouchableOpacity>
                  
                  <TouchableOpacity
                    style={[
                      styles.priorityButton,
                      value === 'MEDIUM' && styles.priorityButtonMediumSelected
                    ]}
                    onPress={() => onChange('MEDIUM')}
                  >
                    <Text
                      style={[
                        styles.priorityButtonText,
                        value === 'MEDIUM' && styles.priorityButtonTextSelected
                      ]}
                    >
                      Moyenne
                    </Text>
                  </TouchableOpacity>
                  
                  <TouchableOpacity
                    style={[
                      styles.priorityButton,
                      value === 'HIGH' && styles.priorityButtonHighSelected
                    ]}
                    onPress={() => onChange('HIGH')}
                  >
                    <Text
                      style={[
                        styles.priorityButtonText,
                        value === 'HIGH' && styles.priorityButtonTextSelected
                      ]}
                    >
                      Haute
                    </Text>
                  </TouchableOpacity>
                </View>
                {errors.priority && (
                  <Text style={styles.errorText}>{errors.priority.message}</Text>
                )}
              </View>
            )}
          />
          
          <TextInput
            control={control}
            name="source"
            label="Source"
            placeholder="LinkedIn, Site web, etc."
            error={errors.source?.message}
            leftIcon={<Ionicons name="search-outline" size={20} color="#6B7280" />}
          />
          
          <TextInput
            control={control}
            name="url"
            label="URL"
            placeholder="https://example.com/job"
            keyboardType="url"
            error={errors.url?.message}
            leftIcon={<Ionicons name="link-outline" size={20} color="#6B7280" />}
          />
          
          <Controller
            control={control}
            name="description"
            render={({ field: { onChange, value } }) => (
              <View style={styles.textAreaContainer}>
                <Text style={styles.label}>Description</Text>
                <View style={styles.textAreaWrapper}>
                  <TextInput
                    control={control}
                    name="description"
                    multiline
                    numberOfLines={4}
                    style={styles.textArea}
                    placeholder="Description du poste ou notes personnelles"
                    error={errors.description?.message}
                  />
                </View>
              </View>
            )}
          />
          
          <Controller
            control={control}
            name="notes"
            render={({ field: { onChange, value } }) => (
              <View style={styles.textAreaContainer}>
                <Text style={styles.label}>Notes</Text>
                <View style={styles.textAreaWrapper}>
                  <TextInput
                    control={control}
                    name="notes"
                    multiline
                    numberOfLines={4}
                    style={styles.textArea}
                    placeholder="Notes supplémentaires"
                    error={errors.notes?.message}
                  />
                </View>
              </View>
            )}
          />
          
          <View style={styles.buttonsContainer}>
            <Button
              title="Annuler"
              variant="outline"
              onPress={() => router.back()}
              style={styles.cancelButton}
            />
            
            <Button
              title={applicationId ? "Mettre à jour" : "Créer"}
              onPress={handleSubmit(onSubmit)}
              isLoading={loading}
              style={styles.submitButton}
              size="large"
              fullWidth={false}
              icon={<Ionicons name="checkmark-circle" size={20} color="white" />}
            />
          </View>
          
          {/* Bouton flottant pour la soumission facile */}
          <TouchableOpacity 
            style={styles.floatingButton}
            onPress={handleSubmit(onSubmit)}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="white" size="small" />
            ) : (
              <Ionicons name="checkmark" size={24} color="white" />
            )}
          </TouchableOpacity>
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
    padding: 16,
  },
  formContainer: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 6,
    color: '#374151',
  },
  datePickerContainer: {
    marginBottom: 16,
  },
  datePickerButton: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  datePickerText: {
    marginLeft: 8,
    color: '#1F2937',
    fontSize: 16,
  },
  priorityContainer: {
    marginBottom: 16,
  },
  priorityButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  priorityButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    marginHorizontal: 4,
    alignItems: 'center',
  },
  priorityButtonLowSelected: {
    backgroundColor: '#10B981',
    borderColor: '#10B981',
  },
  priorityButtonMediumSelected: {
    backgroundColor: '#F59E0B',
    borderColor: '#F59E0B',
  },
  priorityButtonHighSelected: {
    backgroundColor: '#EF4444',
    borderColor: '#EF4444',
  },
  priorityButtonText: {
    color: '#6B7280',
    fontWeight: '500',
  },
  priorityButtonTextSelected: {
    color: 'white',
  },
  textAreaContainer: {
    marginBottom: 16,
  },
  textAreaWrapper: {
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 8,
  },
  textArea: {
    minHeight: 100,
    textAlignVertical: 'top',
    padding: 12,
  },
  buttonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 24,
    marginBottom: 60, // Espace pour le bouton flottant
  },
  cancelButton: {
    flex: 1,
    marginRight: 8,
  },
  submitButton: {
    flex: 2,
    marginLeft: 8,
    backgroundColor: '#2563EB',
  },
  floatingButton: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
    zIndex: 1000,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    color: '#6B7280',
  },
  errorText: {
    color: '#EF4444',
    fontSize: 12,
    marginTop: 4,
  },
});
