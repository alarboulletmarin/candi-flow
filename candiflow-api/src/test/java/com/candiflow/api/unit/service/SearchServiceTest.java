package com.candiflow.api.unit.service;

import com.candiflow.api.dto.recruiter.JobOpeningResponse;
import com.candiflow.api.dto.recruiter.OpeningApplicantResponse;
import com.candiflow.api.factory.JobOpeningResponseFactory;
import com.candiflow.api.factory.OpeningApplicantResponseFactory;
import com.candiflow.api.model.entity.JobOpening;
import com.candiflow.api.model.entity.OpeningApplicant;
import com.candiflow.api.model.entity.User;
import com.candiflow.api.model.enums.JobStatus;
import com.candiflow.api.model.enums.UserRole;
import com.candiflow.api.repository.JobOpeningRepository;
import com.candiflow.api.repository.OpeningApplicantRepository;
import com.candiflow.api.service.SearchService;
import com.candiflow.api.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour le SearchService
 */
class SearchServiceTest extends BaseUnitTest {

    @Mock
    private JobOpeningRepository jobOpeningRepository;
    
    @Mock
    private OpeningApplicantRepository openingApplicantRepository;
    
    @Mock
    private JobOpeningResponseFactory jobOpeningResponseFactory;
    
    @Mock
    private OpeningApplicantResponseFactory openingApplicantResponseFactory;
    
    @InjectMocks
    private SearchService searchService;
    
    private JobOpening jobOpening1;
    private JobOpening jobOpening2;
    private OpeningApplicant applicant1;
    private OpeningApplicant applicant2;
    private User recruiter;
    private UUID recruiterId;
    
    @BeforeEach
    void setUp() {
        // Initialiser le recruteur
        recruiterId = UUID.randomUUID();
        recruiter = new User();
        recruiter.setId(recruiterId);
        recruiter.setEmail("recruiter@example.com");
        recruiter.setRole(UserRole.RECRUITER);
        
        // Initialiser les offres d'emploi
        jobOpening1 = new JobOpening();
        jobOpening1.setId(UUID.randomUUID());
        jobOpening1.setTitle("Développeur Java Senior");
        jobOpening1.setStatus(JobStatus.OPEN);
        jobOpening1.setRecruiter(recruiter);
        jobOpening1.setCreatedAt(java.time.Instant.now().minus(java.time.Duration.ofDays(5)));
        
        jobOpening2 = new JobOpening();
        jobOpening2.setId(UUID.randomUUID());
        jobOpening2.setTitle("Chef de Projet IT");
        jobOpening2.setStatus(JobStatus.OPEN);
        jobOpening2.setRecruiter(recruiter);
        jobOpening2.setCreatedAt(java.time.Instant.now().minus(java.time.Duration.ofDays(2)));
        
        // Initialiser les candidats
        applicant1 = new OpeningApplicant();
        applicant1.setId(UUID.randomUUID());
        applicant1.setName("Jean Dupont");
        applicant1.setEmail("jean.dupont@example.com");
        applicant1.setJobOpening(jobOpening1);
        applicant1.setApplicationDate(LocalDate.now().minusDays(3));
        
        applicant2 = new OpeningApplicant();
        applicant2.setId(UUID.randomUUID());
        applicant2.setName("Marie Martin");
        applicant2.setEmail("marie.martin@example.com");
        applicant2.setJobOpening(jobOpening1);
        applicant2.setApplicationDate(LocalDate.now().minusDays(1));
    }
    
    @Test
    @DisplayName("Rechercher des offres d'emploi par titre")
    @SuppressWarnings("unchecked")
    void searchJobOpenings_ByTitle_ShouldReturnMatchingJobOpenings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<JobOpening> jobOpenings = Arrays.asList(jobOpening1);
        Page<JobOpening> jobOpeningsPage = new PageImpl<>(jobOpenings, pageable, jobOpenings.size());
        
        JobOpeningResponse mockResponse = new JobOpeningResponse();
        mockResponse.setTitle("Développeur Java");
        mockResponse.setTotalApplicants(1);
        
        // Utiliser les paramètres de la méthode réelle au lieu de Specification
        String title = "Java";
        
