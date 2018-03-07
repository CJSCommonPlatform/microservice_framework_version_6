package uk.gov.justice.services.eventsourcing.repository.jdbc;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface EventStreamMetadata {

     /**
      * Get the stream id.
      * @return the id of the stream
      */
     UUID getStreamId();

     /**
      * Get the stream position.
      * @return the position of the stream.
      */
     long getPosition();

     /**
      * Is it an active stream
      * @return boolean flag if the stream is active
      */
     boolean isActive();

     /**
      * Get the stream created time.
      * @return created time
      */
     ZonedDateTime getCreatedAt();
}
