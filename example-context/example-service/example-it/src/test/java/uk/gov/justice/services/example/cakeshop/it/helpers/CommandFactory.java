package uk.gov.justice.services.example.cakeshop.it.helpers;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

public class CommandFactory {

    public String addRecipeCommand() {
        return createObjectBuilder()
                .add("name", "Chocolate muffin in six easy steps")
                .add("glutenFree", false)
                .add("ingredients", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("name", "chocolate")
                                .add("quantity", 1)
                        ).build())
                .build().toString();
    }
}
