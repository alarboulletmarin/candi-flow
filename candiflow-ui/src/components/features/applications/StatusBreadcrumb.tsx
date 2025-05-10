import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { StatusUpdate } from '../../../types/application';

interface StatusBreadcrumbProps {
  statusUpdates: StatusUpdate[];
  compact?: boolean;
}

// Définition des statuts et leurs couleurs
const STATUS_COLORS: Record<string, string> = {
  APPLIED: '#6B7280', // Gris
  RESUME_REVIEWED: '#3B82F6', // Bleu
  PHONE_SCREEN: '#8B5CF6', // Violet
  INTERVIEW_SCHEDULED: '#10B981', // Vert
  TECHNICAL_INTERVIEW: '#F59E0B', // Orange
  FINAL_INTERVIEW: '#EF4444', // Rouge
  OFFER_RECEIVED: '#6366F1', // Indigo
  ACCEPTED: '#059669', // Vert foncé
  REJECTED: '#DC2626', // Rouge foncé
  DECLINED: '#9CA3AF', // Gris clair
};

// Ordre des statuts dans le processus de candidature
const STATUS_ORDER = [
  'APPLIED',
  'RESUME_REVIEWED',
  'PHONE_SCREEN',
  'INTERVIEW_SCHEDULED',
  'TECHNICAL_INTERVIEW',
  'FINAL_INTERVIEW',
  'OFFER_RECEIVED',
  'ACCEPTED',
];

// Fonction pour traduire les noms de statuts en français
const translateStatus = (status: string): string => {
  const translations: Record<string, string> = {
    APPLIED: 'Postulé',
    RESUME_REVIEWED: 'CV examiné',
    PHONE_SCREEN: 'Entretien tél.',
    INTERVIEW_SCHEDULED: 'Entretien planifié',
    TECHNICAL_INTERVIEW: 'Entretien technique',
    FINAL_INTERVIEW: 'Entretien final',
    OFFER_RECEIVED: 'Offre reçue',
    ACCEPTED: 'Accepté',
    REJECTED: 'Rejeté',
    DECLINED: 'Décliné',
  };
  
  return translations[status] || status;
};

export default function StatusBreadcrumb({ statusUpdates, compact = false }: StatusBreadcrumbProps) {
  if (!statusUpdates || statusUpdates.length === 0) {
    return null;
  }
  
  // Trier les mises à jour par date
  const sortedUpdates = [...statusUpdates].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );
  
  // Obtenir le dernier statut
  const lastStatus = sortedUpdates[sortedUpdates.length - 1].status.name;
  
  // Déterminer les statuts atteints
  const reachedStatuses = new Set<string>();
  sortedUpdates.forEach(update => {
    reachedStatuses.add(update.status.name);
  });
  
  // Si le mode compact est activé, afficher une version simplifiée
  if (compact) {
    return (
      <View style={styles.compactContainer}>
        {STATUS_ORDER.map((status, index) => {
          const isReached = reachedStatuses.has(status);
          const isLast = status === lastStatus;
          
          // Ne pas afficher les statuts après le rejet ou l'acceptation
          if ((reachedStatuses.has('REJECTED') || reachedStatuses.has('ACCEPTED')) && 
              !reachedStatuses.has(status)) {
            return null;
          }
          
          return (
            <React.Fragment key={status}>
              <View 
                style={[
                  styles.compactDot,
                  { backgroundColor: isReached ? STATUS_COLORS[status] : '#E5E7EB' },
                  isLast && styles.compactDotLast
                ]}
              />
              {index < STATUS_ORDER.length - 1 && (
                <View 
                  style={[
                    styles.compactLine,
                    { 
                      backgroundColor: isReached && reachedStatuses.has(STATUS_ORDER[index + 1]) 
                        ? STATUS_COLORS[status] 
                        : '#E5E7EB' 
                    }
                  ]}
                />
              )}
            </React.Fragment>
          );
        })}
      </View>
    );
  }
  
  // Version complète avec les noms des statuts
  return (
    <View style={styles.container}>
      {STATUS_ORDER.map((status, index) => {
        const isReached = reachedStatuses.has(status);
        const isLast = status === lastStatus;
        
        // Ne pas afficher les statuts après le rejet ou l'acceptation
        if ((reachedStatuses.has('REJECTED') || reachedStatuses.has('ACCEPTED')) && 
            !reachedStatuses.has(status)) {
          return null;
        }
        
        return (
          <React.Fragment key={status}>
            <View style={styles.statusItem}>
              <View 
                style={[
                  styles.dot,
                  { backgroundColor: isReached ? STATUS_COLORS[status] : '#E5E7EB' },
                  isLast && styles.dotLast
                ]}
              />
              <Text 
                style={[
                  styles.statusText,
                  isReached ? { color: STATUS_COLORS[status], fontWeight: '500' } : { color: '#9CA3AF' }
                ]}
              >
                {translateStatus(status)}
              </Text>
            </View>
            
            {index < STATUS_ORDER.length - 1 && (
              <View 
                style={[
                  styles.line,
                  { 
                    backgroundColor: isReached && reachedStatuses.has(STATUS_ORDER[index + 1]) 
                      ? STATUS_COLORS[status] 
                      : '#E5E7EB' 
                  }
                ]}
              />
            )}
          </React.Fragment>
        );
      })}
      
      {/* Afficher le statut de rejet s'il existe */}
      {reachedStatuses.has('REJECTED') && (
        <View style={styles.statusItem}>
          <View 
            style={[
              styles.dot,
              { backgroundColor: STATUS_COLORS['REJECTED'] },
              styles.dotLast
            ]}
          />
          <Text 
            style={[
              styles.statusText,
              { color: STATUS_COLORS['REJECTED'], fontWeight: '500' }
            ]}
          >
            {translateStatus('REJECTED')}
          </Text>
        </View>
      )}
      
      {/* Afficher le statut de déclinaison s'il existe */}
      {reachedStatuses.has('DECLINED') && (
        <View style={styles.statusItem}>
          <View 
            style={[
              styles.dot,
              { backgroundColor: STATUS_COLORS['DECLINED'] },
              styles.dotLast
            ]}
          />
          <Text 
            style={[
              styles.statusText,
              { color: STATUS_COLORS['DECLINED'], fontWeight: '500' }
            ]}
          >
            {translateStatus('DECLINED')}
          </Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    paddingVertical: 8,
  },
  statusItem: {
    flexDirection: 'column',
    alignItems: 'center',
    marginHorizontal: 4,
  },
  dot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginBottom: 4,
  },
  dotLast: {
    width: 16,
    height: 16,
    borderRadius: 8,
  },
  line: {
    width: 20,
    height: 2,
    marginTop: -8,
  },
  statusText: {
    fontSize: 10,
    textAlign: 'center',
    width: 70,
  },
  compactContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 16,
  },
  compactDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  compactDotLast: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  compactLine: {
    width: 12,
    height: 2,
  },
});
