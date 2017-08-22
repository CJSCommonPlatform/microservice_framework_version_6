package uk.gov.justice.services.eventsourcing.source.api.feed.event;


public class EventPayload {

    private final String streamId;
    private final String payloadContent;

    public EventPayload(final String streamId, final String payloadContent) {
        this.streamId = streamId;
        this.payloadContent = payloadContent;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getPayloadContent() {
        return payloadContent;
    }

}
