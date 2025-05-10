import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Link } from 'expo-router';
import { Application } from '../../../types/application';
import applicationService from '../../../services/applicationService';

type ApplicationListProps = {
  onSelectApplication?: (application: Application) => void;
  showAddButton?: boolean;
};

export default function ApplicationList({ onSelectApplication, showAddButton = true }: ApplicationListProps) {
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadApplications = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await applicationService.getApplications();
      
      // Vérifier que data est bien un tableau
      if (!Array.isArray(data)) {
        console.error('Les données reçues ne sont pas un tableau:', data);
        setApplications([]);
        setError('Format de données incorrect. Veuillez réessayer ou contacter le support.');
      } else {
        setApplications(data);
      }
    } catch (err) {
      console.error('Error loading applications:', err);
      setError('Impossible de charger les candidatures. Veuillez réessayer.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApplications();
  }, []);

  const handleRefresh = () => {
    loadApplications();
  };

  const renderItem = ({ item }: { item: Application }) => (
    <TouchableOpacity
      style={styles.applicationCard}
      onPress={() => onSelectApplication && onSelectApplication(item)}
    >
      <View style={styles.applicationHeader}>
        <Text style={styles.companyName}>{item.company}</Text>
        <View style={[styles.statusBadge, { backgroundColor: item.currentStatus?.color || '#e5e7eb' }]}>
          <Text style={styles.statusText}>{item.currentStatus?.name || 'Aucun statut'}</Text>
        </View>
      </View>
      
      <Text style={styles.positionTitle}>{item.position}</Text>
      
      <View style={styles.applicationDetail}>
        <Ionicons name="location-outline" size={16} color="#6B7280" />
        <Text style={styles.detailText}>{item.location || 'Non spécifié'}</Text>
      </View>
      
      <View style={styles.applicationDetail}>
        <Ionicons name="calendar-outline" size={16} color="#6B7280" />
        <Text style={styles.detailText}>
          {new Date(item.applicationDate).toLocaleDateString('fr-FR', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
          })}
        </Text>
      </View>
      
      {item.priority && (
        <View style={styles.priorityContainer}>
          <View 
            style={[
              styles.priorityIndicator, 
              item.priority === 'HIGH' 
                ? styles.highPriority 
                : item.priority === 'MEDIUM' 
                  ? styles.mediumPriority 
                  : styles.lowPriority
            ]}
          />
          <Text style={styles.priorityText}>
            {item.priority === 'HIGH' 
              ? 'Priorité haute' 
              : item.priority === 'MEDIUM' 
                ? 'Priorité moyenne' 
                : 'Priorité basse'}
          </Text>
        </View>
      )}
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#2563EB" />
        <Text style={styles.loadingText}>Chargement des candidatures...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Ionicons name="alert-circle-outline" size={50} color="#EF4444" />
        <Text style={styles.errorText}>{error}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={handleRefresh}>
          <Text style={styles.retryButtonText}>Réessayer</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (applications.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <Ionicons name="document-text-outline" size={50} color="#d1d5db" />
        <Text style={styles.emptyText}>Vous n'avez pas encore de candidatures</Text>
        {showAddButton && (
          <Link href="/applications/new" asChild>
            <TouchableOpacity style={styles.addButton}>
              <Text style={styles.addButtonText}>Ajouter une candidature</Text>
            </TouchableOpacity>
          </Link>
        )}
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={applications}
        renderItem={renderItem}
        keyExtractor={(item) => item.id.toString()}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.listContent}
        onRefresh={handleRefresh}
        refreshing={loading}
      />
      
      {showAddButton && (
        <Link href="/applications/new" asChild>
          <TouchableOpacity style={styles.floatingButton}>
            <Ionicons name="add" size={24} color="white" />
          </TouchableOpacity>
        </Link>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  listContent: {
    padding: 16,
    paddingBottom: 80,
  },
  applicationCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
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
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
  },
  statusText: {
    fontSize: 12,
    color: 'white',
    fontWeight: '500',
  },
  positionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2563EB',
    marginBottom: 12,
  },
  applicationDetail: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  detailText: {
    marginLeft: 8,
    color: '#6B7280',
    fontSize: 14,
  },
  priorityContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
  },
  priorityIndicator: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginRight: 8,
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
  priorityText: {
    fontSize: 12,
    color: '#6B7280',
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
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  emptyText: {
    color: '#6B7280',
    fontSize: 16,
    marginVertical: 16,
  },
  addButton: {
    backgroundColor: '#2563EB',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 8,
  },
  addButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  floatingButton: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    backgroundColor: '#2563EB',
    width: 56,
    height: 56,
    borderRadius: 28,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
});
