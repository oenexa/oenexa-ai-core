@bdd
Feature: Oenexa Memory Engine V2 SSD-First Hierarchical Retrieval
  As an AI software engineer
  I want a multi-tier memory system (L1, L2, RAM, Compressed, SSD, Knowledge)
  So that massive repositories operate seamlessly with minimal RAM overhead

  Scenario: Hierarchical memory retrieval escalates from knowledge storage to hot cache
    Given a memory engine initialized with sample repository files
    When the user requests file "UserService.java" in workspace "default-ws"
    Then the data movement pipeline returns the item successfully
    And the item is promoted to the L1 cache
    And subsequent retrieval hits the hot cache immediately

  Scenario: Intelligent context compression reduces context size
    Given a large context buffer of 1000 lines with duplicate import statements
    When the context compression engine processes the buffer
    Then the compressed size should be significantly smaller than original size
    And critical function signatures must be preserved
