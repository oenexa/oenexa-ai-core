package com.oenexa.application.bdd;

import com.oenexa.application.UserService;
import com.oenexa.domain.User;
import com.oenexa.domain.UserRegistration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber BDD Step Definitions for User Registration.
 * Why: Connects human-readable Gherkin scenarios to real domain and service execution.
 * How: Directly invokes real UserService without mocks.
 */
public class UserRegistrationSteps {

    private final UserService userService = new UserService();
    private UserRegistration registration;
    private User registeredUser;
    private Exception registrationError;

    @Given("a new user registration with username {string}, email {string}, and password {string}")
    public void aNewUserRegistrationWithUsernameEmailAndPassword(String username, String email, String password) {
        this.registration = new UserRegistration(username, email, password);
        this.registrationError = null;
    }

    @Given("an empty user registration")
    public void anEmptyUserRegistration() {
        this.registration = null;
        this.registrationError = null;
    }

    @When("the user registers through the user service")
    public void theUserRegistersThroughTheUserService() {
        try {
            this.registeredUser = userService.registerUser(this.registration);
        } catch (Exception e) {
            this.registrationError = e;
        }
    }

    @Then("the registered user should be enabled with username {string} and email {string}")
    public void theRegisteredUserShouldBeEnabledWithUsernameAndEmail(String expectedUsername, String expectedEmail) {
        assertNull(registrationError, "Registration should not have thrown an exception");
        assertNotNull(registeredUser);
        assertEquals(expectedUsername, registeredUser.username());
        assertEquals(expectedEmail, registeredUser.email());
        assertTrue(registeredUser.isEnabled());
    }

    @Then("registration should be rejected with an error")
    public void registrationShouldBeRejectedWithAnError() {
        assertNotNull(registrationError, "Expected registration exception but none was thrown");
        assertInstanceOf(NullPointerException.class, registrationError);
    }
}
