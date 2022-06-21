package benjamin.projects.impl

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.type.EnumType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class ProjectRoleToPostgreEnum : EnumType<ProjectRole>() {
    override fun nullSafeGet(
        rs: ResultSet,
        names: Array<out String>,
        session: SharedSessionContractImplementor?,
        owner: Any?
    ): ProjectRole? {
        val enum = rs.getObject(names[0]) ?: return null
        return when (enum) {
            is String -> ProjectRole.valueOf(enum)
            else -> null
        }
    }

    override fun nullSafeSet(
        st: PreparedStatement,
        value: Any?,
        index: Int,
        session: SharedSessionContractImplementor?
    ) {
        when (value) {
            null -> st.setNull(index, Types.OTHER)
            else -> st.setObject(index, value.toString(), Types.OTHER)
        }
    }
}
