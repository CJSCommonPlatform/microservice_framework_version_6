package uk.gov.justice.services.messaging.subscription.cms;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;

import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptorDef;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class SubscriptionDescriptorDefLoader {

    public SubscriptionDescriptorDef loadFrom(final Path absolutePath) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule(PROPERTIES));

        mapper.setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        return mapper.readValue(absolutePath.toFile(), SubscriptionDescriptorDef.class);
    }
}
