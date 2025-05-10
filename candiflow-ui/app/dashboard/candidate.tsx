import React, { useState, useEffect } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  ScrollView, 
  TouchableOpacity, 
  ActivityIndicator, 
  FlatList,
  Image
} from 'react-native';
import { Stack, useRouter } from 'expo-router';
import { useAuth } from '../../src/store/AuthContext';
import { Ionicons } from '@expo/vector-icons';
import { BarChart, LineChart, PieChart } from 'react-native-chart-kit';
import { Dimensions } from 'react-native';
import applicationService from '../../src/services/applicationService';
import { Application } from '../../src/types/application';
import StatusBreadcrumb from '../../src/components/features/applications/StatusBreadcrumb';
// Fonction de formatage personnalisée pour éviter les problèmes avec date-fns
const formatDate = (date: Date, format: string): string => {
  if (format === 'MMM yyyy') {
    const months = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sept', 'Oct', 'Nov', 'Déc'];
    return `${months[date.getMonth()]} ${date.getFullYear()}`;
  } else if (format === 'dd/MM/yyyy') {
    return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`;
  }
  return date.toLocaleDateString();
};

export default function CandidateDashboard() {
  const { user } = useAuth();
  const router = useRouter();
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    total: 0,
    inProgress: 0,
    upcoming: 0,
    byStatus: {} as Record<string, number>,
    byMonth: {} as Record<string, number>,
    conversionRates: {} as Record<string, number>,
    recentApplications: [] as Application[],
    upcomingInterviews: [] as any[]
  });
  
  const screenWidth = Dimensions.get('window').width - 40;

  useEffect(() => {
    const fetchApplications = async () => {
      try {
        setLoading(true);
        const data = await applicationService.getUserApplications();
        
        // Vérifier que data est bien un tableau
        if (!Array.isArray(data)) {
          console.error('Les données reçues ne sont pas un tableau:', data);
          setApplications([]);
          setStats({
            total: 0,
            inProgress: 0,
            upcoming: 0,
            byStatus: {},
            byMonth: {},
            conversionRates: {},
            recentApplications: [],
            upcomingInterviews: []
          });
          setLoading(false);
          return;
        }
        
        setApplications(data);
        
        // Calculer les statistiques
        const today = new Date();
        const inProgress = data.filter(app => {
          if (!app.statusUpdates) return true;
          return !app.statusUpdates.some(status => 
            status.status.name === 'ACCEPTED' || status.status.name === 'REJECTED'
          );
        }).length;
        
        // Entretiens à venir
        const upcomingInterviews = [];
        data.forEach(app => {
          if (!app.statusUpdates) return;
          
          app.statusUpdates.forEach(update => {
            if (update.status.name === 'INTERVIEW_SCHEDULED' && new Date(update.interviewDate) > today) {
              upcomingInterviews.push({
                id: app.id,
                company: app.company,
                position: app.position,
                date: new Date(update.interviewDate),
                notes: update.notes
              });
            }
          });
        });
        
        // Trier les entretiens par date
        upcomingInterviews.sort((a, b) => a.date.getTime() - b.date.getTime());
        
        // Statistiques par statut
        const byStatus: Record<string, number> = {};
        data.forEach(app => {
          if (!app.statusUpdates || app.statusUpdates.length === 0) {
            byStatus['APPLIED'] = (byStatus['APPLIED'] || 0) + 1;
            return;
          }
          
          const currentStatus = app.statusUpdates[app.statusUpdates.length - 1].status.name;
          byStatus[currentStatus] = (byStatus[currentStatus] || 0) + 1;
        });
        
        // Statistiques par mois
        const byMonth: Record<string, number> = {};
        data.forEach(app => {
          const month = formatDate(new Date(app.applicationDate), 'MMM yyyy');
          byMonth[month] = (byMonth[month] || 0) + 1;
        });
        
        // Calculer les taux de conversion entre les statuts
        const conversionRates: Record<string, number> = {};
        const statusFlow = [
          'APPLIED',
          'PHONE_SCREEN',
          'INTERVIEW_SCHEDULED',
          'TECHNICAL_INTERVIEW',
          'FINAL_INTERVIEW',
          'OFFER_RECEIVED',
          'ACCEPTED'
        ];
        
        for (let i = 0; i < statusFlow.length - 1; i++) {
          const currentStatus = statusFlow[i];
          const nextStatus = statusFlow[i + 1];
          
          const currentCount = byStatus[currentStatus] || 0;
          const nextCount = byStatus[nextStatus] || 0;
          
          if (currentCount > 0) {
            conversionRates[`${currentStatus}_to_${nextStatus}`] = Math.round((nextCount / currentCount) * 100);
          } else {
            conversionRates[`${currentStatus}_to_${nextStatus}`] = 0;
          }
        }
        
        // Récupérer les candidatures récentes (5 dernières)
        const recentApplications = [...data]
          .sort((a, b) => new Date(b.applicationDate).getTime() - new Date(a.applicationDate).getTime())
          .slice(0, 5);
        
        setStats({
          total: data.length,
          inProgress,
          upcoming: upcomingInterviews.length,
          byStatus,
          byMonth,
          conversionRates,
          recentApplications,
          upcomingInterviews
        });
      } catch (error) {
        console.error('Erreur lors du chargement des candidatures:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchApplications();
  }, []);
  
  const navigateToApplications = () => {
    router.push('/applications');
  };
  
  const navigateToNewApplication = () => {
    router.push('/applications/new');
  };
  
  const navigateToCalendar = () => {
    router.push('/calendar');
  };
  
  const navigateToApplication = (id: string) => {
    router.push(`/applications/${id}`);
  };
  
  const navigateToExport = () => {
    router.push('/applications/export');
  };

  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Tableau de bord',
          headerShown: true,
          headerStyle: {
            backgroundColor: '#2563EB',
          },
          headerTintColor: '#fff',
          headerTitleStyle: {
            fontWeight: 'bold',
          },
        }} 
      />
      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#2563EB" />
          <Text style={styles.loadingText}>Chargement des données...</Text>
        </View>
      ) : (
        <ScrollView style={styles.container}>
          <View style={styles.header}>
            <Text style={styles.greeting}>Bonjour, {user?.name} 👋</Text>
            <Text style={styles.subtitle}>Gérez vos candidatures et restez organisé</Text>
          </View>

          <View style={styles.statsContainer}>
            <View style={styles.statCard}>
              <Text style={styles.statNumber}>{stats.inProgress}</Text>
              <Text style={styles.statLabel}>Candidatures en cours</Text>
            </View>
            <View style={styles.statCard}>
              <Text style={styles.statNumber}>{stats.upcoming}</Text>
              <Text style={styles.statLabel}>Entretiens à venir</Text>
            </View>
            <View style={styles.statCard}>
              <Text style={styles.statNumber}>{stats.total}</Text>
              <Text style={styles.statLabel}>Total</Text>
            </View>
          </View>

          <View style={styles.actionsContainer}>
            <TouchableOpacity style={styles.actionButton} onPress={navigateToNewApplication}>
              <Ionicons name="add-circle" size={24} color="#2563EB" />
              <Text style={styles.actionButtonText}>Nouvelle candidature</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionButton} onPress={navigateToApplications}>
              <Ionicons name="search" size={24} color="#2563EB" />
              <Text style={styles.actionButtonText}>Rechercher</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionButton} onPress={navigateToCalendar}>
              <Ionicons name="calendar" size={24} color="#2563EB" />
              <Text style={styles.actionButtonText}>Calendrier</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionButton} onPress={navigateToExport}>
              <Ionicons name="download" size={24} color="#2563EB" />
              <Text style={styles.actionButtonText}>Exporter</Text>
            </TouchableOpacity>
          </View>

          {/* Entretiens à venir */}
          <View style={styles.sectionContainer}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Entretiens à venir</Text>
              <TouchableOpacity onPress={navigateToCalendar}>
                <Text style={styles.seeAllText}>Voir calendrier</Text>
              </TouchableOpacity>
            </View>
            
            {stats.upcomingInterviews.length > 0 ? (
              <View style={styles.calendarContainer}>
                {stats.upcomingInterviews.map((interview, index) => (
                  <TouchableOpacity 
                    key={`interview-${index}`}
                    style={styles.interviewCard}
                    onPress={() => navigateToApplication(interview.id)}
                  >
                    <View style={styles.interviewDateContainer}>
                      <Text style={styles.interviewDay}>{interview.date.getDate()}</Text>
                      <Text style={styles.interviewMonth}>{formatDate(interview.date, 'MMM')}</Text>
                    </View>
                    <View style={styles.interviewDetails}>
                      <Text style={styles.interviewCompany}>{interview.company}</Text>
                      <Text style={styles.interviewPosition}>{interview.position}</Text>
                      <Text style={styles.interviewTime}>
                        {interview.date.getHours()}:{String(interview.date.getMinutes()).padStart(2, '0')}
                      </Text>
                    </View>
                    <Ionicons name="chevron-forward" size={20} color="#6B7280" />
                  </TouchableOpacity>
                ))}
              </View>
            ) : (
              <View style={styles.emptyStateContainer}>
                <Ionicons name="calendar-outline" size={40} color="#D1D5DB" />
                <Text style={styles.emptyStateText}>Aucun entretien à venir</Text>
              </View>
            )}
          </View>
          
          {/* Candidatures récentes */}
          <View style={styles.sectionContainer}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Candidatures récentes</Text>
              <TouchableOpacity onPress={navigateToApplications}>
                <Text style={styles.seeAllText}>Voir tout</Text>
              </TouchableOpacity>
            </View>
            
            {stats.recentApplications.length > 0 ? (
              <View>
                {stats.recentApplications.map((app) => (
                  <TouchableOpacity 
                    key={`app-${app.id}`}
                    style={styles.applicationCard}
                    onPress={() => navigateToApplication(app.id)}
                  >
                    <View style={styles.applicationCardHeader}>
                      <View>
                        <Text style={styles.applicationCompany}>{app.company}</Text>
                        <Text style={styles.applicationPosition}>{app.position}</Text>
                      </View>
                      <Text style={styles.applicationDate}>
                        {formatDate(new Date(app.applicationDate), 'dd/MM/yyyy')}
                      </Text>
                    </View>
                    
                    {app.statusUpdates && app.statusUpdates.length > 0 && (
                      <View style={styles.statusContainer}>
                        <StatusBreadcrumb 
                          statusUpdates={app.statusUpdates} 
                          compact={true} 
                        />
                      </View>
                    )}
                  </TouchableOpacity>
                ))}
              </View>
            ) : (
              <View style={styles.emptyStateContainer}>
                <Ionicons name="document-outline" size={40} color="#D1D5DB" />
                <Text style={styles.emptyStateText}>Aucune candidature récente</Text>
              </View>
            )}
          </View>
          
          {/* Graphique des candidatures par statut */}
          {Object.keys(stats.byStatus).length > 0 && (
            <View style={styles.chartContainer}>
              <Text style={styles.chartTitle}>Candidatures par statut</Text>
              <PieChart
                data={Object.entries(stats.byStatus).map(([status, count], index) => ({
                  name: status,
                  count,
                  color: [
                    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'
                  ][index % 6],
                  legendFontColor: '#7F7F7F',
                  legendFontSize: 12
                }))}
                width={screenWidth}
                height={220}
                chartConfig={{
                  backgroundColor: '#ffffff',
                  backgroundGradientFrom: '#ffffff',
                  backgroundGradientTo: '#ffffff',
                  decimalPlaces: 0,
                  color: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
                }}
                accessor="count"
                backgroundColor="transparent"
                paddingLeft="15"
                absolute
              />
            </View>
          )}

          {/* Graphique des candidatures par mois */}
          {Object.keys(stats.byMonth).length > 0 && (
            <View style={styles.chartContainer}>
              <Text style={styles.chartTitle}>Candidatures par mois</Text>
              <BarChart
                data={{
                  labels: Object.keys(stats.byMonth),
                  datasets: [{
                    data: Object.values(stats.byMonth)
                  }]
                }}
                width={screenWidth}
                height={220}
                yAxisLabel=""
                yAxisSuffix=""
                chartConfig={{
                  backgroundColor: '#ffffff',
                  backgroundGradientFrom: '#ffffff',
                  backgroundGradientTo: '#ffffff',
                  decimalPlaces: 0,
                  color: (opacity = 1) => `rgba(37, 99, 235, ${opacity})`,
                  labelColor: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
                  style: {
                    borderRadius: 16,
                  },
                  barPercentage: 0.7,
                }}
                style={{
                  marginVertical: 8,
                  borderRadius: 16,
                }}
              />
            </View>
          )}

          <View style={styles.sectionContainer}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Candidatures récentes</Text>
              <TouchableOpacity onPress={navigateToApplications}>
                <Text style={styles.seeAllText}>Voir tout</Text>
              </TouchableOpacity>
            </View>
            {applications.length > 0 ? (
              applications.slice(0, 3).map((app) => (
                <TouchableOpacity 
                  key={app.id} 
                  style={styles.applicationCard}
                  onPress={() => router.push(`/applications/${app.id}`)}
                >
                  <View style={styles.applicationHeader}>
                    <Text style={styles.companyName}>{app.company}</Text>
                    <View style={[styles.priorityBadge, {
                      backgroundColor: app.priority === 'HIGH' ? '#FEE2E2' : 
                                      app.priority === 'MEDIUM' ? '#FEF3C7' : '#DBEAFE'
                    }]}>
                      <Text style={[styles.priorityText, {
                        color: app.priority === 'HIGH' ? '#DC2626' : 
                               app.priority === 'MEDIUM' ? '#D97706' : '#2563EB'
                      }]}>
                        {app.priority === 'HIGH' ? 'Élevée' : 
                         app.priority === 'MEDIUM' ? 'Moyenne' : 'Basse'}
                      </Text>
                    </View>
                  </View>
                  <Text style={styles.positionText}>{app.position}</Text>
                  <Text style={styles.locationText}>{app.location}</Text>
                  <Text style={styles.dateText}>Postulé le {formatDate(new Date(app.applicationDate), 'dd/MM/yyyy')}</Text>
                </TouchableOpacity>
              ))
            ) : (
              <View style={styles.emptyState}>
                <Ionicons name="document-text-outline" size={50} color="#d1d5db" />
                <Text style={styles.emptyStateText}>Vous n'avez pas encore de candidatures</Text>
                <TouchableOpacity 
                  style={styles.emptyStateButton}
                  onPress={navigateToNewApplication}
                >
                  <Text style={styles.emptyStateButtonText}>Ajouter une candidature</Text>
                </TouchableOpacity>
              </View>
            )}
          </View>

          <View style={styles.sectionContainer}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Entretiens à venir</Text>
              <TouchableOpacity onPress={navigateToCalendar}>
                <Text style={styles.seeAllText}>Voir calendrier</Text>
              </TouchableOpacity>
            </View>
            {stats.upcoming > 0 ? (
              <Text>Affichage des entretiens à venir ici</Text>
            ) : (
              <View style={styles.emptyState}>
                <Ionicons name="calendar-outline" size={50} color="#d1d5db" />
                <Text style={styles.emptyStateText}>Pas d'entretiens à venir</Text>
              </View>
            )}
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
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    color: '#6B7280',
  },
  header: {
    padding: 20,
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  greeting: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1F2937',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 16,
    color: '#6B7280',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 16,
  },
  statCard: {
    flex: 1,
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginHorizontal: 4,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
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
  actionsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 16,
    backgroundColor: 'white',
    marginHorizontal: 16,
    marginVertical: 8,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  actionButton: {
    alignItems: 'center',
  },
  actionButtonText: {
    marginTop: 8,
    fontSize: 12,
    color: '#6B7280',
  },
  chartContainer: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    margin: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  chartTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1F2937',
    marginBottom: 16,
  },
  sectionContainer: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    margin: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1F2937',
  },
  seeAllText: {
    fontSize: 14,
    color: '#2563EB',
  },
  applicationCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 15,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  applicationHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  companyName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1F2937',
  },
  priorityBadge: {
    borderRadius: 12,
    paddingVertical: 4,
    paddingHorizontal: 8,
  },
  priorityText: {
    fontSize: 12,
    fontWeight: '500',
  },
  positionText: {
    fontSize: 14,
    color: '#4B5563',
    marginBottom: 4,
  },
  locationText: {
    fontSize: 12,
    color: '#6B7280',
    marginBottom: 8,
  },
  dateText: {
    fontSize: 12,
    color: '#9CA3AF',
  },
  emptyState: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 30,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  emptyStateText: {
    fontSize: 16,
    color: '#6B7280',
    marginTop: 10,
    marginBottom: 20,
    textAlign: 'center',
  },
  emptyStateButton: {
    backgroundColor: '#2563EB',
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 20,
  },
  emptyStateButtonText: {
    fontSize: 14,
    color: 'white',
    fontWeight: 'bold',
  },
});
