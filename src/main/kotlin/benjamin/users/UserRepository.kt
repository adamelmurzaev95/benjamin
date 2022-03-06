package benjamin.users

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<UserEntity, Int> {
    fun findByUserName(userName: String): UserEntity?
    fun existsByUserName(userName: String): Boolean
}
