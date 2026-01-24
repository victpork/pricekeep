package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "group_prd_code")
@NoArgsConstructor
public class GroupProductCode {

    @EmbeddedId
    private GroupProductCodeId id;

    @ManyToOne
    @MapsId("productId") // Maps the productId field of the composite key
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @MapsId("storeGroupId") // Maps the storeGroupId field of the composite key
    @JoinColumn(name= "storegroup_id")
    private StoreGroup storeGroup;

    @Column(name = "internal_code")
    private String internalCode;

}
