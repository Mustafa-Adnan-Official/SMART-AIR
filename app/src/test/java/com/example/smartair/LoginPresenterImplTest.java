package com.example.smartair;

import android.text.TextUtils;
import com.example.smartair.ui.login.mvp.LoginContract;
import com.example.smartair.ui.login.mvp.LoginPresenterImpl;
import com.example.smartair.callbacks.ChildUnderParentLoginCallback;
import com.example.smartair.callbacks.LoginResultCallback;
import com.example.smartair.callbacks.SimpleResultCallback;
import com.example.smartair.models.users.RoleType;
import com.example.smartair.services.AuthService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Example unit tests for LoginPresenterImpl.
 *
 * These tests:
 *  - mock the View and AuthService
 *  - call presenter methods
 *  - verify that the presenter behaves correctly
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterImplTest {

    @Mock
    private LoginContract.View view;

    @Mock
    private AuthService authService;

    private LoginPresenterImpl presenter;

    // Used to fake TextUtils.isEmpty() in local unit tests (no Android runtime).
    private MockedStatic<TextUtils> mockedTextUtils;

    @Before
    public void setUp() {
        // Mock Android dependency TextUtils.isEmpty(...)
        mockedTextUtils = mockStatic(TextUtils.class);
        mockedTextUtils.when(() -> TextUtils.isEmpty(any(CharSequence.class)))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    CharSequence a = invocation.getArgument(0);
                    return a == null || a.length() == 0;
                });

        presenter = new LoginPresenterImpl(view, authService);

        // Default for most tests (non-child screen, no child-under-parent mode)
        lenient().when(view.isChildMode()).thenReturn(false);
        lenient().when(view.isChildUnderParentMode()).thenReturn(false);
    }

    @After
    public void tearDown() {
        mockedTextUtils.close();
    }

    //
    // 1) VALIDATION TESTS (no Firebase calls expected)
    //

    @Test
    public void onLoginClicked_invalidEmail_showsEmailError_andDoesNotCallAuth() {
        // Arrange
        when(view.getEmail()).thenReturn("not-an-email");
        when(view.getPassword()).thenReturn("ValidPass@1");

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showEmailError("Invalid email format");
        // Password error should NOT be shown because we stop after email is invalid
        verify(view, never()).showPasswordError(anyString());
        // AuthService must NOT be called for invalid input
        verify(authService, never())
                .loginWithEmailPassword(anyString(), anyString(), any(LoginResultCallback.class));
        // Loading UI should not be shown when validation fails
        verify(view, never()).showLoading(true);
    }

    @Test
    public void onLoginClicked_invalidPassword_showsPasswordError_andDoesNotCallAuth() {
        // Arrange
        when(view.getEmail()).thenReturn("parent@example.com");
        // Intentionally invalid password (too short / no special char)
        when(view.getPassword()).thenReturn("short");

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showPasswordError(
                "Password must be ≥ 6 chars and contain a special character"
        );
        verify(authService, never())
                .loginWithEmailPassword(anyString(), anyString(), any(LoginResultCallback.class));
        verify(view, never()).showLoading(true);
    }

    //
    // 2) NORMAL EMAIL + PASSWORD LOGIN (Parent / Provider / Child)
    //

    @Test
    public void onLoginClicked_parentLoginSuccess_navigatesToParentHome() {
        // Arrange
        when(view.getEmail()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        // When loginWithEmailPassword is called, simulate success.
        doAnswer(invocation -> {
            LoginResultCallback callback = invocation.getArgument(2);
            // Simulate Firebase + Firestore success: parent, already onboarded
            callback.onSuccess("uid123", RoleType.PARENT, true);
            return null;
        }).when(authService).loginWithEmailPassword(
                anyString(), anyString(), any(LoginResultCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).navigateToParentHome();
    }

    @Test
    public void onLoginClicked_unverifiedEmail_showsEmailNotVerifiedMessage() {
        // Arrange
        when(view.getEmail()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        doAnswer(invocation -> {
            LoginResultCallback callback = invocation.getArgument(2);
            // Simulate Firebase user BUT email not verified
            callback.onUnverifiedEmail();
            return null;
        }).when(authService).loginWithEmailPassword(
                anyString(), anyString(), any(LoginResultCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).showEmailNotVerifiedMessage();
    }

    @Test
    public void onLoginClicked_childScreenWithParentRole_showsGenericError() {
        // Arrange: we are on the CHILD login screen, independent mode
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(false);
        when(view.getEmail()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        doAnswer(invocation -> {
            LoginResultCallback callback = invocation.getArgument(2);
            // Backend says: this UID is a PARENT
            callback.onSuccess("uid123", RoleType.PARENT, true);
            return null;
        }).when(authService).loginWithEmailPassword(
                anyString(), anyString(), any(LoginResultCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        // We don't care about the exact string here, just that an error is shown
        verify(view).showGenericError(anyString());
        // And we should NOT navigate to any home screen from the child UI
        verify(view, never()).navigateToParentHome();
        verify(view, never()).navigateToProviderHome();
        verify(view, never()).navigateToChildHome(anyString());
    }

    /**
     * Independent CHILD login screen:
     * valid email/password, role CHILD, already onboarded
     * → should go straight to child home.
     */
    @Test
    public void onLoginClicked_childIndependent_successNavigatesToChildHome() {
        // Arrange: child screen, independent mode
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(false);
        when(view.getEmail()).thenReturn("child@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        doAnswer(invocation -> {
            LoginResultCallback cb = invocation.getArgument(2);
            cb.onSuccess("childUid123", RoleType.CHILD, true);
            return null;
        }).when(authService).loginWithEmailPassword(
                anyString(), anyString(), any(LoginResultCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).navigateToChildHome("childUid123");
    }

    /**
     * Generic login failure (e.g., wrong password / network).
     * Presenter should stop loading and show a generic error.
     */
    @Test
    public void onLoginClicked_loginFailure_showsGenericError() {
        // Arrange
        when(view.getEmail()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        doAnswer(invocation -> {
            LoginResultCallback cb = invocation.getArgument(2);
            cb.onFailure("Network error");
            return null;
        }).when(authService).loginWithEmailPassword(
                anyString(), anyString(), any(LoginResultCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).showGenericError("Network error");
        // No navigation should happen on failure
        verify(view, never()).navigateToParentHome();
        verify(view, never()).navigateToProviderHome();
        verify(view, never()).navigateToChildHome(anyString());
    }

    //
    // 3) CHILD-UNDER-PARENT LOGIN
    //

    @Test
    public void onLoginClicked_childUnderParent_successNavigatesToChildHome() {
        // Arrange: child screen + switch ON
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(true);

        when(view.getChildName()).thenReturn("Elyas");
        when(view.getParentEmailForChildMode()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        // When loginChildUnderParent is called, simulate success via callback
        doAnswer(invocation -> {
            ChildUnderParentLoginCallback cb = invocation.getArgument(3);
            cb.onSuccess("childUid123", true); // onboarded = true
            return null;
        }).when(authService).loginChildUnderParent(
                anyString(), anyString(), anyString(), any(ChildUnderParentLoginCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).navigateToChildHome("childUid123");
    }

    /**
     * Child-under-parent login: invalid parent email.
     * Should show email error and not call the service.
     */
    @Test
    public void onLoginClicked_childUnderParent_invalidParentEmail_showsEmailErrorAndSkipsAuth() {
        // Arrange
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(true);
        when(view.getChildName()).thenReturn("Elyas"); // valid name
        when(view.getParentEmailForChildMode()).thenReturn("bad-email"); // invalid
        when(view.getPassword()).thenReturn("ValidPass@1");

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showEmailError("Invalid parent email");
        verify(view, never()).showLoading(true);
        verify(authService, never()).loginChildUnderParent(
                anyString(), anyString(), anyString(), any(ChildUnderParentLoginCallback.class)
        );
    }

    /**
     * Child-under-parent login: invalid child name.
     * Should show child name error and not call the service.
     */
    @Test
    public void onLoginClicked_childUnderParent_invalidChildName_showsChildNameErrorAndSkipsAuth() {
        // Arrange
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(true);
        when(view.getChildName()).thenReturn(""); // invalid (empty)
        when(view.getParentEmailForChildMode()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showChildNameError("Invalid child name");
        verify(view, never()).showLoading(true);
        // Email error should NOT be shown in this case
        verify(view, never()).showEmailError(anyString());
        verify(authService, never()).loginChildUnderParent(
                anyString(), anyString(), anyString(), any(ChildUnderParentLoginCallback.class)
        );
    }

    /**
     * Child-under-parent login: backend failure after validation passes.
     * Presenter should stop loading and show a generic error.
     */
    @Test
    public void onLoginClicked_childUnderParent_loginFailure_showsGenericError() {
        // Arrange
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(true);
        when(view.getChildName()).thenReturn("Elyas");
        when(view.getParentEmailForChildMode()).thenReturn("parent@example.com");
        when(view.getPassword()).thenReturn("ValidPass@1");

        doAnswer(invocation -> {
            ChildUnderParentLoginCallback cb = invocation.getArgument(3);
            cb.onFailure("Child login error");
            return null;
        }).when(authService).loginChildUnderParent(
                anyString(), anyString(), anyString(), any(ChildUnderParentLoginCallback.class)
        );

        // Act
        presenter.onLoginClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).showGenericError("Child login error");
        // No navigation on failure
        verify(view, never()).navigateToChildHome(anyString());
    }

    //
    // 4) FORGOT PASSWORD FLOWS
    //

    @Test
    public void onForgotPasswordClicked_invalidEmail_showsEmailError() {
        // Arrange: normal (non-child-under-parent) flow uses defaults from setUp()
        when(view.getEmail()).thenReturn("bad-email");

        // Act
        presenter.onForgotPasswordClicked();

        // Assert
        verify(view).showEmailError("Enter a valid email to reset password");
        verify(authService, never())
                .sendPasswordReset(anyString(), any(SimpleResultCallback.class));
    }

    @Test
    public void onForgotPasswordClicked_validEmail_sendsResetAndShowsMessage() {
        // Arrange
        when(view.getEmail()).thenReturn("parent@example.com");

        // When sendPasswordReset is called, immediately call onSuccess()
        doAnswer(invocation -> {
            SimpleResultCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(authService).sendPasswordReset(
                anyString(), any(SimpleResultCallback.class)
        );

        // Act
        presenter.onForgotPasswordClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        verify(view).showPasswordResetSent("parent@example.com");
    }

    @Test
    public void onForgotPasswordClicked_childUnderParent_flowUsesParentEmailAndChildName() {
        // Arrange: child-under-parent mode
        when(view.isChildMode()).thenReturn(true);
        when(view.isChildUnderParentMode()).thenReturn(true);
        when(view.getParentEmailForChildMode()).thenReturn("parent@example.com");
        when(view.getChildName()).thenReturn("Elyas");

        doAnswer(invocation -> {
            SimpleResultCallback cb = invocation.getArgument(2);
            cb.onSuccess();
            return null;
        }).when(authService).sendChildPasswordResetUnderParent(
                anyString(), anyString(), any(SimpleResultCallback.class)
        );

        // Act
        presenter.onForgotPasswordClicked();

        // Assert
        verify(view).showLoading(true);
        verify(view).showLoading(false);
        // We expect a reset message sent to the parent email
        verify(view).showPasswordResetSent("parent@example.com");
    }
}
