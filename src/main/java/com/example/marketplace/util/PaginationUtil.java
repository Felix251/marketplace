package com.example.marketplace.util;

import com.example.marketplace.dto.product.ProductSearchCriteria;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PaginationUtil {

    public static Pageable getPageable(ProductSearchCriteria criteria) {
        // Valeurs par défaut
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 10;

        // Si des champs de tri sont spécifiés
        if (criteria.getSortFields() != null && !criteria.getSortFields().isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>();

            // Construire les ordres de tri
            for (int i = 0; i < criteria.getSortFields().size(); i++) {
                String direction = "asc";
                if (criteria.getSortDirections() != null &&
                        criteria.getSortDirections().size() > i) {
                    direction = criteria.getSortDirections().get(i);
                }

                if ("desc".equalsIgnoreCase(direction)) {
                    orders.add(Sort.Order.desc(criteria.getSortFields().get(i)));
                } else {
                    orders.add(Sort.Order.asc(criteria.getSortFields().get(i)));
                }
            }

            return PageRequest.of(page, size, Sort.by(orders));
        }

        // Sans tri spécifique, utiliser l'ordre par défaut (ID décroissant - plus récent d'abord)
        return PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));
    }

    public static Pageable getDefaultPageable() {
        return PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")));
    }
}