package benjamin.users

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "user_name")
    lateinit var userName: String

    @Column(name = "first_name")
    lateinit var firstName: String

    @Column(name = "last_name")
    lateinit var lastName: String

    @Column(name = "email")
    lateinit var email: String

    @Column(name = "password_hash")
    lateinit var passwordHash: String
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserEntity) return false
        return userName == other.userName
    }

    override fun hashCode(): Int {
        return userName.hashCode()
    }
}
