package com.candiflow.api.dto.dashboard;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour les statistiques du tableau de bord d'un recruteur
 * Utilise le pattern Builder pour faciliter la construction d'objets complexes
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecruiterDashboardResponse {
    private long totalJobOpenings;
    private long activeJobOpenings;
    private long totalApplicants;
    private long newApplicantsThisMonth;
    private long newApplicantsThisWeek;
    private double averageHiringTimeInDays;
    
    /**
     * Alias pour getAverageHiringTimeInDays() pour compatibilité avec les tests
     * @return Temps moyen d'embauche en jours
     */
    public double getAvgHiringTimeInDays() {
        return this.averageHiringTimeInDays;
    }
    private List<StageConversionRate> conversionRates;
    
    /**
     * Crée un nouveau builder pour RecruiterDashboardResponse
     * @return Builder pour RecruiterDashboardResponse
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder pour RecruiterDashboardResponse
     * Implémente le pattern Builder pour faciliter la construction d'objets complexes
     */
    public static class Builder {
        private final RecruiterDashboardResponse response;
        
        private Builder() {
            response = new RecruiterDashboardResponse();
            response.conversionRates = new ArrayList<>();
        }
        
        /**
         * Définit le nombre total d'offres d'emploi
         * @param totalJobOpenings Nombre total d'offres d'emploi
         * @return Builder pour chaînage
         */
        public Builder totalJobOpenings(long totalJobOpenings) {
            response.totalJobOpenings = totalJobOpenings;
            return this;
        }
        
        /**
         * Définit le nombre d'offres d'emploi actives
         * @param activeJobOpenings Nombre d'offres d'emploi actives
         * @return Builder pour chaînage
         */
        public Builder activeJobOpenings(long activeJobOpenings) {
            response.activeJobOpenings = activeJobOpenings;
            return this;
        }
        
        /**
         * Définit le nombre total de candidats
         * @param totalApplicants Nombre total de candidats
         * @return Builder pour chaînage
         */
        public Builder totalApplicants(long totalApplicants) {
            response.totalApplicants = totalApplicants;
            return this;
        }
        
        /**
         * Définit le nombre de nouveaux candidats ce mois-ci
         * @param newApplicantsThisMonth Nombre de nouveaux candidats ce mois-ci
         * @return Builder pour chaînage
         */
        public Builder newApplicantsThisMonth(long newApplicantsThisMonth) {
            response.newApplicantsThisMonth = newApplicantsThisMonth;
            return this;
        }
        
        /**
         * Définit le nombre de nouveaux candidats cette semaine
         * @param newApplicantsThisWeek Nombre de nouveaux candidats cette semaine
         * @return Builder pour chaînage
         */
        public Builder newApplicantsThisWeek(long newApplicantsThisWeek) {
            response.newApplicantsThisWeek = newApplicantsThisWeek;
            return this;
        }
        
        /**
         * Définit le temps moyen d'embauche en jours
         * @param averageHiringTimeInDays Temps moyen d'embauche en jours
         * @return Builder pour chaînage
         */
        public Builder averageHiringTimeInDays(double averageHiringTimeInDays) {
            response.averageHiringTimeInDays = averageHiringTimeInDays;
            return this;
        }
        
        /**
         * Définit les taux de conversion entre les étapes
         * @param conversionRates Liste des taux de conversion
         * @return Builder pour chaînage
         */
        public Builder conversionRates(List<StageConversionRate> conversionRates) {
            response.conversionRates = conversionRates;
            return this;
        }
        
        /**
         * Ajoute un taux de conversion
         * @param conversionRate Taux de conversion à ajouter
         * @return Builder pour chaînage
         */
        public Builder addConversionRate(StageConversionRate conversionRate) {
            response.conversionRates.add(conversionRate);
            return this;
        }
        
        /**
         * Construit l'objet RecruiterDashboardResponse
         * @return Objet RecruiterDashboardResponse construit
         */
        public RecruiterDashboardResponse build() {
            return response;
        }
    }
}
