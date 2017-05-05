Feature: Recipe Management

  Scenario: Add a recipe in system

    Given no previous events in system
    When addRecipe to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using add-recipe
    Then the recipe-added


  Scenario: Rename a recipe in system

    Given no previous events in system
    When addRecipe to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using add-recipe
    Then the recipe-added
    When renameRecipe to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using rename-recipe
    Then the recipe-renamed


  Scenario: Remove a recipe in system

    Given no previous events in system
    When addRecipe to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using add-recipe
    Then the recipe-added
    When removeRecipe to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using remove-recipe
    Then the recipe-removed


  Scenario: Make Cake

    Given no previous events in system
    When addRecipe to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using add-recipe
    Then the recipe-added
    When makeCake to a uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe using make-cake
    Then the cake-made

