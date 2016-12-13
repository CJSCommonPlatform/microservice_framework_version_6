package uk.gov.justice.services.example.cakeshop.query.view.service;


import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.example.cakeshop.persistence.CakeRepository;
import uk.gov.justice.services.example.cakeshop.query.view.response.CakeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.CakesView;

import javax.inject.Inject;

public class CakeService {

    @Inject
    private CakeRepository cakeRepository;

    public CakesView cakes() {
        return new CakesView(cakeRepository.findAll().stream().map(cake -> new CakeView(cake.getCakeId(), cake.getName())).collect(toList()));
    }
}
