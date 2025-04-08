package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.user.Address;
import com.example.marketplace.model.user.User;
import com.example.marketplace.repository.jpa.AddressRepository;
import com.example.marketplace.service.AddressService;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Override
    public List<Address> getUserAddresses(Long userId) {
        // Verify that user exists
        userService.getUserById(userId);

        return addressRepository.findByUserId(userId);
    }

    @Override
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
    }

    @Override
    public Address getUserAddressById(Long userId, Long addressId) {
        return addressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId + " for user id: " + userId));
    }

    @Override
    @Transactional
    public Address createAddress(Long userId, Address address) {
        User user = userService.getUserById(userId);

        address.setUser(user);

        // If this is the first address or explicitly set as default
        if (addressRepository.findByUserIdAndIsDefaultTrue(userId).isEmpty() ||
                Boolean.TRUE.equals(address.getIsDefault())) {
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Long userId, Long addressId, Address addressDetails) {
        Address address = getUserAddressById(userId, addressId);

        // Update address fields
        if (addressDetails.getStreet() != null) {
            address.setStreet(addressDetails.getStreet());
        }
        if (addressDetails.getCity() != null) {
            address.setCity(addressDetails.getCity());
        }
        if (addressDetails.getState() != null) {
            address.setState(addressDetails.getState());
        }
        if (addressDetails.getPostalCode() != null) {
            address.setPostalCode(addressDetails.getPostalCode());
        }
        if (addressDetails.getCountry() != null) {
            address.setCountry(addressDetails.getCountry());
        }

        // If setting as default address
        if (Boolean.TRUE.equals(addressDetails.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            setDefaultAddress(userId, addressId);
        }

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = getUserAddressById(userId, addressId);

        // If deleting the default address, we need to set another as default if available
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // Reset all current default addresses
        addressRepository.resetDefaultAddress(userId);

        // Set the new default address
        Address address = getUserAddressById(userId, addressId);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Override
    public List<String> getAllCountries() {
        return addressRepository.findAllCountries();
    }

    @Override
    public List<String> getCitiesByCountry(String country) {
        return addressRepository.findCitiesByCountry(country);
    }

    @Override
    public Map<String, Long> getAddressCountByCountry() {
        List<Object[]> results = addressRepository.countAddressesByCountry();
        // Convert result to Map<String, Long>
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }
}