package uk.gov.justice.services.jmx.command;

public class TestCommandHandler {

    @HandlesSystemCommand("some-command_1")
    public void validHandlerMethod(final SystemCommand systemCommand) {
        
    }

    @HandlesSystemCommand("some-command_2")
    private void invalidPrivateHandlerMethod(final SystemCommand systemCommand) {

    }

    @HandlesSystemCommand("some-command_3")
    public void invalidMissingParameterHandlerMethod() {

    }

    @HandlesSystemCommand("some-command_4")
    public void invalidTooManyParametersHandlerMethod(final SystemCommand systemCommand, final String someString) {

    }

    @HandlesSystemCommand("some-command_5")
    public void invalidNoSystemCommandHandlerMethod(final String someString) {

    }
}
