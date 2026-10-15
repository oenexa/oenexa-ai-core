package ai.oenexa.testing.service;

import ai.oenexa.testing.domain.CoverageReport;
import ai.oenexa.testing.domain.GeneratedTestSuite;
import ai.oenexa.testing.domain.TestGenerationRequest;

/**
 * Service port for generating test suites and verifying coverage quality gates.
 */
public interface TestingService {

    GeneratedTestSuite generateTests(TestGenerationRequest request);

    CoverageReport evaluateCoverage(String moduleName, int linesTotal, int linesCovered, int branchesTotal, int branchesCovered, double thresholdPercent);
}
