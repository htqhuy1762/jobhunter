package vn.hoidanit.notificationservice.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.notificationservice.annotation.RateLimit;
import vn.hoidanit.notificationservice.domain.Subscriber;
import vn.hoidanit.notificationservice.domain.response.RestResponse;
import vn.hoidanit.notificationservice.service.SubscriberService;
import vn.hoidanit.notificationservice.util.SecurityUtil;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class SubscriberController {
    private final SubscriberService subscriberService;

    @RateLimit(name = "createSubscriber")
    @PostMapping("/subscribers")
    public ResponseEntity<RestResponse<Subscriber>> create(@Valid @RequestBody Subscriber subscriber) {
        // Check email exists
        if (this.subscriberService.isExistsByEmail(subscriber.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        Subscriber createdSubscriber = this.subscriberService.create(subscriber);
        return RestResponse.created(createdSubscriber, "Create subscriber successfully");
    }

    @PutMapping("/subscribers")
    public ResponseEntity<RestResponse<Subscriber>> update(@RequestBody Subscriber subscriber) {
        // Check exists
        Subscriber currentSubscriber = this.subscriberService.findById(subscriber.getId());
        if (currentSubscriber == null) {
            return ResponseEntity.notFound().build();
        }

        Subscriber updatedSubscriber = this.subscriberService.update(currentSubscriber, subscriber);
        return RestResponse.ok(updatedSubscriber, "Update subscriber successfully");
    }

    @GetMapping("/subscribers/skills")
    public ResponseEntity<RestResponse<List<Long>>> getSubscriberSkills() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("Fetching subscribed skills for user: {}", email);

        Subscriber subscriber = this.subscriberService.findByEmail(email);

        if (subscriber == null) {
            log.warn("No subscriber found for user: {}", email);
            return RestResponse.ok(List.of(), "No subscriber found for current user");
        }

        List<Long> skillIds = subscriber.getSkillIds() != null ? subscriber.getSkillIds() : List.of();
        log.info("Found {} subscribed skills for user: {}", skillIds.size(), email);
        return RestResponse.ok(skillIds, "Fetch subscriber skills successfully");
    }

    @GetMapping("/subscribers/{id}")
    public ResponseEntity<RestResponse<Subscriber>> fetchById(@PathVariable("id") long id) {
        Subscriber subscriber = this.subscriberService.findById(id);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        return RestResponse.ok(subscriber, "Fetch subscriber by id successfully");
    }

    @DeleteMapping("/subscribers/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        Subscriber subscriber = this.subscriberService.findById(id);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        this.subscriberService.delete(id);
        return RestResponse.ok(null, "Delete subscriber successfully");
    }

    @GetMapping("/subscribers/by-email")
    public ResponseEntity<RestResponse<Subscriber>> fetchByEmail(String email) {
        Subscriber subscriber = this.subscriberService.findByEmail(email);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        return RestResponse.ok(subscriber, "Fetch subscriber by email successfully");
    }
}


