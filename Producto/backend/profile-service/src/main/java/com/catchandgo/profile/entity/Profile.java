package com.catchandgo.profile.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userId; // ID del usuario en Auth (Keycloak/Supabase)
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    
    // Datos Profesionales/Empresa
    @Column(columnDefinition = "TEXT")
    private String photoUrl;
    
    @Column(columnDefinition = "TEXT")
    private String cvUrl;
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(columnDefinition = "TEXT")
    private String certificateUrl;
    
    @Column(columnDefinition = "TEXT")
    private String representativeName;
    
    // Datos Bancarios
    private String rut;
    private String bankName;
    private String accountType;
    private String accountNumber;
    
    // Tipo de Perfil: TRABAJADOR o EMPRESA
    private String type;

    // Geolocalización
    private Double latitude;
    private Double longitude;

    // Perfil de habilidades (JSON String)
    @Column(columnDefinition = "TEXT")
    private String skills;

    // Calificación
    private Double rating;
    private Integer ratingCount;

    // Plan de Suscripción
    private String plan = "TRIAL";
    private LocalDateTime planExpiry;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    public String getRepresentativeName() { return representativeName; }
    public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public LocalDateTime getPlanExpiry() { return planExpiry; }
    public void setPlanExpiry(LocalDateTime planExpiry) { this.planExpiry = planExpiry; }
}
