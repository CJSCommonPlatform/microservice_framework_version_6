package uk.gov.justice.services.publishing.command.handler;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("publish.event.robot-added")
public class RobotAdded {

    private final UUID robotId;
    private final String robotType;
    private final boolean evil;
    private final boolean brainTheSizeOfAPlanet;

    public RobotAdded(
            final UUID robotId,
            final String robotType,
            final boolean evil,
            final boolean brainTheSizeOfAPlanet) {
        this.robotId = robotId;
        this.robotType = robotType;
        this.evil = evil;
        this.brainTheSizeOfAPlanet = brainTheSizeOfAPlanet;
    }

    public UUID getRobotId() {
        return robotId;
    }

    public String getRobotType() {
        return robotType;
    }

    public boolean isEvil() {
        return evil;
    }

    public boolean isBrainTheSizeOfAPlanet() {
        return brainTheSizeOfAPlanet;
    }
}
