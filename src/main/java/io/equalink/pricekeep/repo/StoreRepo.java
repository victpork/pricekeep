package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.data.StoreGroup;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import org.hibernate.annotations.processing.Pattern;

import java.util.List;
import java.util.Optional;


@Repository
public interface StoreRepo {
    @Find
    StoreGroup findStoreGroupByName(@Pattern String name);

    @Find
    List<Store> findStoreByName(@Pattern String name);

    @Find
    Optional<Store> findById(Long id);

    @Save
    void persist(Store store);

    @Query("select s, geo_distance(s.geoPoint, geo_point(:latitude, :longitude)) as distance from Store s order by distance asc")
    Store locateNearestStore(double latitude, double longitude);
}
