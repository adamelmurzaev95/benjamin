package benjamin.users.impl

import benjamin.users.api.User
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock
    private lateinit var usersFetcher: UsersFetcher

    private val users = listOf(
        User(
            username = "a.elmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev"
        )
    )

    @Test
    fun `existsByUserName should return true if users list is not empty`() {
        Mockito.`when`(usersFetcher.fetchByUserName("adamelmurzaev95"))
            .thenReturn(users)

        assertTrue(UserService(usersFetcher).existsByUserName("adamelmurzaev95"))
    }

    @Test
    fun `existsByUserName should return false if users list is empty`() {
        Mockito.`when`(usersFetcher.fetchByUserName("adamelmurzaev95"))
            .thenReturn(emptyList())

        assertFalse(UserService(usersFetcher).existsByUserName("adamelmurzaev95"))
    }
}
