package benjamin.users

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserService(
    private val repo: UserRepository
) {
    private val hasher = BCryptPasswordEncoder()

    @Transactional
    fun register(command: RegisterUserCommand): RegisterUserResult {
        if (repo.existsByUserName(command.userName)) {
            return RegisterUserResult.AlreadyExists
        }

        val userEntity = toEntity(command)
        repo.save(userEntity)
        return RegisterUserResult.Success
    }

    @Transactional(readOnly = true)
    fun getByUserName(userName: String): User? {
        val userEntity = repo.findByUserName(userName)
        return if (userEntity != null) fromEntity(userEntity) else null
    }

    @Transactional(readOnly = true)
    fun checkCredentials(userName: String, password: String): Boolean {
        val userEntity = repo.findByUserName(userName)
        if (userEntity == null) return false
        return hasher.matches(password, userEntity.passwordHash)
    }

    private fun fromEntity(userEntity: UserEntity): User {
        return User(
            userName = userEntity.userName,
            firstName = userEntity.firstName,
            lastName = userEntity.lastName,
            email = userEntity.email
        )
    }

    private fun toEntity(command: RegisterUserCommand): UserEntity {
        return UserEntity().apply {
            userName = command.userName
            firstName = command.firstName
            lastName = command.lastName
            email = command.email
            passwordHash = hasher.encode(command.password)
        }
    }
}
