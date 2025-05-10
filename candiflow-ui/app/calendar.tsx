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
import { Calendar as RNCalendar, LocaleConfig } from 'react-native-calendars';
import applicationService from '../src/services/applicationService';
import { Application } from '../src/types/application';

// Configuration de la localisation pour le calendrier
LocaleConfig.locales['fr'] = {
  monthNames: [
    'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
    'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
  ],
  monthNamesShort: [
    'Janv.', 'Févr.', 'Mars', 'Avril', 'Mai', 'Juin',
    'Juil.', 'Août', 'Sept.', 'Oct.', 'Nov.', 'Déc.'
  ],
  dayNames: [
    'Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'
  ],
  dayNamesShort: ['Dim.', 'Lun.', 'Mar.', 'Mer.', 'Jeu.', 'Ven.', 'Sam.']
};
LocaleConfig.defaultLocale = 'fr';

// Fonction de formatage de date
const formatDate = (date: Date): string => {
  return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`;
};

// Fonction de formatage d'heure
const formatTime = (date: Date): string => {
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

export default function CalendarScreen() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [interviews, setInterviews] = useState<any[]>([]);
  const [markedDates, setMarkedDates] = useState<any>({});
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [selectedDateInterviews, setSelectedDateInterviews] = useState<any[]>([]);

  useEffect(() => {
    const fetchInterviews = async () => {
      try {
        setLoading(true);
        const applications = await applicationService.getUserApplications();
        
        // Extraire tous les entretiens
        const allInterviews: any[] = [];
        const marked: any = {};
        
        applications.forEach(app => {
          if (!app.statusUpdates) return;
          
          app.statusUpdates.forEach(update => {
            if (update.status.name === 'INTERVIEW_SCHEDULED' && update.interviewDate) {
              const interviewDate = new Date(update.interviewDate);
              const dateString = interviewDate.toISOString().split('T')[0];
              
              allInterviews.push({
                id: app.id,
                company: app.company,
                position: app.position,
                date: interviewDate,
                notes: update.notes,
                dateString
              });
              
              // Marquer la date dans le calendrier
              marked[dateString] = {
                marked: true,
                dotColor: '#2563EB',
                selected: dateString === selectedDate,
                selectedColor: '#E5EDFF'
              };
            }
          });
        });
        
        // Trier les entretiens par date
        allInterviews.sort((a, b) => a.date.getTime() - b.date.getTime());
        
        setInterviews(allInterviews);
        setMarkedDates(marked);
        
        // Filtrer les entretiens pour la date sélectionnée
        filterInterviewsByDate(selectedDate, allInterviews);
      } catch (error) {
        console.error('Erreur lors du chargement des entretiens:', error);
        Alert.alert('Erreur', 'Impossible de charger les entretiens');
      } finally {
        setLoading(false);
      }
    };
    
    fetchInterviews();
  }, []);
  
  const filterInterviewsByDate = (date: string, allInterviews = interviews) => {
    const filtered = allInterviews.filter(interview => interview.dateString === date);
    setSelectedDateInterviews(filtered);
  };
  
  const handleDateSelect = (day: any) => {
    const selected = day.dateString;
    
    // Mettre à jour la date sélectionnée
    setSelectedDate(selected);
    
    // Mettre à jour les marqueurs de dates
    const newMarkedDates = { ...markedDates };
    
    // Réinitialiser toutes les dates sélectionnées
    Object.keys(newMarkedDates).forEach(date => {
      if (newMarkedDates[date].selected) {
        newMarkedDates[date] = {
          ...newMarkedDates[date],
          selected: false
        };
      }
    });
    
    // Marquer la nouvelle date sélectionnée
    newMarkedDates[selected] = {
      ...(newMarkedDates[selected] || {}),
      selected: true,
      selectedColor: '#E5EDFF'
    };
    
    setMarkedDates(newMarkedDates);
    
    // Filtrer les entretiens pour la date sélectionnée
    filterInterviewsByDate(selected);
  };
  
  const navigateToApplication = (id: number) => {
    router.push(`/applications/${id}`);
  };

  return (
    <>
      <Stack.Screen 
        options={{ 
          title: 'Calendrier des entretiens',
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
          <Text style={styles.loadingText}>Chargement du calendrier...</Text>
        </View>
      ) : (
        <View style={styles.container}>
          <RNCalendar
            style={styles.calendar}
            theme={{
              calendarBackground: '#FFFFFF',
              textSectionTitleColor: '#1F2937',
              selectedDayBackgroundColor: '#2563EB',
              selectedDayTextColor: '#FFFFFF',
              todayTextColor: '#2563EB',
              dayTextColor: '#1F2937',
              textDisabledColor: '#9CA3AF',
              dotColor: '#2563EB',
              selectedDotColor: '#FFFFFF',
              arrowColor: '#2563EB',
              monthTextColor: '#1F2937',
              indicatorColor: '#2563EB',
              textDayFontWeight: '400',
              textMonthFontWeight: 'bold',
              textDayHeaderFontWeight: '600',
              textDayFontSize: 14,
              textMonthFontSize: 16,
              textDayHeaderFontSize: 12
            }}
            markedDates={markedDates}
            onDayPress={handleDateSelect}
            enableSwipeMonths={true}
            hideExtraDays={false}
          />
          
          <View style={styles.interviewsContainer}>
            <Text style={styles.dateTitle}>
              {selectedDate ? new Date(selectedDate).toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }) : 'Aucune date sélectionnée'}
            </Text>
            
            {selectedDateInterviews.length > 0 ? (
              <ScrollView style={styles.interviewsList}>
                {selectedDateInterviews.map((interview, index) => (
                  <TouchableOpacity 
                    key={`interview-${index}`}
                    style={styles.interviewCard}
                    onPress={() => navigateToApplication(interview.id)}
                  >
                    <View style={styles.interviewTimeContainer}>
                      <Text style={styles.interviewTime}>{formatTime(interview.date)}</Text>
                    </View>
                    <View style={styles.interviewDetails}>
                      <Text style={styles.interviewCompany}>{interview.company}</Text>
                      <Text style={styles.interviewPosition}>{interview.position}</Text>
                      {interview.notes && (
                        <Text style={styles.interviewNotes} numberOfLines={2}>
                          {interview.notes}
                        </Text>
                      )}
                    </View>
                    <Ionicons name="chevron-forward" size={20} color="#6B7280" />
                  </TouchableOpacity>
                ))}
              </ScrollView>
            ) : (
              <View style={styles.emptyStateContainer}>
                <Ionicons name="calendar-outline" size={40} color="#D1D5DB" />
                <Text style={styles.emptyStateText}>
                  Aucun entretien prévu pour cette date
                </Text>
                <TouchableOpacity 
                  style={styles.emptyStateButton}
                  onPress={() => router.push('/applications/new')}
                >
                  <Text style={styles.emptyStateButtonText}>
                    Ajouter une candidature
                  </Text>
                </TouchableOpacity>
              </View>
            )}
          </View>
        </View>
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
  calendar: {
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  interviewsContainer: {
    flex: 1,
    padding: 16,
  },
  dateTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1F2937',
    marginBottom: 16,
    textTransform: 'capitalize',
  },
  interviewsList: {
    flex: 1,
  },
  interviewCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  interviewTimeContainer: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#E5EDFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  interviewTime: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#2563EB',
  },
  interviewDetails: {
    flex: 1,
  },
  interviewCompany: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1F2937',
    marginBottom: 4,
  },
  interviewPosition: {
    fontSize: 14,
    color: '#4B5563',
    marginBottom: 4,
  },
  interviewNotes: {
    fontSize: 12,
    color: '#6B7280',
  },
  emptyStateContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  emptyStateText: {
    marginTop: 16,
    marginBottom: 24,
    fontSize: 16,
    color: '#6B7280',
    textAlign: 'center',
  },
  emptyStateButton: {
    backgroundColor: '#2563EB',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  emptyStateButtonText: {
    color: 'white',
    fontWeight: '600',
    fontSize: 14,
  },
});
