package uk.gov.justice.services.example.cakeshop.custom.api.response;

import java.util.List;

public class OvensStatus {

    private final List<OvenStatus> ovens;

    public OvensStatus(final List<OvenStatus> ovens) {
        this.ovens = ovens;
    }

    public List<OvenStatus> getOvens() {
        return ovens;
    }
}
