package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "group_prd_code")
public class GroupProductCode {

    @Embeddable
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class GroupProductId implements Serializable {
        @Column(name = "product_id")
        private Long productId;
        @Column(name = "store_group_id")
        private Long storeGroupId;
    }

    @EmbeddedId
    private GroupProductId id;

    @ManyToOne
    @MapsId("productId") // Maps the productId field of the composite key
    private Product product;

    @ManyToOne
    @MapsId("storeGroupId") // Maps the storeGroupId field of the composite key
    private StoreGroup storeGroup;

    @Column(name = "internal_code")
    private String internalCode;

    public GroupProductCode() {
        this.id = new GroupProductId();
    }
}
