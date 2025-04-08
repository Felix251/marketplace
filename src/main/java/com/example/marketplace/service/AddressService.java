package com.example.marketplace.service;

import com.example.marketplace.model.user.Address;

import java.util.List;
import java.util.Map;

public interface AddressService {

    List<Address> getUserAddresses(Long userId);

    Address getAddressById(Long addressId);

    Address getUserAddressById(Long userId, Long addressId);

    Address createAddress(Long userId, Address address);

    Address updateAddress(Long userId, Long addressId, Address addressDetails);

    void deleteAddress(Long userId, Long addressId);

    void setDefaultAddress(Long userId, Long addressId);

    List<String> getAllCountries();

    List<String> getCitiesByCountry(String country);

    Map<String, Long> getAddressCountByCountry();
}