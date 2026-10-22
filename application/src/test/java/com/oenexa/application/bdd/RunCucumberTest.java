package com.oenexa.application.bdd;

import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Cucumber JUnit Platform Suite Runner for BDD Scenarios.
 * Why: Executes Gherkin feature scenarios against real application components.
 * How: @Suite with @IncludeEngines("cucumber") and glue path configuration.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.oenexa.application.bdd")
@Tag("bdd")
public class RunCucumberTest {
}
