package co.com.crediya.reportes.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllowedRolesTest {

    @Test
    void shouldHaveAdminRole() {
        // When & Then
        AllowedRoles admin = AllowedRoles.ADMIN;
        assertNotNull(admin);
        assertEquals("ADMIN", admin.name());
    }

    @Test
    void shouldHaveAsesorRole() {
        // When & Then
        AllowedRoles asesor = AllowedRoles.ASESOR;
        assertNotNull(asesor);
        assertEquals("ASESOR", asesor.name());
    }

    @Test
    void shouldHaveExactlyTwoRoles() {
        // When
        AllowedRoles[] roles = AllowedRoles.values();

        // Then
        assertEquals(2, roles.length);
        assertTrue(java.util.Arrays.asList(roles).contains(AllowedRoles.ADMIN));
        assertTrue(java.util.Arrays.asList(roles).contains(AllowedRoles.ASESOR));
    }

    @Test
    void shouldSupportValueOfMethod() {
        // When & Then
        assertEquals(AllowedRoles.ADMIN, AllowedRoles.valueOf("ADMIN"));
        assertEquals(AllowedRoles.ASESOR, AllowedRoles.valueOf("ASESOR"));
    }

    @Test
    void shouldThrowExceptionForInvalidRole() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            AllowedRoles.valueOf("INVALID_ROLE");
        });
    }

    @Test
    void shouldHaveCorrectOrdinalValues() {
        // When & Then
        assertEquals(0, AllowedRoles.ADMIN.ordinal());
        assertEquals(1, AllowedRoles.ASESOR.ordinal());
    }
}
