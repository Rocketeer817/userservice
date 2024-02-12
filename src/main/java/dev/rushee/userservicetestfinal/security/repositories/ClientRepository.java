package dev.rushee.userservicetestfinal.security.repositories;

import java.util.Optional;

import dev.rushee.userservicetestfinal.security.models.AuthorizationConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import dev.rushee.userservicetestfinal.security.models.Client;


@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
}



