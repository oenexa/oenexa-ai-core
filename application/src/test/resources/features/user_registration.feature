Feature: User Registration Management
  As an application user
  I want to register an account
  So that I can use the OENEXA AI Engineer platform securely

  Scenario: Register user with valid attributes
    Given a new user registration with username "david", email "david@oenexa.com", and password "strong_pass_123"
    When the user registers through the user service
    Then the registered user should be enabled with username "david" and email "david@oenexa.com"

  Scenario: Attempt user registration with null payload
    Given an empty user registration
    When the user registers through the user service
    Then registration should be rejected with an error
