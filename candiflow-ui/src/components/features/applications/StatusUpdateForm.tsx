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
  Alert
} from 'react-native';
import { useForm, Controller } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

import TextInput from '../../ui/TextInput';
import Button from '../../ui/Button';
import { StatusUpdateFormData, ApplicationStatus } from '../../../types/application';
import applicationService from '../../../services/applicationService';

// Schéma de validation pour le formulaire de mise à jour de statut
const statusUpdateSchema = z.object({
  statusId: z.number().positive('Veuillez sélectionner un statut'),
  notes: z.string().optional(),
});

type StatusUpdateFormProps = {
  applicationId: number;
  statusUpdateId?: number;
  onSuccess?: () => void;
};

export default function StatusUpdateForm({ applicationId, statusUpdateId, onSuccess }: StatusUpdateFormProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [statuses, setStatuses] = useState<ApplicationStatus[]>([]);
  const [selectedStatus, setSelectedStatus] = useState<ApplicationStatus | null>(null);

  const { control, handleSubmit, setValue, formState: { errors } } = useForm<StatusUpdateFormData>({
    resolver: zodResolver(statusUpdateSchema),
    defaultValues: {
      statusId: 0,
      notes: '',
    }
  });

  // Chargement des statuts disponibles et des données existantes si en mode édition
  useEffect(() => {
    const loadData = async () => {
      try {
        setInitialLoading(true);
        
        // Chargement des statuts disponibles
        const statusesData = await applicationService.getApplicationStatuses();
        setStatuses(statusesData);
        
        // Si on est en mode édition, charger la mise à jour existante
        if (statusUpdateId) {
          const statusUpdates = await applicationService.getStatusUpdates(applicationId);
          const currentStatusUpdate = statusUpdates.find(update => update.id === statusUpdateId);
          
          if (currentStatusUpdate) {
            setValue('statusId', currentStatusUpdate.statusId);
            setValue('notes', currentStatusUpdate.notes || '');
            setSelectedStatus(currentStatusUpdate.status);
          }
        }
      } catch (error) {
        console.error('Erreur lors du chargement des données:', error);
        Alert.alert(
          'Erreur',
          'Impossible de charger les données nécessaires'
        );
      } finally {
        setInitialLoading(false);
      }
    };

    loadData();
  }, [applicationId, statusUpdateId]);

  const handleStatusSelect = (status: ApplicationStatus) => {
    setSelectedStatus(status);
    setValue('statusId', status.id);
  };

  const onSubmit = async (data: StatusUpdateFormData) => {
    try {
      setLoading(true);
      
      if (statusUpdateId) {
        // Mode édition
        await applicationService.updateStatusUpdate(applicationId, statusUpdateId, data);
        Alert.alert('Succès', 'Statut mis à jour avec succès');
      } else {
        // Mode création
        await applicationService.createStatusUpdate(applicationId, data);
        Alert.alert('Succès', 'Statut ajouté avec succès');
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
        'Une erreur est survenue lors de la sauvegarde du statut'
      );
    } finally {
      setLoading(false);
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
    >
      <ScrollView 
        contentContainerStyle={styles.scrollContainer}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.formContainer}>
          <Text style={styles.label}>Statut</Text>
          <ScrollView 
            horizontal 
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.statusesContainer}
          >
            {statuses.map(status => (
              <TouchableOpacity
                key={status.id}
                style={[
                  styles.statusOption,
                  { backgroundColor: status.color },
                  selectedStatus?.id === status.id && styles.selectedStatus
                ]}
                onPress={() => handleStatusSelect(status)}
              >
                <Text style={styles.statusText}>{status.name}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
          {errors.statusId && (
            <Text style={styles.errorText}>{errors.statusId.message}</Text>
          )}
          
          <View style={styles.notesContainer}>
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
                      placeholder="Ajouter des notes ou commentaires sur ce changement de statut"
                      error={errors.notes?.message}
                    />
                  </View>
                </View>
              )}
            />
          </View>
          
          <View style={styles.buttonsContainer}>
            <Button
              title="Annuler"
              variant="outline"
              onPress={() => router.back()}
              style={styles.cancelButton}
            />
            
            <Button
              title={statusUpdateId ? "Mettre à jour" : "Ajouter"}
              onPress={handleSubmit(onSubmit)}
              isLoading={loading}
              style={styles.submitButton}
            />
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
  statusesContainer: {
    flexDirection: 'row',
    paddingVertical: 12,
  },
  statusOption: {
    marginRight: 10,
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
    minWidth: 100,
    alignItems: 'center',
  },
  selectedStatus: {
    borderWidth: 2,
    borderColor: '#1F2937',
  },
  statusText: {
    color: 'white',
    fontWeight: '500',
  },
  notesContainer: {
    marginTop: 20,
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
  },
  cancelButton: {
    flex: 1,
    marginRight: 8,
  },
  submitButton: {
    flex: 1,
    marginLeft: 8,
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
