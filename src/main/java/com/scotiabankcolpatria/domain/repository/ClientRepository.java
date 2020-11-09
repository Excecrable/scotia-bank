package com.scotiabankcolpatria.domain.repository;

import com.scotiabankcolpatria.domain.entity.Client;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, Integer>, Serializable {

    Optional<Client> findByName(String name);

}
