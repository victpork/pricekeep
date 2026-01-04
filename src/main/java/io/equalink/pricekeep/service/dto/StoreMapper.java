package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.repo.StoreRepo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface StoreMapper {

    @Mapping(target = "storeGroupLogoPath", source = "s.group.iconPath")
    StoreInfo toDTO(Store s);

    //@Mapping(target = "geoPoint", ignore = true)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "group", ignore = true)
    Store toEntity(StoreInfo sDTO);

    @Mapping(target = "id", source = "storeId")
    @Mapping(target = "name", source = "storeName")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "storeGroupLogoPath", ignore = true)
    StoreInfo toDTO(CompactQuote q);

    default BaseEntity<StoreInfo> toBaseEntity(CompactQuote cq) {
        return new BaseEntity.WithDetail<>(toDTO(cq));
    }

    default BaseEntity<StoreInfo> toBaseEntity(Store s) {
        return new BaseEntity.WithDetail<>(toDTO(s));
    }

    default Store fromBaseEntity(BaseEntity<StoreInfo> sBase, StoreRepo storeRepo) {
        return switch (sBase) {
            case BaseEntity.WithDetail<StoreInfo> beStore -> toEntity(beStore.value());
            case BaseEntity.WithId<StoreInfo> idOnly -> storeRepo.findById(idOnly.id()).orElseThrow();
        };
    }
}
