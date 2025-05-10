package com.candiflow.api.service;

import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour l'export de données
 */
@Service
@RequiredArgsConstructor
public class ExportService {

    private final JobOpeningRepository jobOpeningRepository;
    private final OpeningApplicantRepository openingApplicantRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Exporte les candidats d'une offre d'emploi au format CSV
     * @param jobOpeningId ID de l'offre d'emploi
     * @param recruiter Recruteur qui demande l'export
     * @return Contenu CSV
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportApplicantsToCSV(UUID jobOpeningId, User recruiter) {
        JobOpening jobOpening = jobOpeningRepository.findById(jobOpeningId)
                .orElseThrow(() -> new IllegalArgumentException("Offre d'emploi non trouvée"));
        
        // Vérifier que l'offre appartient au recruteur
        if (!jobOpening.getRecruiter().getId().equals(recruiter.getId())) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à exporter les données de cette offre");
        }
        
        List<OpeningApplicant> applicants = openingApplicantRepository.findByJobOpening(jobOpening);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {
            
            // En-tête CSV
            writer.println("ID,Prénom,Nom,Email,Téléphone,Étape,Source,Date de candidature,Dernière mise à jour");
            
            // Lignes de données
            for (OpeningApplicant applicant : applicants) {
                writer.println(String.join(",",
                        quote(applicant.getId().toString()),
                        quote(applicant.getName()), // Utiliser name au lieu de firstName et lastName
                        quote(""), // Pas de lastName séparé
                        quote(applicant.getEmail()),
                        quote(applicant.getPhone()),
                        quote(applicant.getCurrentStage().getName()),
                        quote(applicant.getSource().getName()),
                        quote(applicant.getApplicationDate() != null ? applicant.getApplicationDate().toString() : ""),
                        quote(applicant.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(DATE_FORMATTER))
                ));
            }
            
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du CSV", e);
        }
    }
    
    /**
     * Exporte toutes les offres d'emploi d'un recruteur au format CSV
     * @param recruiter Recruteur qui demande l'export
     * @return Contenu CSV
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportJobOpeningsToCSV(User recruiter) {
        List<JobOpening> jobOpenings = jobOpeningRepository.findByRecruiterOrderByCreatedAtDesc(recruiter);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {
            
            // En-tête CSV
            writer.println("ID,Titre,Description,Statut,Date de création,Nombre de candidats");
            
            // Lignes de données
            for (JobOpening jobOpening : jobOpenings) {
                long applicantCount = openingApplicantRepository.countByJobOpening(jobOpening);
                
                writer.println(String.join(",",
                        quote(jobOpening.getId().toString()),
                        quote(jobOpening.getTitle()),
                        quote(jobOpening.getDescription()),
                        quote(jobOpening.getStatus().toString()),
                        quote(jobOpening.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(DATE_FORMATTER)),
                        quote(String.valueOf(applicantCount))
                ));
            }
            
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du CSV", e);
        }
    }
    
    /**
     * Exporte les statistiques globales au format CSV (admin uniquement)
     * @return Contenu CSV
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportGlobalStatsToCSV() {
        List<JobOpening> allJobOpenings = jobOpeningRepository.findAll();
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {
            
            // En-tête CSV
            writer.println("ID Offre,Titre,Recruteur,Statut,Date de création,Nombre de candidats");
            
            // Lignes de données
            for (JobOpening jobOpening : allJobOpenings) {
                long applicantCount = openingApplicantRepository.countByJobOpening(jobOpening);
                
                writer.println(String.join(",",
                        quote(jobOpening.getId().toString()),
                        quote(jobOpening.getTitle()),
                        quote(jobOpening.getRecruiter().getEmail()),
                        quote(jobOpening.getStatus().toString()),
                        quote(jobOpening.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(DATE_FORMATTER)),
                        quote(String.valueOf(applicantCount))
                ));
            }
            
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du CSV", e);
        }
    }
    
    /**
     * Échappe une chaîne pour le format CSV
     * @param value Valeur à échapper
     * @return Valeur échappée
     */
    private String quote(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
