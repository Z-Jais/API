package fr.ziedelth.entities

import jakarta.persistence.*

@MappedSuperclass
open class Platformeable(
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "platform_uuid",
        nullable = false,
        foreignKey = ForeignKey(foreignKeyDefinition = "FOREIGN KEY (platform_uuid) REFERENCES platform(uuid) ON DELETE CASCADE")
    )
    var platform: Platform? = null,
)