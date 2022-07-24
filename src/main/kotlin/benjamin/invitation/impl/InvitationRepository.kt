package benjamin.invitation.impl

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InvitationRepository : JpaRepository<InvitationEntity, Int> {
    fun findByInvitationUuid(uuid: UUID): InvitationEntity?
}
