import React, { useState, useEffect } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  TouchableOpacity, 
  ActivityIndicator,
  Alert,
  ScrollView
} from 'react-native';
import { Stack, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import * as Calendar from 'expo-calendar';
import { useAuth } from '../../src/store/AuthContext';
import applicationService from '../../src/services/applicationService';
import { Application } from '../../src/types/application';
import Button from '../../src/components/ui/Button';

export default function ExportScreen() {
  const router = useRouter();
  const { user } = useAuth();
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [calendarPermission, setCalendarPermission] = useState(false);
  const [calendarSyncing, setCalendarSyncing] = useState(false);

  useEffect(() => {
    const fetchApplications = async () => {
      try {
        setLoading(true);
        const data = await applicationService.getUserApplications();
        setApplications(data);
      } catch (error) {
        console.error('Erreur lors du chargement des candidatures:', error);
        Alert.alert('Erreur', 'Impossible de charger les candidatures');
      } finally {
        setLoading(false);
      }
    };

    const checkCalendarPermission = async () => {
      try {
        const { status } = await Calendar.requestCalendarPermissionsAsync();
        setCalendarPermission(status === 'granted');
      } catch (error) {
        console.error('Erreur lors de la vérification des permissions du calendrier:', error);
      }
    };

    fetchApplications();
    checkCalendarPermission();
  }, []);

  const exportToCSV = async () => {
    try {
      setExporting(true);
      
      // Création de l'en-tête CSV
      let csvContent = 'Entreprise,Poste,Lieu,Date de candidature,Statut actuel,Priorité,Contact,Email,URL\n';
      
      // Ajout des données
      applications.forEach(app => {
        const currentStatus = app.statusUpdates && app.statusUpdates.length > 0 
          ? app.statusUpdates[app.statusUpdates.length - 1].status.name 
          : 'APPLIED';
        
        // Échapper les virgules dans les champs
        const escapeCsv = (field: string) => {
          if (!field) return '';
          return `"${field.replace(/"/g, '""')}"`;
        };
        
        csvContent += [
          escapeCsv(app.company),
          escapeCsv(app.position),
          escapeCsv(app.location || ''),
          new Date(app.applicationDate).toLocaleDateString(),
          currentStatus,
          app.priority,
          escapeCsv(app.contactPerson || ''),
          escapeCsv(app.contactEmail || ''),
          escapeCsv(app.url || '')
        ].join(',') + '\n';
      });
      
      // Enregistrement du fichier
      const fileName = `candidatures_${new Date().toISOString().slice(0, 10)}.csv`;
      const fileUri = `${FileSystem.documentDirectory}${fileName}`;
      
      await FileSystem.writeAsStringAsync(fileUri, csvContent, {
        encoding: FileSystem.EncodingType.UTF8
      });
      
      // Partage du fichier
      await Sharing.shareAsync(fileUri, {
        mimeType: 'text/csv',
        dialogTitle: 'Exporter les candidatures'
      });
      
      Alert.alert('Succès', 'Vos candidatures ont été exportées avec succès');
    } catch (error) {
      console.error('Erreur lors de l\'exportation:', error);
      Alert.alert('Erreur', 'Impossible d\'exporter les candidatures');
    } finally {
      setExporting(false);
    }
  };

  const syncWithCalendar = async () => {
    if (!calendarPermission) {
      Alert.alert(
        'Permission requise', 
        'Veuillez autoriser l\'accès au calendrier pour utiliser cette fonctionnalité',
        [
          { text: 'Annuler', style: 'cancel' },
          { 
            text: 'Paramètres', 
            onPress: () => {
              // Rediriger vers les paramètres de l'application
              // Note: cela ne fonctionne que sur les appareils réels
            } 
          }
        ]
      );
      return;
    }
    
    try {
      setCalendarSyncing(true);
      
      // Récupérer les calendriers disponibles
      const calendars = await Calendar.getCalendarsAsync(Calendar.EntityTypes.EVENT);
      const defaultCalendar = calendars.find(cal => cal.isPrimary) || calendars[0];
      
      if (!defaultCalendar) {
        throw new Error('Aucun calendrier disponible');
      }
      
      // Récupérer les entretiens à venir
      const interviews = [];
      for (const app of applications) {
        if (!app.statusUpdates) continue;
        
        for (const update of app.statusUpdates) {
          if (
            update.status.name === 'INTERVIEW_SCHEDULED' && 
            update.interviewDate && 
            new Date(update.interviewDate) > new Date()
          ) {
            interviews.push({
              id: app.id,
              company: app.company,
              position: app.position,
              date: new Date(update.interviewDate),
              notes: update.notes
            });
          }
        }
      }
      
      if (interviews.length === 0) {
        Alert.alert('Information', 'Aucun entretien à synchroniser avec le calendrier');
        setCalendarSyncing(false);
        return;
      }
      
      // Ajouter les entretiens au calendrier
      let addedCount = 0;
      for (const interview of interviews) {
        const eventId = await Calendar.createEventAsync(defaultCalendar.id, {
          title: `Entretien - ${interview.company}`,
          startDate: interview.date,
          endDate: new Date(interview.date.getTime() + 60 * 60 * 1000), // +1 heure
          notes: `Poste: ${interview.position}\n${interview.notes || ''}`,
          alarms: [{ relativeOffset: -60 }] // Rappel 1 heure avant
        });
        
        if (eventId) {
          addedCount++;
        }
      }
      
      Alert.alert(
        'Synchronisation terminée', 
        `${addedCount} entretien(s) ajouté(s) à votre calendrier`
      );
    } catch (error) {
      console.error('Erreur lors de la synchronisation avec le calendrier:', error);
      Alert.alert('Erreur', 'Impossible de synchroniser avec le calendrier');
    } finally {
      setCalendarSyncing(false);
    }
  };

  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Exporter les données',
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
      
      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#2563EB" />
          <Text style={styles.loadingText}>Chargement des données...</Text>
        </View>
      ) : (
        <ScrollView style={styles.container}>
          <View style={styles.card}>
            <Text style={styles.title}>Exporter vos candidatures</Text>
            <Text style={styles.description}>
              Exportez vos données de candidature pour les utiliser dans d'autres applications ou pour garder une sauvegarde.
            </Text>
            
            <View style={styles.section}>
              <View style={styles.sectionHeader}>
                <Ionicons name="document-text-outline" size={24} color="#2563EB" />
                <Text style={styles.sectionTitle}>Exporter au format CSV</Text>
              </View>
              <Text style={styles.sectionDescription}>
                Exportez vos candidatures dans un fichier CSV compatible avec Excel, Google Sheets et autres tableurs.
              </Text>
              <Button
                title="Exporter en CSV"
                onPress={exportToCSV}
                isLoading={exporting}
                icon={<Ionicons name="download-outline" size={20} color="white" />}
                style={styles.button}
              />
            </View>
            
            <View style={styles.section}>
              <View style={styles.sectionHeader}>
                <Ionicons name="calendar-outline" size={24} color="#2563EB" />
                <Text style={styles.sectionTitle}>Synchroniser avec le calendrier</Text>
              </View>
              <Text style={styles.sectionDescription}>
                Ajoutez automatiquement vos entretiens à venir dans le calendrier de votre appareil.
              </Text>
              <Button
                title="Synchroniser les entretiens"
                onPress={syncWithCalendar}
                isLoading={calendarSyncing}
                icon={<Ionicons name="sync-outline" size={20} color="white" />}
                style={styles.button}
                variant={calendarPermission ? 'primary' : 'secondary'}
              />
              {!calendarPermission && (
                <Text style={styles.warningText}>
                  Veuillez autoriser l'accès au calendrier pour utiliser cette fonctionnalité.
                </Text>
              )}
            </View>
            
            <View style={styles.statsContainer}>
              <View style={styles.statCard}>
                <Text style={styles.statNumber}>{applications.length}</Text>
                <Text style={styles.statLabel}>Candidatures</Text>
              </View>
              
              <View style={styles.statCard}>
                <Text style={styles.statNumber}>
                  {applications.filter(app => 
                    app.statusUpdates && 
                    app.statusUpdates.some(update => 
                      update.status.name === 'INTERVIEW_SCHEDULED' && 
                      new Date(update.interviewDate) > new Date()
                    )
                  ).length}
                </Text>
                <Text style={styles.statLabel}>Entretiens à venir</Text>
              </View>
            </View>
          </View>
        </ScrollView>
      )}
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
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    color: '#6B7280',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    margin: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1F2937',
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    color: '#6B7280',
    marginBottom: 24,
  },
  section: {
    marginBottom: 24,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1F2937',
    marginLeft: 8,
  },
  sectionDescription: {
    fontSize: 14,
    color: '#6B7280',
    marginBottom: 16,
  },
  button: {
    marginVertical: 8,
  },
  warningText: {
    fontSize: 12,
    color: '#EF4444',
    marginTop: 8,
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 16,
    paddingTop: 16,
    borderTopWidth: 1,
    borderTopColor: '#E5E7EB',
  },
  statCard: {
    flex: 1,
    alignItems: 'center',
    padding: 12,
  },
  statNumber: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#2563EB',
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 12,
    color: '#6B7280',
    textAlign: 'center',
  },
});
