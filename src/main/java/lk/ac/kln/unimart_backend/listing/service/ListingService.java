package lk.ac.kln.unimart_backend.listing.service;

import lk.ac.kln.unimart_backend.category.entity.Category;
import lk.ac.kln.unimart_backend.category.repository.CategoryRepository;
import lk.ac.kln.unimart_backend.common.exception.ConflictException;
import lk.ac.kln.unimart_backend.common.exception.ForbiddenException;
import lk.ac.kln.unimart_backend.common.exception.ResourceNotFoundException;
import lk.ac.kln.unimart_backend.listing.dto.ListingRequest;
import lk.ac.kln.unimart_backend.listing.dto.ListingResponse;
import lk.ac.kln.unimart_backend.listing.entity.Listing;
import lk.ac.kln.unimart_backend.listing.entity.ListingStatus;
import lk.ac.kln.unimart_backend.listing.mapper.ListingMapper;
import lk.ac.kln.unimart_backend.listing.repository.ListingRepository;
import lk.ac.kln.unimart_backend.user.entity.User;
import lk.ac.kln.unimart_backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ListingService {

    private final ListingRepository listings;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final ListingMapper mapper;

    public ListingService(ListingRepository listings, CategoryRepository categories,
                          UserRepository users, ListingMapper mapper) {
        this.listings = listings;
        this.categories = categories;
        this.users = users;
        this.mapper = mapper;
    }

    @Transactional
    public ListingResponse create(ListingRequest request, String email) {
        User seller = users.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = categories.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle(request.title().trim());
        listing.setDescription(request.description().trim());
        listing.setPrice(request.price());
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());
        return mapper.toResponse(listings.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingResponse get(Long id) {
        Listing listing = listings.findByIdAndStatusNot(id, ListingStatus.ARCHIVED)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        return mapper.toResponse(listing);
    }

    @Transactional
    public ListingResponse update(Long id, ListingRequest request, String email) {
        Listing listing = requireOwnedListing(id, email);
        Category category = categories.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        listing.setTitle(request.title().trim());
        listing.setDescription(request.description().trim());
        listing.setPrice(request.price());
        listing.setCategory(category);
        listing.setUpdatedAt(Instant.now());
        return mapper.toResponse(listing);
    }

    @Transactional
    public void archive(Long id, String email) {
        Listing listing = requireOwnedListing(id, email);
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new ConflictException("Sold listings cannot be deleted");
        }
        listing.setStatus(ListingStatus.ARCHIVED);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> search(String q, Long categoryId, ListingStatus status, Pageable pageable) {
        int pageSize = Math.min(pageable.getPageSize(), 50);
        Pageable capped = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());
        Specification<Listing> spec = Specification.where(null);
        if (q != null && !q.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), ListingStatus.ARCHIVED));
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return listings.findAll(spec, capped).map(mapper::toResponse);
    }

    private Listing requireOwnedListing(Long id, String email) {
        Listing listing = listings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (!listing.getSeller().getUniversityEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You do not own this listing");
        }
        return listing;
    }
}