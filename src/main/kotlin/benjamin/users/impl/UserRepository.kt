package benjamin.users.impl

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<UserEntity, Int> {
    fun findByUserName(userName: String): UserEntity?
    fun existsByUserName(userName: String): Boolean
}
