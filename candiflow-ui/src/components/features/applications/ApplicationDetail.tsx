import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  ScrollView, 
  TouchableOpacity, 
  ActivityIndicator,
  Alert
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Application, StatusUpdate } from '../../../types/application';
import applicationService from '../../../services/applicationService';
import Button from '../../ui/Button';

type ApplicationDetailProps = {
  applicationId: number;
};

export default function ApplicationDetail({ applicationId }: ApplicationDetailProps) {
  const router = useRouter();
  const [application, setApplication] = useState<Application | null>(null);
  const [statusUpdates, setStatusUpdates] = useState<StatusUpdate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadApplicationData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Charger les détails de la candidature
      const applicationData = await applicationService.getApplicationById(applicationId);
      setApplication(applicationData);
      
      // Charger les mises à jour de statut
      const statusUpdatesData = await applicationService.getStatusUpdates(applicationId);
      setStatusUpdates(statusUpdatesData);
    } catch (err) {
      console.error('Error loading application details:', err);
      setError('Impossible de charger les détails de la candidature');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApplicationData();
  }, [applicationId]);

  const handleEdit = () => {
    router.push(`/applications/${applicationId}/edit`);
  };

  const handleAddStatusUpdate = () => {
    router.push(`/applications/${applicationId}/status/new`);
  };

  const handleDelete = async () => {
    Alert.alert(
      'Confirmation',
      'Êtes-vous sûr de vouloir supprimer cette candidature ?',
      [
        { text: 'Annuler', style: 'cancel' },
        {
          text: 'Supprimer',
          style: 'destructive',
          onPress: async () => {
            try {
              setLoading(true);
              await applicationService.deleteApplication(applicationId);
              Alert.alert('Succès', 'Candidature supprimée avec succès');
              router.replace('/applications');
            } catch (err) {
              console.error('Error deleting application:', err);
              Alert.alert('Erreur', 'Impossible de supprimer la candidature');
              setLoading(false);
            }
          }
        }
      ]
    );
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#2563EB" />
        <Text style={styles.loadingText}>Chargement des détails...</Text>
      </View>
    );
  }

  if (error || !application) {
    return (
      <View style={styles.errorContainer}>
        <Ionicons name="alert-circle-outline" size={50} color="#EF4444" />
        <Text style={styles.errorText}>{error || 'Candidature non trouvée'}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={loadApplicationData}>
          <Text style={styles.retryButtonText}>Réessayer</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Formater la date
  const formattedDate = new Date(application.applicationDate).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });

  return (
    <ScrollView style={styles.container}>
      {/* En-tête avec le statut actuel */}
      <View style={styles.header}>
        <View style={[styles.statusBadge, { backgroundColor: application.currentStatus?.color || '#e5e7eb' }]}>
          <Text style={styles.statusText}>{application.currentStatus?.name || 'Aucun statut'}</Text>
        </View>
      </View>
      
      {/* Informations principales */}
      <View style={styles.mainInfoContainer}>
        <Text style={styles.companyName}>{application.company}</Text>
        <Text style={styles.positionTitle}>{application.position}</Text>
        
        <View style={styles.detailRow}>
          <Ionicons name="location-outline" size={18} color="#6B7280" />
          <Text style={styles.detailText}>{application.location || 'Non spécifié'}</Text>
        </View>
        
        <View style={styles.detailRow}>
          <Ionicons name="calendar-outline" size={18} color="#6B7280" />
          <Text style={styles.detailText}>Postulé le {formattedDate}</Text>
        </View>
        
        {application.salary && (
          <View style={styles.detailRow}>
            <Ionicons name="cash-outline" size={18} color="#6B7280" />
            <Text style={styles.detailText}>{application.salary}</Text>
          </View>
        )}
        
        {application.priority && (
          <View style={styles.detailRow}>
            <View 
              style={[
                styles.priorityIndicator, 
                application.priority === 'HIGH' 
                  ? styles.highPriority 
                  : application.priority === 'MEDIUM' 
                    ? styles.mediumPriority 
                    : styles.lowPriority
              ]}
            />
            <Text style={styles.detailText}>
              {application.priority === 'HIGH' 
                ? 'Priorité haute' 
                : application.priority === 'MEDIUM' 
                  ? 'Priorité moyenne' 
                  : 'Priorité basse'}
            </Text>
          </View>
        )}
      </View>
      
      {/* Contact */}
      {(application.contactPerson || application.contactEmail) && (
        <View style={styles.sectionContainer}>
          <Text style={styles.sectionTitle}>Contact</Text>
          {application.contactPerson && (
            <View style={styles.detailRow}>
              <Ionicons name="person-outline" size={18} color="#6B7280" />
              <Text style={styles.detailText}>{application.contactPerson}</Text>
            </View>
          )}
          {application.contactEmail && (
            <View style={styles.detailRow}>
              <Ionicons name="mail-outline" size={18} color="#6B7280" />
              <Text style={styles.detailText}>{application.contactEmail}</Text>
            </View>
          )}
        </View>
      )}
      
      {/* Description */}
      {application.description && (
        <View style={styles.sectionContainer}>
          <Text style={styles.sectionTitle}>Description</Text>
          <Text style={styles.descriptionText}>{application.description}</Text>
        </View>
      )}
      
      {/* Notes */}
      {application.notes && (
        <View style={styles.sectionContainer}>
          <Text style={styles.sectionTitle}>Notes</Text>
          <Text style={styles.descriptionText}>{application.notes}</Text>
        </View>
      )}
      
      {/* Source/URL */}
      {(application.source || application.url) && (
        <View style={styles.sectionContainer}>
          <Text style={styles.sectionTitle}>Source</Text>
          {application.source && (
            <View style={styles.detailRow}>
              <Ionicons name="search-outline" size={18} color="#6B7280" />
              <Text style={styles.detailText}>{application.source}</Text>
            </View>
          )}
          {application.url && (
            <View style={styles.detailRow}>
              <Ionicons name="link-outline" size={18} color="#6B7280" />
              <Text style={styles.detailText}>{application.url}</Text>
            </View>
          )}
        </View>
      )}
      
      {/* Timeline des statuts */}
      <View style={styles.timelineContainer}>
        <View style={styles.timelineHeader}>
          <Text style={styles.sectionTitle}>Timeline des statuts</Text>
          <TouchableOpacity 
            style={styles.addStatusButton}
            onPress={handleAddStatusUpdate}
          >
            <Ionicons name="add-circle-outline" size={20} color="#2563EB" />
            <Text style={styles.addStatusText}>Ajouter</Text>
          </TouchableOpacity>
        </View>
        
        {statusUpdates.length === 0 ? (
          <View style={styles.emptyTimelineContainer}>
            <Ionicons name="time-outline" size={40} color="#d1d5db" />
            <Text style={styles.emptyTimelineText}>Aucune mise à jour de statut</Text>
          </View>
        ) : (
          <View style={styles.timeline}>
            {statusUpdates.map((update, index) => (
              <View key={update.id} style={styles.timelineItem}>
                <View style={styles.timelineLine}>
                  <View 
                    style={[
                      styles.timelineDot,
                      { backgroundColor: update.status.color || '#6B7280' }
                    ]} 
                  />
                  {index < statusUpdates.length - 1 && (
                    <View style={styles.timelineConnector} />
                  )}
                </View>
                <View style={styles.timelineContent}>
                  <Text style={styles.timelineStatus}>{update.status.name}</Text>
                  <Text style={styles.timelineDate}>
                    {new Date(update.createdAt).toLocaleDateString('fr-FR', {
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric'
                    })}
                  </Text>
                  {update.notes && (
                    <Text style={styles.timelineNotes}>{update.notes}</Text>
                  )}
                </View>
              </View>
            ))}
          </View>
        )}
      </View>
      
      {/* Boutons d'action */}
      <View style={styles.actionButtonsContainer}>
        <Button
          title="Modifier"
          variant="outline"
          onPress={handleEdit}
          icon={<Ionicons name="create-outline" size={20} color="#2563EB" />}
          style={styles.actionButton}
        />
        <Button
          title="Supprimer"
          variant="outline"
          onPress={handleDelete}
          icon={<Ionicons name="trash-outline" size={20} color="#EF4444" />}
          style={[styles.actionButton, styles.deleteButton]}
        />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3F4F6',
  },
  header: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
    alignItems: 'flex-end',
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 14,
    color: 'white',
    fontWeight: '600',
  },
  mainInfoContainer: {
    backgroundColor: 'white',
    padding: 16,
    marginTop: 12,
    marginHorizontal: 12,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  companyName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1F2937',
    marginBottom: 4,
  },
  positionTitle: {
    fontSize: 22,
    fontWeight: '600',
    color: '#2563EB',
    marginBottom: 16,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  detailText: {
    marginLeft: 8,
    color: '#6B7280',
    fontSize: 15,
  },
  priorityIndicator: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  highPriority: {
    backgroundColor: '#EF4444',
  },
  mediumPriority: {
    backgroundColor: '#F59E0B',
  },
  lowPriority: {
    backgroundColor: '#10B981',
  },
  sectionContainer: {
    backgroundColor: 'white',
    padding: 16,
    marginTop: 12,
    marginHorizontal: 12,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1F2937',
    marginBottom: 12,
  },
  descriptionText: {
    color: '#4B5563',
    lineHeight: 20,
  },
  timelineContainer: {
    backgroundColor: 'white',
    padding: 16,
    marginTop: 12,
    marginHorizontal: 12,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
    marginBottom: 12,
  },
  timelineHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  addStatusButton: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  addStatusText: {
    color: '#2563EB',
    marginLeft: 4,
    fontWeight: '500',
  },
  emptyTimelineContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 24,
  },
  emptyTimelineText: {
    color: '#6B7280',
    marginTop: 8,
  },
  timeline: {
    marginLeft: 8,
  },
  timelineItem: {
    flexDirection: 'row',
    marginBottom: 20,
  },
  timelineLine: {
    width: 24,
    alignItems: 'center',
  },
  timelineDot: {
    width: 16,
    height: 16,
    borderRadius: 8,
  },
  timelineConnector: {
    width: 2,
    height: '100%',
    backgroundColor: '#D1D5DB',
    position: 'absolute',
    top: 16,
    left: 7,
  },
  timelineContent: {
    flex: 1,
    paddingLeft: 8,
    marginTop: -4,
  },
  timelineStatus: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1F2937',
  },
  timelineDate: {
    fontSize: 12,
    color: '#6B7280',
    marginBottom: 6,
  },
  timelineNotes: {
    color: '#4B5563',
  },
  actionButtonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    marginBottom: 24,
  },
  actionButton: {
    flex: 1,
    marginHorizontal: 4,
  },
  deleteButton: {
    borderColor: '#EF4444',
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
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    color: '#6B7280',
    fontSize: 16,
    textAlign: 'center',
    marginVertical: 16,
  },
  retryButton: {
    backgroundColor: '#2563EB',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 8,
  },
  retryButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
});
