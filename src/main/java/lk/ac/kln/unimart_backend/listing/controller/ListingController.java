package lk.ac.kln.unimart_backend.listing.controller;

import jakarta.validation.Valid;
import lk.ac.kln.unimart_backend.listing.dto.ListingRequest;
import lk.ac.kln.unimart_backend.listing.dto.ListingResponse;
import lk.ac.kln.unimart_backend.listing.entity.ListingStatus;
import lk.ac.kln.unimart_backend.listing.service.ListingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lk.ac.kln.unimart_backend.review.dto.ReviewResponse;
import lk.ac.kln.unimart_backend.review.service.ReviewService;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingService service;
    private final ReviewService reviewService;

    public ListingController(ListingService service, ReviewService reviewService) {
        this.service = service;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}/reviews")
    public Page<ReviewResponse> listReviews(@PathVariable Long id, Pageable pageable) {
        ListingResponse listing = service.get(id);
        return reviewService.getBySeller(listing.sellerId(), pageable);
    }

    @GetMapping
    public Page<ListingResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ListingStatus status,
            Pageable pageable) {
        return service.search(q, categoryId, status, pageable);
    }

    @GetMapping("/{id}")
    public ListingResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(
            @Valid @RequestBody ListingRequest request,
            Authentication authentication) {
        ListingResponse created = service.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ListingRequest request,
                                  Authentication authentication) {
        return service.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.archive(id, authentication.getName());
    }
}