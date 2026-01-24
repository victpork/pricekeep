package io.equalink.pricekeep.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupProductCodeId implements Serializable {
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "storegroup_id")
    private Long storeGroupId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GroupProductCodeId that = (GroupProductCodeId) o;
        return Objects.equals(productId, that.productId) && Objects.equals(storeGroupId, that.storeGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, storeGroupId);
    }
}
