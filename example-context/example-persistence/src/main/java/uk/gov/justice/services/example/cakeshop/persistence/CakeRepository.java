package uk.gov.justice.services.example.cakeshop.persistence;


import uk.gov.justice.services.example.cakeshop.persistence.entity.Cake;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface CakeRepository extends EntityRepository<Cake, UUID> {
}
