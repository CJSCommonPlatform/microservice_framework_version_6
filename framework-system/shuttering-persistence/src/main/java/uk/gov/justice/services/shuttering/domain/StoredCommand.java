package uk.gov.justice.services.shuttering.domain;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class StoredCommand {

    private final UUID envelopeId;
    private final String commandJsonEnvelope;
    private final String destination;
    private final ZonedDateTime dateReceived;

    public StoredCommand(
            final UUID envelopeId,
            final String commandJsonEnvelope,
            final String destination,
            final ZonedDateTime dateReceived) {
        this.envelopeId = envelopeId;
        this.commandJsonEnvelope = commandJsonEnvelope;
        this.destination = destination;
        this.dateReceived = dateReceived;
    }

    public UUID getEnvelopeId() {
        return envelopeId;
    }

    public String getCommandJsonEnvelope() {
        return commandJsonEnvelope;
    }

    public String getDestination() {
        return destination;
    }

    public ZonedDateTime getDateReceived() {
        return dateReceived;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof StoredCommand)) return false;
        final StoredCommand that = (StoredCommand) o;
        return Objects.equals(envelopeId, that.envelopeId) &&
                Objects.equals(commandJsonEnvelope, that.commandJsonEnvelope) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(dateReceived, that.dateReceived);
    }

    @Override
    public int hashCode() {
        return Objects.hash(envelopeId, commandJsonEnvelope, destination, dateReceived);
    }

    @Override
    public String toString() {
        return "ShutteredCommand{" +
                "envelopeId=" + envelopeId +
                ", commandJsonEnvelope='" + commandJsonEnvelope + '\'' +
                ", destination='" + destination + '\'' +
                ", dateReceived=" + dateReceived +
                '}';
    }
}
