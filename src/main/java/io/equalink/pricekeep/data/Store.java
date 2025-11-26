package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.EmbeddedColumnNaming;
import org.locationtech.jts.geom.Point;

@Setter
@Getter
@Entity
@Table(
    name = "store",
    indexes = {
        @Index(name = "nameIdx", columnList = "name"),
        @Index(name = "geoLocIdx", columnList = "geoPoint"),
        @Index(name = "group_index", columnList = "group_id,int_id")
    }
)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private String url;

    private Point geoPoint;
    private String address;

    @Column(name = "int_id")
    private String internalId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private StoreGroup group;
}
