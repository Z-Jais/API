package fr.ziedelth.entities

import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

@MappedSuperclass
open class Platformeable(
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "platform_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (platform_uuid) REFERENCES platform(uuid) ON DELETE CASCADE")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var platform: Platform? = null,
)