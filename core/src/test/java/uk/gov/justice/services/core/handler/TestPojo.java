package uk.gov.justice.services.core.handler;

public class TestPojo {

    private String payloadId;
    long payloadVersion;
    String payloadName;

    public TestPojo() {}

    public String getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(String payloadId) {
        this.payloadId = payloadId;
    }

    public long getPayloadVersion() {
        return payloadVersion;
    }

    public void setPayloadVersion(long payloadVersion) {
        this.payloadVersion = payloadVersion;
    }

    public String getPayloadName() {
        return payloadName;
    }

    public void setPayloadName(String payloadName) {
        this.payloadName = payloadName;
    }
}
