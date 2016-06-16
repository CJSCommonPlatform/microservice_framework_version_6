package uk.gov.justice.services.example.cakeshop.query.view.service;

import uk.gov.justice.services.example.cakeshop.persistence.CakeOrderRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.CakeOrder;
import uk.gov.justice.services.example.cakeshop.query.view.response.CakeOrderView;

import java.util.UUID;

import javax.inject.Inject;

public class CakeOrderService {

    @Inject
    private CakeOrderRepository repository;

    public CakeOrderView findOrder(final String orderId) {
        final CakeOrder cakeOrder = repository.findBy(UUID.fromString(orderId));
        return cakeOrder != null ? new CakeOrderView(cakeOrder.getOrderId(), cakeOrder.getRecipeId(), cakeOrder.getDeliveryDate()) : null;
    }
}