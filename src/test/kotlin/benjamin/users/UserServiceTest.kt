package benjamin.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserService::class)
class UserServiceTest {
    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `register should return AlreadyExists if such user name already exists in db`() {
        val registerCommand = RegisterUserCommand(
            userName = "adamelmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev",
            email = "adamelmurzaev@gmail.com",
            password = "user123"
        )

        userService.register(registerCommand)

        assertEquals(
            RegisterUserResult.AlreadyExists,
            userService.register(registerCommand)
        )
    }

    @Test
    fun `register should return Success if user with such user name doesnt exist`() {
        val registerCommand = RegisterUserCommand(
            userName = "adamelmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev",
            email = "adamelmurzaev@gmail.com",
            password = "user123"
        )

        assertEquals(
            RegisterUserResult.Success,
            userService.register(registerCommand)
        )
    }

    @Test
    fun `getByUserName should return null if user with such user name doesnt exist`() {
        assertEquals(
            null,
            userService.getByUserName("adamelmurzaev95")
        )
    }

    @Test
    fun `getByUserName should return correct user if user with such user name exist`() {
        val registerCommand = RegisterUserCommand(
            userName = "adamelmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev",
            email = "adamelmurzaev@gmail.com",
            password = "user123"
        )

        userService.register(registerCommand)

        val expected = User(
            userName = "adamelmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev",
            email = "adamelmurzaev@gmail.com"
        )

        assertEquals(
            expected,
            userService.getByUserName("adamelmurzaev95")
        )
    }

    @Test
    fun `checkCredentials should return false if user with such user name doesnt exist`() {
        assertEquals(
            false,
            userService.checkCredentials("adamelmurzaev95", "user123")
        )
    }

    @Test
    fun `checkCredentials should return false if passwords dont match`() {
        val registerCommand = RegisterUserCommand(
            userName = "adamelmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev",
            email = "adamelmurzaev@gmail.com",
            password = "user123"
        )

        userService.register(registerCommand)

        assertEquals(
            false,
            userService.checkCredentials("adamelmurzaev95", "user1234")
        )
    }

    @Test
    fun `checkCredentials should return true if passwords match`() {
        val registerCommand = RegisterUserCommand(
            userName = "adamelmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev",
            email = "adamelmurzaev@gmail.com",
            password = "user123"
        )

        userService.register(registerCommand)

        assertEquals(
            true,
            userService.checkCredentials("adamelmurzaev95", "user123")
        )
    }
}