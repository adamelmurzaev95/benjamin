package benjamin.invitation.impl

import org.springframework.data.repository.CrudRepository

interface InvitationEventRepository : CrudRepository<InvitationEventEntity, Int>
