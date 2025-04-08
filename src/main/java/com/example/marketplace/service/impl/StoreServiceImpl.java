package com.example.marketplace.service.impl;

import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.model.store.Store;
import com.example.marketplace.model.user.User;
import com.example.marketplace.model.user.UserRole;
import com.example.marketplace.repository.jpa.StoreRepository;
import com.example.marketplace.service.StoreService;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserService userService;

    @Override
    @Cacheable(value = "stores", key = "#id")
    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
    }

    @Override
    public Store getStoreByOwnerId(Long ownerId) {
        return storeRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found for owner id: " + ownerId));
    }

    @Override
    public Page<Store> getAllStores(Pageable pageable) {
        return storeRepository.findAll(pageable);
    }

    @Override
    public Page<Store> searchStores(String keyword, Pageable pageable) {
        return storeRepository.searchStores(keyword, pageable);
    }

    @Override
    public Page<Store> getStoresByName(String name, Pageable pageable) {
        return storeRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public List<Store> getActiveStores() {
        return storeRepository.findByActiveTrue();
    }

    @Override
    @Transactional
    public Store createStore(Long userId, Store store) {
        User owner = userService.getUserById(userId);

        // Check if user already has a store
        if (storeRepository.findByOwnerId(userId).isPresent()) {
            throw new IllegalArgumentException("User already has a store");
        }

        // Ensure user has SELLER role
        if (owner.getRole() != UserRole.SELLER) {
            owner.setRole(UserRole.SELLER);
            userService.changeUserRole(userId, UserRole.SELLER);
        }

        store.setOwner(owner);

        // Set active by default
        if (store.getActive() == null) {
            store.setActive(true);
        }

        return storeRepository.save(store);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stores", key = "#id")
    public Store updateStore(Long id, Store storeDetails) {
        Store store = getStoreById(id);

        // Update store details
        if (storeDetails.getName() != null) {
            store.setName(storeDetails.getName());
        }
        if (storeDetails.getDescription() != null) {
            store.setDescription(storeDetails.getDescription());
        }
        if (storeDetails.getLogo() != null) {
            store.setLogo(storeDetails.getLogo());
        }
        if (storeDetails.getBanner() != null) {
            store.setBanner(storeDetails.getBanner());
        }
        if (storeDetails.getActive() != null) {
            store.setActive(storeDetails.getActive());
        }

        return storeRepository.save(store);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stores", key = "#id")
    public void deleteStore(Long id) {
        Store store = getStoreById(id);

        // Revert owner's role to BUYER
        User owner = store.getOwner();
        owner.setRole(UserRole.BUYER);
        userService.changeUserRole(owner.getId(), UserRole.BUYER);

        storeRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "stores", key = "#id")
    public void toggleStoreStatus(Long id) {
        Store store = getStoreById(id);
        store.setActive(!store.getActive());
        storeRepository.save(store);
    }

    @Override
    public List<Store> getTopStoresByOrderCount(int limit) {
        return storeRepository.findTopStoresByOrderCount(limit);
    }

    @Override
    public List<Store> getNewStores() {
        return storeRepository.findNewStores();
    }

    @Override
    public boolean isUserStoreOwner(Long userId, Long storeId) {
        Store store = getStoreById(storeId);
        return store.getOwner().getId().equals(userId);
    }
}