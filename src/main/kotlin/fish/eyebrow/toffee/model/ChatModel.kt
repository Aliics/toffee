package fish.eyebrow.toffee.model

import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "chat")
class ChatModel {
    @Id
    var id: Int = 0
    var text: String = ""
    var author: String = ""
    var timestamp: Timestamp = Timestamp(System.currentTimeMillis())
}