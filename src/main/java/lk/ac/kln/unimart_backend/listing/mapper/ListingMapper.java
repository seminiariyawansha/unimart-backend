package lk.ac.kln.unimart_backend.listing.mapper;

import lk.ac.kln.unimart_backend.listing.dto.ListingResponse;
import lk.ac.kln.unimart_backend.listing.entity.Listing;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {
    public ListingResponse toResponse(Listing l) {
        return new ListingResponse(
                l.getId(),
                l.getSeller().getId(),
                l.getSeller().getFullName(),
                l.getCategory().getId(),
                l.getCategory().getName(),
                l.getTitle(),
                l.getDescription(),
                l.getPrice(),
                l.getStatus(),
                l.getCreatedAt(),
                l.getUpdatedAt()
        );
    }
}