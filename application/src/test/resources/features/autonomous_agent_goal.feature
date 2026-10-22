@bdd
Feature: Autonomous AI Engineer Goal Planning and Execution
  As an engineering lead
  I want the OENEXA Autonomous Agent to decompose and execute complex engineering tasks
  So that repository refactoring, bug fixing, and testing proceed autonomously with self-healing

  Scenario: Plan and execute an autonomous engineering goal successfully
    Given an engineering goal "Implement secure OAuth2 token rotation with redis blacklisting" for repository "repo-core" and user "lead-dev"
    When the autonomous agent engine executes the engineering goal
    Then the goal execution should complete with status "COMPLETED"
    And the plan should contain decomposed steps
    And the goal events should record planning and execution phases

  Scenario: Trigger autonomous self-healing when encountering an execution failure
    Given an existing goal with identifier "goal-self-heal-test"
    When a compilation or test failure "Compilation error: Symbol not found" is reported
    Then the agent triggers a self-healing cycle
    And the self-healing cycle records the detected issue and corrective resolution
