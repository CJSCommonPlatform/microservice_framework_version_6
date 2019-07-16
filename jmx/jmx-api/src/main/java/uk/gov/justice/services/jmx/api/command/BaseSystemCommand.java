package uk.gov.justice.services.jmx.api.command;

import java.util.Objects;

public abstract class BaseSystemCommand implements SystemCommand {

    private final String name;
    private final String description;

    protected BaseSystemCommand(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseSystemCommand)) return false;
        final BaseSystemCommand that = (BaseSystemCommand) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
