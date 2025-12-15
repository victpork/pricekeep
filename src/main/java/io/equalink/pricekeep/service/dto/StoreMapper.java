package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.repo.StoreRepo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface StoreMapper {

    StoreInfo toDTO(Store s);

    @Mapping(target = "geoPoint", ignore = true)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "group", ignore = true)
    Store toEntity(StoreInfo sDTO);

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
