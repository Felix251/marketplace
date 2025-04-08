package com.example.marketplace.service;

import com.example.marketplace.model.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreService {

    Store getStoreById(Long id);

    Store getStoreByOwnerId(Long ownerId);

    Page<Store> getAllStores(Pageable pageable);

    Page<Store> searchStores(String keyword, Pageable pageable);

    Page<Store> getStoresByName(String name, Pageable pageable);

    List<Store> getActiveStores();

    Store createStore(Long userId, Store store);

    Store updateStore(Long id, Store storeDetails);

    void deleteStore(Long id);

    void toggleStoreStatus(Long id);

    List<Store> getTopStoresByOrderCount(int limit);

    List<Store> getNewStores();

    boolean isUserStoreOwner(Long userId, Long storeId);
}