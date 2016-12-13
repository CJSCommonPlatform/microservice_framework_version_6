package uk.gov.justice.services.example.cakeshop.query.view.response;

import java.util.List;

public class CakesView {

    private final List<CakeView> cakes;

    public CakesView(final List<CakeView> cakes) {
        this.cakes = cakes;
    }

    public List<CakeView> getCakes() {
        return cakes;
    }
}
