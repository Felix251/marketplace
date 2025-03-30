package com.example.marketplace.repository;

import com.example.marketplace.model.user.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id = :addressId")
    Optional<Address> findByUserIdAndAddressId(@Param("userId") Long userId, @Param("addressId") Long addressId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void resetDefaultAddress(@Param("userId") Long userId);

    @Query("SELECT DISTINCT a.country FROM Address a")
    List<String> findAllCountries();

    @Query("SELECT DISTINCT a.city FROM Address a WHERE a.country = :country")
    List<String> findCitiesByCountry(@Param("country") String country);

    @Query(value = "SELECT country, COUNT(*) as address_count " +
            "FROM addresses " +
            "GROUP BY country " +
            "ORDER BY address_count DESC",
            nativeQuery = true)
    List<Object[]> countAddressesByCountry();
}