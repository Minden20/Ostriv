import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.minden.util.Validator;
import com.minden.exception.ValidationException;

public class ValidatorTest {

    // --- ТЕСТИ ДЛЯ EMAIL ---

    @Test
    void shouldPassValidEmails() {
        assertDoesNotThrow(() -> Validator.validateEmail("user@example.com"));
        assertDoesNotThrow(() -> Validator.validateEmail("player.one@rpg-game.org"));
        assertDoesNotThrow(() -> Validator.validateEmail("admin-test@sub.domain.ua"));
    }

    @Test
    void shouldFailEmptyEmails() {
        assertThrows(ValidationException.class, () -> Validator.validateEmail(""));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("   "));
        assertThrows(ValidationException.class, () -> Validator.validateEmail(null));
    }

    @Test
    void shouldFailInvalidEmails() {
        assertThrows(ValidationException.class, () -> Validator.validateEmail("plainaddress"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("#@%^%#$@#$@#.com"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("@domain.com"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("Joe Smith <email@domain.com>"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("email.domain.com"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("email@domain@domain.com"));
    }

    // --- ТЕСТИ ДЛЯ ПАРОЛІВ ---

    @Test
    void shouldPassValidPasswords() {
        assertDoesNotThrow(() -> Validator.validatePassword("strongpassword123"));
        assertDoesNotThrow(() -> Validator.validatePassword("admin123")); // Рівно 8 символів
    }

    @Test
    void shouldFailEmptyPasswords() {
        assertThrows(ValidationException.class, () -> Validator.validatePassword(""));
        assertThrows(ValidationException.class, () -> Validator.validatePassword("   "));
        assertThrows(ValidationException.class, () -> Validator.validatePassword(null));
    }

    @Test
    void shouldFailShortPasswords() {
        assertThrows(ValidationException.class, () -> Validator.validatePassword("1234567")); // 7 символів (менше 8)
        assertThrows(ValidationException.class, () -> Validator.validatePassword("abc"));
    }

    // --- ТЕСТИ ДЛЯ ІМЕНІ КОРИСТУВАЧА (USERNAME) ---

    @Test
    void shouldPassValidUsernames() {
        assertDoesNotThrow(() -> Validator.validateUsername("adm")); // Рівно 3 символи
        assertDoesNotThrow(() -> Validator.validateUsername("player1"));
        assertDoesNotThrow(() -> Validator.validateUsername("SuperHero"));
    }

    @Test
    void shouldFailEmptyUsernames() {
        assertThrows(ValidationException.class, () -> Validator.validateUsername(""));
        assertThrows(ValidationException.class, () -> Validator.validateUsername("   "));
        assertThrows(ValidationException.class, () -> Validator.validateUsername(null));
    }

    @Test
    void shouldFailShortUsernames() {
        assertThrows(ValidationException.class, () -> Validator.validateUsername("jo")); // 2 символи (менше 3)
        assertThrows(ValidationException.class, () -> Validator.validateUsername("a"));
    }
}
