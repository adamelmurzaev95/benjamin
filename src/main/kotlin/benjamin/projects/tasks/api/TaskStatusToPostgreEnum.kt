package benjamin.projects.tasks.api

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.type.EnumType
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class TaskStatusToPostgreEnum : EnumType<TaskStatus>() {
    override fun nullSafeGet(
        rs: ResultSet,
        names: Array<out String>,
        session: SharedSessionContractImplementor?,
        owner: Any?
    ): TaskStatus? {
        val enum = rs.getObject(names[0]) ?: return null
        return when (enum) {
            is PGobject -> {
                val enumValue = enum.value
                if (enumValue != null) TaskStatus.valueOf(enumValue) else null
            }
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
