package vn.hoidanit.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.authservice.domain.response.RestResponse;
import vn.hoidanit.authservice.dto.CreateSubscriberDTO;
import vn.hoidanit.authservice.dto.ResSubscriberDTO;
import vn.hoidanit.authservice.dto.UpdateSubscriberSkillsDTO;
import vn.hoidanit.authservice.service.SubscriberService;

@RestController
@RequestMapping("/api/v1/subscribers")
@RequiredArgsConstructor
@Slf4j
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping
    public ResponseEntity<RestResponse<ResSubscriberDTO>> createSubscriber(
            @Valid @RequestBody CreateSubscriberDTO dto) {

        log.info("Creating subscriber: email={}", dto.getEmail());
        ResSubscriberDTO result = subscriberService.createSubscriber(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RestResponse<>(
                        HttpStatus.CREATED.value(),
                        null,
                        "Subscriber created successfully",
                        result
                ));
    }

    @PutMapping("/{id}/skills")
    public ResponseEntity<RestResponse<ResSubscriberDTO>> updateSkills(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriberSkillsDTO dto) {

        log.info("Updating skills for subscriber: id={}", id);
        ResSubscriberDTO result = subscriberService.updateSkills(id, dto);

        return ResponseEntity.ok(new RestResponse<>(
                HttpStatus.OK.value(),
                null,
                "Skills updated successfully",
                result
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<RestResponse<ResSubscriberDTO>> getMySubscription() {
        log.info("Fetching current user's subscription");
        ResSubscriberDTO result = subscriberService.getMySubscription();

        return ResponseEntity.ok(new RestResponse<>(
                HttpStatus.OK.value(),
                null,
                "Subscription fetched successfully",
                result
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResSubscriberDTO>> getSubscriberById(@PathVariable Long id) {
        log.info("Fetching subscriber by id: {}", id);
        ResSubscriberDTO result = subscriberService.getSubscriberById(id);

        return ResponseEntity.ok(new RestResponse<>(
                HttpStatus.OK.value(),
                null,
                "Subscriber fetched successfully",
                result
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteSubscriber(@PathVariable Long id) {
        log.info("Deleting subscriber: id={}", id);
        subscriberService.deleteSubscriber(id);

        return RestResponse.ok(null, "Subscriber deleted successfully");
    }
}

