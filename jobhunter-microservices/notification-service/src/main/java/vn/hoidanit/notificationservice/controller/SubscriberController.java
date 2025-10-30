package vn.hoidanit.notificationservice.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import vn.hoidanit.notificationservice.annotation.RequireRole;
import vn.hoidanit.notificationservice.domain.Subscriber;
import vn.hoidanit.notificationservice.domain.response.RestResponse;
import vn.hoidanit.notificationservice.dto.ReqCreateSubscriberDTO;
import vn.hoidanit.notificationservice.dto.ResSubscriberSkillsDTO;
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
    public ResponseEntity<RestResponse<Subscriber>> create(@Valid @RequestBody ReqCreateSubscriberDTO reqDto) {
        // Check email exists
        if (this.subscriberService.isExistsByEmail(reqDto.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        // Convert DTO to Entity
        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(reqDto.getEmail());
        subscriber.setName(reqDto.getName());

        // Extract skill IDs from array of objects
        List<Long> skillIds = reqDto.getSkills().stream()
                .map(ReqCreateSubscriberDTO.SkillRef::getId)
                .collect(Collectors.toList());
        subscriber.setSkillIds(skillIds);

        Subscriber createdSubscriber = this.subscriberService.create(subscriber);
        return RestResponse.created(createdSubscriber, "Create subscriber successfully");
    }

    @PutMapping("/subscribers")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Subscriber>> update(@RequestBody Subscriber subscriber) {
        // Check exists
        Subscriber currentSubscriber = this.subscriberService.findById(subscriber.getId());
        if (currentSubscriber == null) {
            return ResponseEntity.notFound().build();
        }

        // Data-level authorization: USER can only update their own subscription
        String currentUserEmail = SecurityUtil.getCurrentUserLogin().orElse("");
        String currentUserRoles = SecurityUtil.getCurrentUserRoles().orElse("");

        boolean isAdmin = currentUserRoles != null && currentUserRoles.contains("ROLE_ADMIN");
        boolean isOwner = currentUserEmail != null && currentUserEmail.equals(currentSubscriber.getEmail());

        if (!isAdmin && !isOwner) {
            log.warn("User {} attempted to update subscriber {} without permission", currentUserEmail, currentSubscriber.getEmail());
            return RestResponse.error(org.springframework.http.HttpStatus.FORBIDDEN,
                "You don't have permission to update this subscription");
        }

        Subscriber updatedSubscriber = this.subscriberService.update(currentSubscriber, subscriber);
        return RestResponse.ok(updatedSubscriber, "Update subscriber successfully");
    }

    @GetMapping("/subscribers/skills")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<ResSubscriberSkillsDTO>> getSubscriberSkills() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("Fetching subscribed skills for user: {}", email);

        ResSubscriberSkillsDTO result = this.subscriberService.getSubscriberSkillsDetails(email);

        if (result == null || result.getSkills().isEmpty()) {
            log.warn("No subscribed skills found for user: {}", email);
            return RestResponse.ok(
                new ResSubscriberSkillsDTO(List.of()),
                "No subscribed skills found"
            );
        }

        log.info("Found {} subscribed skills for user: {}", result.getSkills().size(), email);
        return RestResponse.ok(result, "Fetch subscriber skills successfully");
    }

    @GetMapping("/subscribers/{id}")
    @RequireRole({"ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Subscriber>> fetchById(@PathVariable("id") long id) {
        Subscriber subscriber = this.subscriberService.findById(id);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        return RestResponse.ok(subscriber, "Fetch subscriber by id successfully");
    }

    @DeleteMapping("/subscribers/{id}")
    @RequireRole({"ROLE_USER", "ROLE_HR", "ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        Subscriber subscriber = this.subscriberService.findById(id);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        // Data-level authorization: USER can only delete their own subscription
        String currentUserEmail = SecurityUtil.getCurrentUserLogin().orElse("");
        String currentUserRoles = SecurityUtil.getCurrentUserRoles().orElse("");

        boolean isAdmin = currentUserRoles != null && currentUserRoles.contains("ROLE_ADMIN");
        boolean isOwner = currentUserEmail != null && currentUserEmail.equals(subscriber.getEmail());

        if (!isAdmin && !isOwner) {
            log.warn("User {} attempted to delete subscriber {} without permission", currentUserEmail, subscriber.getEmail());
            return RestResponse.error(org.springframework.http.HttpStatus.FORBIDDEN,
                "You don't have permission to delete this subscription");
        }

        this.subscriberService.delete(id);
        return RestResponse.ok(null, "Delete subscriber successfully");
    }

    @GetMapping("/subscribers/by-email")
    @RequireRole({"ROLE_ADMIN"})
    public ResponseEntity<RestResponse<Subscriber>> fetchByEmail(String email) {
        Subscriber subscriber = this.subscriberService.findByEmail(email);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        return RestResponse.ok(subscriber, "Fetch subscriber by email successfully");
    }
}