        when(jobOpeningRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(jobOpeningsPage);
        when(jobOpeningResponseFactory.createFromEntity(any(JobOpening.class)))
                .thenReturn(mockResponse);

        // Act
        Page<JobOpeningResponse> result = searchService.searchJobOpenings(title, null, null, null, null, pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Java");
        assertThat(result.getContent().get(0).getTotalApplicants()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Rechercher des offres d'emploi par statut")
    @SuppressWarnings("unchecked")
    void searchJobOpenings_ByStatus_ShouldReturnMatchingJobOpenings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<JobOpening> jobOpenings = Arrays.asList(jobOpening1, jobOpening2);
        Page<JobOpening> jobOpeningsPage = new PageImpl<>(jobOpenings, pageable, jobOpenings.size());
        
        JobOpeningResponse mockResponse1 = new JobOpeningResponse();
        mockResponse1.setTitle("Développeur Java Senior");
        mockResponse1.setStatus(JobStatus.OPEN);
        
        JobOpeningResponse mockResponse2 = new JobOpeningResponse();
        mockResponse2.setTitle("Chef de Projet IT");
        mockResponse2.setStatus(JobStatus.OPEN);
        
        when(jobOpeningRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(jobOpeningsPage);
        when(jobOpeningResponseFactory.createFromEntity(jobOpening1))
                .thenReturn(mockResponse1);
        when(jobOpeningResponseFactory.createFromEntity(jobOpening2))
                .thenReturn(mockResponse2);
        
        // Act
        Page<JobOpeningResponse> result = searchService.searchJobOpenings(null, JobStatus.OPEN.toString(), null, null, recruiterId, pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(JobStatus.OPEN);
    }
    
    @Test
    @DisplayName("Rechercher des candidats par nom")
    @SuppressWarnings("unchecked")
    void searchApplicants_ByName_ShouldReturnMatchingApplicants() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<OpeningApplicant> applicants = Arrays.asList(applicant1);
        Page<OpeningApplicant> applicantsPage = new PageImpl<>(applicants, pageable, applicants.size());
        
        // Mock pour la stratégie de recherche
        OpeningApplicantResponse mockResponse = new OpeningApplicantResponse();
        mockResponse.setName("Jean Dupont");
        mockResponse.setEmail("jean.dupont@example.com");
        
        // Utiliser un cast explicite pour éviter l'erreur de type safety
        Specification<OpeningApplicant> applicantSpec = any(Specification.class);
        when(openingApplicantRepository.findAll(applicantSpec, any(Pageable.class)))
                .thenReturn(applicantsPage);
        when(openingApplicantResponseFactory.createFromEntity(any(OpeningApplicant.class)))
                .thenReturn(mockResponse);
        
        // Act
        Page<OpeningApplicantResponse> result = searchService.searchApplicants(
                recruiterId, "Jean", null, null, null, null, null, null, pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).contains("Jean");
    }
    
    @Test
    @DisplayName("Devrait rechercher des candidats par date de candidature")
    @SuppressWarnings("unchecked")
    void searchApplicants_ByApplicationDate_ShouldReturnMatchingApplicants() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<OpeningApplicant> applicants = Arrays.asList(applicant1, applicant2);
        Page<OpeningApplicant> applicantsPage = new PageImpl<>(applicants, pageable, applicants.size());
        
        // Mock pour la stratégie de recherche
        OpeningApplicantResponse mockResponse1 = new OpeningApplicantResponse();
        mockResponse1.setName("Jean Dupont");
        mockResponse1.setApplicationDate(LocalDate.now().minusDays(3));
        
        OpeningApplicantResponse mockResponse2 = new OpeningApplicantResponse();
        mockResponse2.setName("Marie Martin");
        mockResponse2.setApplicationDate(LocalDate.now().minusDays(1));
        
        // Utiliser un cast explicite pour éviter l'erreur de type safety
        Specification<OpeningApplicant> applicantSpec = any(Specification.class);
        when(openingApplicantRepository.findAll(applicantSpec, any(Pageable.class)))
                .thenReturn(applicantsPage);
        when(openingApplicantResponseFactory.createFromEntity(applicant1))
                .thenReturn(mockResponse1);
        when(openingApplicantResponseFactory.createFromEntity(applicant2))
                .thenReturn(mockResponse2);
        
        // Act
        LocalDate dateAfter = LocalDate.now().minusDays(4);
        Page<OpeningApplicantResponse> result = searchService.searchApplicants(
                recruiterId, null, null, null, null, null, dateAfter, null, pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getApplicationDate()).isAfterOrEqualTo(dateAfter);
        assertThat(result.getContent().get(1).getApplicationDate()).isAfterOrEqualTo(dateAfter);
    }
}
