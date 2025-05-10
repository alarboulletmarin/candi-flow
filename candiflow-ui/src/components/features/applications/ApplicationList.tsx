import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  FlatList, 
  TouchableOpacity, 
  ActivityIndicator,
  TextInput,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter, Link } from 'expo-router';
import { Application, ApplicationStatus } from '../../../types/application';
import applicationService from '../../../services/applicationService';

type ApplicationListProps = {
  onSelectApplication?: (application: Application) => void;
  showAddButton?: boolean;
};

type SortOption = 'date' | 'company' | 'status';
type FilterOption = 'all' | 'active' | 'rejected' | 'accepted';

type ViewMode = 'card' | 'table';

export default function ApplicationList({ onSelectApplication, showAddButton = true }: ApplicationListProps) {
  const [viewMode, setViewMode] = useState<ViewMode>('card');
  const router = useRouter();
  const [applications, setApplications] = useState<Application[]>([]);
  const [filteredApplications, setFilteredApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<SortOption>('date');
  const [filterBy, setFilterBy] = useState<FilterOption>('all');
  const [showSortOptions, setShowSortOptions] = useState(false);
  const [showFilterOptions, setShowFilterOptions] = useState(false);
  const [statuses, setStatuses] = useState<ApplicationStatus[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const loadApplications = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await applicationService.getApplications();
      
      // Vérifier que data est bien un tableau
      if (!Array.isArray(data)) {
        console.error('Les données reçues ne sont pas un tableau:', data);
        setApplications([]);
        setFilteredApplications([]);
        setError('Format de données incorrect. Veuillez réessayer ou contacter le support.');
      } else {
        setApplications(data);
        setFilteredApplications(data);
      }

      // Charger les statuts disponibles
      const statusesData = await applicationService.getApplicationStatuses();
      setStatuses(statusesData);
    } catch (err) {
      console.error('Error loading applications:', err);
      setError('Impossible de charger les candidatures. Veuillez réessayer.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadApplications();
  }, []);

  useEffect(() => {
    filterAndSortApplications();
  }, [applications, searchQuery, sortBy, filterBy]);

  const handleRefresh = () => {
    setRefreshing(true);
    loadApplications();
  };

  const renderHeader = () => (
    <View style={styles.headerContainer}>
      <Text style={styles.headerTitle}>Mes candidatures</Text>
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <TouchableOpacity
          style={[styles.toggleButton, viewMode === 'card' && styles.toggleButtonActive]}
          onPress={() => setViewMode('card')}
        >
          <Ionicons name="grid" size={20} color={viewMode === 'card' ? '#2563EB' : '#6B7280'} />
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.toggleButton, viewMode === 'table' && styles.toggleButtonActive]}
          onPress={() => setViewMode('table')}
        >
          <Ionicons name="list" size={20} color={viewMode === 'table' ? '#2563EB' : '#6B7280'} />
        </TouchableOpacity>
        {showAddButton && (
          <TouchableOpacity
            style={styles.addButton}
            onPress={() => router.push('/applications/new')}
          >
            <Ionicons name="add-circle" size={28} color="#FFFFFF" />
            <Text style={styles.addButtonText}>Ajouter</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );



  const filterAndSortApplications = () => {
    let filtered = [...applications];

    // Filtrer par recherche
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(app => 
        app.company?.toLowerCase().includes(query) || 
        app.position?.toLowerCase().includes(query) ||
        app.location?.toLowerCase().includes(query)
      );
    }

    // Filtrer par statut
    if (filterBy !== 'all') {
      filtered = filtered.filter(app => {
        const status = app.currentStatus?.name?.toLowerCase() || '';
        switch(filterBy) {
          case 'active':
            return !['rejected', 'accepted', 'declined'].includes(status);
          case 'rejected':
            return status === 'rejected';
          case 'accepted':
            return ['accepted', 'offer'].includes(status);
          default:
            return true;
        }
      });
    }

    // Trier les résultats
    filtered.sort((a, b) => {
      switch(sortBy) {
        case 'company':
          return (a.company || '').localeCompare(b.company || '');
        case 'status':
          return (a.currentStatus?.name || '').localeCompare(b.currentStatus?.name || '');
        case 'date':
        default:
          return new Date(b.applicationDate).getTime() - new Date(a.applicationDate).getTime();
      }
    });

    setFilteredApplications(filtered);
  };



  const renderSearchAndFilters = () => (
    <View style={styles.searchAndFiltersContainer}>
      {renderHeader()}
      {/* Barre de recherche */}
      <View style={styles.searchBarContainer}>
        <Ionicons name="search" size={20} color="#6B7280" style={styles.searchIcon} />
        <TextInput
          style={styles.searchInput}
          placeholder="Rechercher une candidature..."
          value={searchQuery}
          onChangeText={setSearchQuery}
          placeholderTextColor="#9CA3AF"
        />
        {searchQuery ? (
          <TouchableOpacity onPress={() => setSearchQuery('')} style={styles.clearButton}>
            <Ionicons name="close-circle" size={20} color="#6B7280" />
          </TouchableOpacity>
        ) : null}
      </View>

      {/* Boutons de filtrage et tri */}
      <View style={styles.filtersRow}>
        {/* Bouton de filtrage */}
        <TouchableOpacity 
          style={[styles.filterButton, filterBy !== 'all' && styles.activeFilterButton]}
          onPress={() => setShowFilterOptions(!showFilterOptions)}
        >
          <Ionicons name="filter" size={18} color={filterBy !== 'all' ? "#2563EB" : "#6B7280"} />
          <Text style={[styles.filterButtonText, filterBy !== 'all' && styles.activeFilterText]}>
            {filterBy === 'all' ? 'Tous' : 
             filterBy === 'active' ? 'En cours' :
             filterBy === 'rejected' ? 'Rejetées' : 'Acceptées'}
          </Text>
          <Ionicons 
            name={showFilterOptions ? "chevron-up" : "chevron-down"} 
            size={16} 
            color={filterBy !== 'all' ? "#2563EB" : "#6B7280"} 
          />
        </TouchableOpacity>

        {/* Bouton de tri */}
        <TouchableOpacity 
          style={styles.filterButton}
          onPress={() => setShowSortOptions(!showSortOptions)}
        >
          <Ionicons name="swap-vertical" size={18} color="#6B7280" />
          <Text style={styles.filterButtonText}>
            {sortBy === 'date' ? 'Date' : 
             sortBy === 'company' ? 'Entreprise' : 'Statut'}
          </Text>
          <Ionicons 
            name={showSortOptions ? "chevron-up" : "chevron-down"} 
            size={16} 
            color="#6B7280" 
          />
        </TouchableOpacity>
      </View>

      {/* Options de filtrage */}
      {showFilterOptions && (
        <View style={styles.optionsContainer}>
          <TouchableOpacity 
            style={[styles.optionItem, filterBy === 'all' && styles.selectedOption]}
            onPress={() => {
              setFilterBy('all');
              setShowFilterOptions(false);
            }}
          >
            <Text style={[styles.optionText, filterBy === 'all' && styles.selectedOptionText]}>Toutes les candidatures</Text>
            {filterBy === 'all' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.optionItem, filterBy === 'active' && styles.selectedOption]}
            onPress={() => {
              setFilterBy('active');
              setShowFilterOptions(false);
            }}
          >
            <Text style={[styles.optionText, filterBy === 'active' && styles.selectedOptionText]}>Candidatures en cours</Text>
            {filterBy === 'active' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.optionItem, filterBy === 'rejected' && styles.selectedOption]}
            onPress={() => {
              setFilterBy('rejected');
              setShowFilterOptions(false);
            }}
          >
            <Text style={[styles.optionText, filterBy === 'rejected' && styles.selectedOptionText]}>Candidatures rejetées</Text>
            {filterBy === 'rejected' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.optionItem, filterBy === 'accepted' && styles.selectedOption]}
            onPress={() => {
              setFilterBy('accepted');
              setShowFilterOptions(false);
            }}
          >
            <Text style={[styles.optionText, filterBy === 'accepted' && styles.selectedOptionText]}>Candidatures acceptées</Text>
            {filterBy === 'accepted' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
        </View>
      )}

      {/* Options de tri */}
      {showSortOptions && (
        <View style={styles.optionsContainer}>
          <TouchableOpacity 
            style={[styles.optionItem, sortBy === 'date' && styles.selectedOption]}
            onPress={() => {
              setSortBy('date');
              setShowSortOptions(false);
            }}
          >
            <Text style={[styles.optionText, sortBy === 'date' && styles.selectedOptionText]}>Date de candidature</Text>
            {sortBy === 'date' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.optionItem, sortBy === 'company' && styles.selectedOption]}
            onPress={() => {
              setSortBy('company');
              setShowSortOptions(false);
            }}
          >
            <Text style={[styles.optionText, sortBy === 'company' && styles.selectedOptionText]}>Nom de l'entreprise</Text>
            {sortBy === 'company' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.optionItem, sortBy === 'status' && styles.selectedOption]}
            onPress={() => {
              setSortBy('status');
              setShowSortOptions(false);
            }}
          >
            <Text style={[styles.optionText, sortBy === 'status' && styles.selectedOptionText]}>Statut</Text>
            {sortBy === 'status' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
          </TouchableOpacity>
        </View>
      )}
    </View>
  );

  const renderApplicationCard = ({ item }: { item: Application }) => {
    // Formater la date
    const formattedDate = new Date(item.applicationDate).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });

    // Déterminer la couleur du statut
    const statusColor = item.currentStatus?.color || '#9CA3AF';
    
    return (
      <TouchableOpacity
        style={styles.applicationCard}
        onPress={() => onSelectApplication && onSelectApplication(item)}
        activeOpacity={0.7}
      >
        {/* Badge de statut */}
        <View style={[styles.statusBadge, { backgroundColor: statusColor }]}>
          <Text style={styles.statusText}>{item.currentStatus?.name || 'Aucun statut'}</Text>
        </View>
        
        {/* En-tête avec logo et nom de l'entreprise */}
        <View style={styles.applicationHeader}>
          <View style={styles.companyLogoContainer}>
            <Text style={styles.companyInitial}>{item.company?.charAt(0) || '?'}</Text>
          </View>
          <View style={styles.headerTextContainer}>
            <Text style={styles.companyName} numberOfLines={1}>{item.company}</Text>
            <Text style={styles.positionTitle} numberOfLines={1}>{item.position}</Text>
          </View>
        </View>
        
        {/* Ligne de séparation */}
        <View style={styles.divider} />
        
        {/* Détails de la candidature */}
        <View style={styles.detailsContainer}>
          {/* Localisation */}
          {item.location && (
            <View style={styles.applicationDetail}>
              <Ionicons name="location-outline" size={16} color="#6B7280" />
              <Text style={styles.detailText} numberOfLines={1}>{item.location}</Text>
            </View>
          )}
          
          {/* Date de candidature */}
          <View style={styles.applicationDetail}>
            <Ionicons name="calendar-outline" size={16} color="#6B7280" />
            <Text style={styles.detailText}>{formattedDate}</Text>
          </View>
          
          {/* Priorité */}
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
        </View>
      </TouchableOpacity>
    );
  };

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

  return (
    <View style={styles.container}>
      {/* Barre de recherche et filtres */}
      {/* Header, recherche, filtres, tri */}
      <View style={styles.searchAndFiltersContainer}>
        <View style={styles.headerContainer}>
          <Text style={styles.headerTitle}>Mes candidatures</Text>
          <TouchableOpacity
            style={styles.toggleButton}
            onPress={() => setViewMode(viewMode === 'card' ? 'table' : 'card')}
          >
            <Ionicons name={viewMode === 'card' ? "grid" : "list"} size={24} color="#2563EB" />
          </TouchableOpacity>
          {showAddButton && (
            <TouchableOpacity
              style={styles.addButton}
              onPress={() => router.push('/applications/new')}
            >
              <Ionicons name="add-circle" size={28} color="#2563EB" />
              <Text style={styles.addButtonText}>Ajouter</Text>
            </TouchableOpacity>
          )}
        </View>
        <View style={styles.searchBarContainer}>
          <Ionicons name="search" size={20} color="#6B7280" style={styles.searchIcon} />
          <TextInput
            style={styles.searchInput}
            placeholder="Rechercher une candidature..."
            value={searchQuery}
            onChangeText={setSearchQuery}
            placeholderTextColor="#9CA3AF"
          />
          {searchQuery ? (
            <TouchableOpacity onPress={() => setSearchQuery('')} style={styles.clearButton}>
              <Ionicons name="close-circle" size={20} color="#6B7280" />
            </TouchableOpacity>
          ) : null}
        </View>
        <View style={styles.filtersRow}>
          <TouchableOpacity 
            style={[styles.filterButton, filterBy !== 'all' && styles.activeFilterButton]}
            onPress={() => setShowFilterOptions(!showFilterOptions)}
          >
            <Ionicons name="filter" size={18} color={filterBy !== 'all' ? "#2563EB" : "#6B7280"} />
            <Text style={[styles.filterButtonText, filterBy !== 'all' && styles.activeFilterText]}>
              {filterBy === 'all' ? 'Tous' : 
                filterBy === 'active' ? 'En cours' :
                filterBy === 'rejected' ? 'Rejetées' : 'Acceptées'}
            </Text>
            <Ionicons 
              name={showFilterOptions ? "chevron-up" : "chevron-down"} 
              size={16} 
              color={filterBy !== 'all' ? "#2563EB" : "#6B7280"} 
            />
          </TouchableOpacity>
          <TouchableOpacity 
            style={styles.filterButton}
            onPress={() => setShowSortOptions(!showSortOptions)}
          >
            <Ionicons name="swap-vertical" size={18} color="#6B7280" />
            <Text style={styles.filterButtonText}>
              {sortBy === 'date' ? 'Date' : 
                sortBy === 'company' ? 'Entreprise' : 'Statut'}
            </Text>
            <Ionicons 
              name={showSortOptions ? "chevron-up" : "chevron-down"} 
              size={16} 
              color="#6B7280" 
            />
          </TouchableOpacity>
        </View>
        {showFilterOptions && (
          <View style={styles.optionsContainer}>
            <TouchableOpacity 
              style={[styles.optionItem, filterBy === 'all' && styles.selectedOption]}
              onPress={() => { setFilterBy('all'); setShowFilterOptions(false); }}
            >
              <Text style={[styles.optionText, filterBy === 'all' && styles.selectedOptionText]}>Toutes les candidatures</Text>
              {filterBy === 'all' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
            <TouchableOpacity 
              style={[styles.optionItem, filterBy === 'active' && styles.selectedOption]}
              onPress={() => { setFilterBy('active'); setShowFilterOptions(false); }}
            >
              <Text style={[styles.optionText, filterBy === 'active' && styles.selectedOptionText]}>Candidatures en cours</Text>
              {filterBy === 'active' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
            <TouchableOpacity 
              style={[styles.optionItem, filterBy === 'rejected' && styles.selectedOption]}
              onPress={() => { setFilterBy('rejected'); setShowFilterOptions(false); }}
            >
              <Text style={[styles.optionText, filterBy === 'rejected' && styles.selectedOptionText]}>Candidatures rejetées</Text>
              {filterBy === 'rejected' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
            <TouchableOpacity 
              style={[styles.optionItem, filterBy === 'accepted' && styles.selectedOption]}
              onPress={() => { setFilterBy('accepted'); setShowFilterOptions(false); }}
            >
              <Text style={[styles.optionText, filterBy === 'accepted' && styles.selectedOptionText]}>Candidatures acceptées</Text>
              {filterBy === 'accepted' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
          </View>
        )}
        {showSortOptions && (
          <View style={styles.optionsContainer}>
            <TouchableOpacity 
              style={[styles.optionItem, sortBy === 'date' && styles.selectedOption]}
              onPress={() => { setSortBy('date'); setShowSortOptions(false); }}
            >
              <Text style={[styles.optionText, sortBy === 'date' && styles.selectedOptionText]}>Date de candidature</Text>
              {sortBy === 'date' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
            <TouchableOpacity 
              style={[styles.optionItem, sortBy === 'company' && styles.selectedOption]}
              onPress={() => { setSortBy('company'); setShowSortOptions(false); }}
            >
              <Text style={[styles.optionText, sortBy === 'company' && styles.selectedOptionText]}>Nom de l'entreprise</Text>
              {sortBy === 'company' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
            <TouchableOpacity 
              style={[styles.optionItem, sortBy === 'status' && styles.selectedOption]}
              onPress={() => { setSortBy('status'); setShowSortOptions(false); }}
            >
              <Text style={[styles.optionText, sortBy === 'status' && styles.selectedOptionText]}>Statut</Text>
              {sortBy === 'status' && <Ionicons name="checkmark" size={18} color="#2563EB" />}
            </TouchableOpacity>
          </View>
        )}
        {/* Résultat count en ligne */}
        <Text style={{color:'#2563EB',fontWeight:'600',marginTop:8,marginBottom:4,alignSelf:'flex-end'}}>{filteredApplications.length} résultat{filteredApplications.length > 1 ? 's' : ''}</Text>
      </View>
      {viewMode === 'card' ? (
        <FlatList
          data={filteredApplications}
          renderItem={renderApplicationCard}
          keyExtractor={(item) => item.id.toString()}
          contentContainerStyle={styles.listContent}
          refreshing={refreshing}
          onRefresh={handleRefresh}
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <Ionicons name="document-text-outline" size={50} color="#9CA3AF" />
              <Text style={styles.emptyText}>
                {searchQuery ? 'Aucun résultat pour cette recherche' : 'Aucune candidature trouvée'}
              </Text>
              <Text style={styles.emptySubtext}>
                {searchQuery 
                  ? 'Essayez de modifier vos critères de recherche'
                  : 'Commencez à suivre vos candidatures en cliquant sur le bouton ci-dessus'}
              </Text>
              {searchQuery && (
                <TouchableOpacity 
                  style={styles.clearSearchButton} 
                  onPress={() => setSearchQuery('')}
                >
                  <Text style={styles.clearSearchButtonText}>Effacer la recherche</Text>
                </TouchableOpacity>
              )}
            </View>
          }
        />
      ) : (
        <FlatList
          data={filteredApplications}
          keyExtractor={(item) => item.id.toString()}
          contentContainerStyle={styles.listContent}
          refreshing={refreshing}
          onRefresh={handleRefresh}
          ListHeaderComponent={
            <View style={styles.tableHeader}>
              <Text style={[styles.tableHeaderCell, {flex:2}]}>Entreprise</Text>
              <Text style={[styles.tableHeaderCell, {flex:2}]}>Poste</Text>
              <Text style={[styles.tableHeaderCell, {flex:1}]}>Statut</Text>
              <Text style={[styles.tableHeaderCell, {flex:1}]}>Date</Text>
              <Text style={[styles.tableHeaderCell, {flex:1}]}>Priorité</Text>
              <Text style={[styles.tableHeaderCell, {flex:1}]}>Action</Text>
            </View>
          }
          renderItem={({item}) => (
            <View style={styles.tableRow}>
              <Text style={[styles.tableCell, {flex:2}]} numberOfLines={1}>{item.company}</Text>
              <Text style={[styles.tableCell, {flex:2}]} numberOfLines={1}>{item.position}</Text>
              <View style={[styles.tableCell, {flex:1, flexDirection:'row', alignItems:'center'}]}>
                <View style={{width:10, height:10, borderRadius:5, backgroundColor:item.currentStatus?.color||'#9CA3AF', marginRight:6}} />
                <Text numberOfLines={1}>{item.currentStatus?.name||'—'}</Text>
              </View>
              <Text style={[styles.tableCell, {flex:1}]}>{new Date(item.applicationDate).toLocaleDateString('fr-FR')}</Text>
              <Text style={[styles.tableCell, {flex:1}]}>{item.priority === 'HIGH' ? 'Haute' : item.priority === 'MEDIUM' ? 'Moyenne' : item.priority === 'LOW' ? 'Basse' : ''}</Text>
              <TouchableOpacity style={[styles.tableCell, {flex:1}]} onPress={() => onSelectApplication && onSelectApplication(item)}>
                <Ionicons name="chevron-forward" size={20} color="#2563EB" />
              </TouchableOpacity>
            </View>
          )}
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <Ionicons name="document-text-outline" size={50} color="#9CA3AF" />
              <Text style={styles.emptyText}>
                {searchQuery ? 'Aucun résultat pour cette recherche' : 'Aucune candidature trouvée'}
              </Text>
              <Text style={styles.emptySubtext}>
                {searchQuery 
                  ? 'Essayez de modifier vos critères de recherche'
                  : 'Commencez à suivre vos candidatures en cliquant sur le bouton ci-dessus'}
              </Text>
              {searchQuery && (
                <TouchableOpacity 
                  style={styles.clearSearchButton} 
                  onPress={() => setSearchQuery('')}
                >
                  <Text style={styles.clearSearchButtonText}>Effacer la recherche</Text>
                </TouchableOpacity>
              )}
            </View>
          }
        />
      )}
      
      {showAddButton && (
        <Link href="/applications/new" asChild>
          <TouchableOpacity style={styles.floatingButton}>
            <Ionicons name="add" size={24} color="#FFFFFF" />
          </TouchableOpacity>
        </Link>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  toggleButton: {
    padding: 8,
    borderRadius: 8,
    marginHorizontal: 2,
    backgroundColor: '#F3F4F6',
  },
  toggleButtonActive: {
    backgroundColor: '#2563EB22',
  },
  tableHeader: {
    flexDirection: 'row',
    backgroundColor: '#F3F4F6',
    paddingVertical: 8,
    paddingHorizontal: 8,
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
    marginTop: 8,
  },
  tableHeaderCell: {
    fontWeight: 'bold',
    color: '#1F2937',
    fontSize: 14,
    paddingHorizontal: 4,
  },
  tableRow: {
    flexDirection: 'row',
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
    alignItems: 'center',
    minHeight: 48,
    paddingHorizontal: 8,
  },
  tableCell: {
    fontSize: 14,
    color: '#374151',
    paddingVertical: 8,
    paddingHorizontal: 4,
  },
  container: {
    flex: 1,
    backgroundColor: '#F3F4F6',
  },
  headerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1F2937',
  },
  addButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#2563EB',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  addButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '500',
    marginLeft: 8,
  },
  searchAndFiltersContainer: {
    padding: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  searchBarContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F3F4F6',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  searchIcon: {
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    color: '#1F2937',
    paddingVertical: 4,
  },
  clearButton: {
    padding: 4,
  },
  filtersRow: {
    flexDirection: 'row',
    marginTop: 12,
    justifyContent: 'space-between',
  },
  filterButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F3F4F6',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginRight: 8,
  },
  activeFilterButton: {
    backgroundColor: '#EBF5FF',
  },
  filterButtonText: {
    fontSize: 14,
    color: '#6B7280',
    marginHorizontal: 8,
  },
  activeFilterText: {
    color: '#2563EB',
  },
  optionsContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    marginTop: 8,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  optionItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  selectedOption: {
    backgroundColor: '#F0F9FF',
  },
  optionText: {
    fontSize: 14,
    color: '#4B5563',
  },
  selectedOptionText: {
    color: '#2563EB',
    fontWeight: '500',
  },
  listContent: {
    padding: 16,
    paddingBottom: 80,
  },
  applicationCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    position: 'relative',
  },
  statusBadge: {
    position: 'absolute',
    top: 12,
    right: 12,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  statusText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '500',
  },
  applicationHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  companyLogoContainer: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#2563EB',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  companyInitial: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
  headerTextContainer: {
    flex: 1,
  },
  companyName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1F2937',
    marginBottom: 4,
  },
  positionTitle: {
    fontSize: 14,
    color: '#4B5563',
  },
  divider: {
    height: 1,
    backgroundColor: '#E5E7EB',
    marginVertical: 12,
  },
  detailsContainer: {
    marginTop: 8,
  },
  applicationDetail: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  detailText: {
    fontSize: 14,
    color: '#6B7280',
    marginLeft: 8,
  },
  priorityContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
  },
  priorityIndicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
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
    marginTop: 12,
    fontSize: 16,
    color: '#6B7280',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  errorText: {
    marginTop: 12,
    fontSize: 16,
    color: '#EF4444',
    textAlign: 'center',
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: '#2563EB',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  retryButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '500',
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
    marginTop: 32,
  },
  emptyText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#4B5563',
    marginTop: 16,
    textAlign: 'center',
  },
  emptySubtext: {
    fontSize: 14,
    color: '#6B7280',
    marginTop: 8,
    textAlign: 'center',
    marginBottom: 16,
  },
  clearSearchButton: {
    backgroundColor: '#F3F4F6',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
    marginTop: 8,
  },
  clearSearchButtonText: {
    color: '#4B5563',
    fontSize: 14,
  },
  floatingButton: {
    position: 'absolute',
    bottom: 24,
    right: 24,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: '#2563EB',
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
});