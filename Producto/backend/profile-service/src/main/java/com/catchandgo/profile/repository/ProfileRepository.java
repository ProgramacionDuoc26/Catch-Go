package com.catchandgo.profile.repository;

import com.catchandgo.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(String userId);
    Optional<Profile> findFirstByUserId(String userId);
}
