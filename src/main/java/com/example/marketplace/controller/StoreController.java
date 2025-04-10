package com.example.marketplace.controller;

import com.example.marketplace.dto.store.StoreCreateRequest;
import com.example.marketplace.dto.store.StoreDto;
import com.example.marketplace.dto.store.StoreUpdateRequest;
import com.example.marketplace.model.store.Store;
import com.example.marketplace.security.CurrentUser;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<Page<StoreDto>> getAllStores(Pageable pageable) {
        log.debug("REST request to get all Stores");
        Page<Store> stores = storeService.getAllStores(pageable);
        return ResponseEntity.ok(stores.map(this::convertToDto));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StoreDto>> getActiveStores() {
        log.debug("REST request to get all active Stores");
        List<Store> stores = storeService.getActiveStores();
        return ResponseEntity.ok(stores.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id) {
        log.debug("REST request to get Store : {}", id);
        Store store = storeService.getStoreById(id);
        return ResponseEntity.ok(convertToDto(store));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<StoreDto> getStoreByOwnerId(@PathVariable Long ownerId) {
        log.debug("REST request to get Store by owner ID: {}", ownerId);
        Store store = storeService.getStoreByOwnerId(ownerId);
        return ResponseEntity.ok(convertToDto(store));
    }

    @GetMapping("/my-store")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<StoreDto> getCurrentUserStore() {
        log.debug("REST request to get current user's Store");
        Long currentUserId = authService.getCurrentUser().getId();
        Store store = storeService.getStoreByOwnerId(currentUserId);
        return ResponseEntity.ok(convertToDto(store));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StoreDto>> searchStores(
            @RequestParam String keyword,
            Pageable pageable) {
        log.debug("REST request to search Stores with keyword: {}", keyword);
        Page<Store> stores = storeService.searchStores(keyword, pageable);
        return ResponseEntity.ok(stores.map(this::convertToDto));
    }

    @GetMapping("/by-name")
    public ResponseEntity<Page<StoreDto>> getStoresByName(
            @RequestParam String name,
            Pageable pageable) {
        log.debug("REST request to get Stores by name: {}", name);
        Page<Store> stores = storeService.getStoresByName(name, pageable);
        return ResponseEntity.ok(stores.map(this::convertToDto));
    }

    @GetMapping("/top")
    public ResponseEntity<List<StoreDto>> getTopStores(@RequestParam(defaultValue = "5") int limit) {
        log.debug("REST request to get top {} Stores", limit);
        List<Store> stores = storeService.getTopStoresByOrderCount(limit);
        return ResponseEntity.ok(stores.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/new")
    public ResponseEntity<List<StoreDto>> getNewStores() {
        log.debug("REST request to get new Stores");
        List<Store> stores = storeService.getNewStores();
        return ResponseEntity.ok(stores.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<StoreDto> createStore(@Valid @RequestBody StoreCreateRequest request) {
        log.debug("REST request to create Store : {}", request);

        Store store = new Store();
        store.setName(request.getName());
        store.setDescription(request.getDescription());
        store.setLogo(request.getLogo());
        store.setBanner(request.getBanner());

        Long currentUserId = authService.getCurrentUser().getId();
        Store result = storeService.createStore(currentUserId, store);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<StoreDto> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreUpdateRequest request) {
        log.debug("REST request to update Store : {}, {}", id, request);

        // Verify ownership unless admin
        Long currentUserId = authService.getCurrentUser().getId();
        if (!authService.getCurrentUser().getRole().name().equals("ADMIN") &&
                !storeService.isUserStoreOwner(currentUserId, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Store store = new Store();
        store.setName(request.getName());
        store.setDescription(request.getDescription());
        store.setLogo(request.getLogo());
        store.setBanner(request.getBanner());

        if (authService.getCurrentUser().getRole().name().equals("ADMIN")) {
            // Only admin can change active status
            store.setActive(request.getActive());
        }

        Store result = storeService.updateStore(id, store);
        return ResponseEntity.ok(convertToDto(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        log.debug("REST request to delete Store : {}", id);

        // Verify ownership unless admin
        Long currentUserId = authService.getCurrentUser().getId();
        if (!authService.getCurrentUser().getRole().name().equals("ADMIN") &&
                !storeService.isUserStoreOwner(currentUserId, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> toggleStoreStatus(@PathVariable Long id) {
        log.debug("REST request to toggle status of Store : {}", id);
        storeService.toggleStoreStatus(id);
        Store store = storeService.getStoreById(id);
        return ResponseEntity.ok(Map.of("active", store.getActive()));
    }

    private StoreDto convertToDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setDescription(store.getDescription());
        dto.setLogo(store.getLogo());
        dto.setBanner(store.getBanner());
        dto.setActive(store.getActive());
        dto.setCreatedAt(store.getCreatedAt());

        if (store.getOwner() != null) {
            dto.setOwnerId(store.getOwner().getId());
            dto.setOwnerName(store.getOwner().getFirstName() + " " + store.getOwner().getLastName());
        }

        return dto;
    }
}