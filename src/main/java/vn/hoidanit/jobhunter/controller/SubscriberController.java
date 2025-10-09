package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.Subscriber;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriberController {
    private final SubscriberService subscriberService;

    @PostMapping("/subscribers")
    @ApiMessage("Create a subscriber")
    // Public - anyone can subscribe to newsletter
    public ResponseEntity<Subscriber> create(@Valid @RequestBody Subscriber subscriber) throws IdInvalidException{
        // check email
        boolean isExist = this.subscriberService.isExistsByEmail(subscriber.getEmail());
        if (isExist == true) {
            throw new IdInvalidException("Email " + subscriber.getEmail() + " đã tồn tại");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.subscriberService.create(subscriber));
    }

    @PutMapping("/subscribers")
    @ApiMessage("Update a subscriber")
    @Secured({"SUPER_ADMIN", "ROLE_ADMIN"})  // Only admin can update subscribers
    public ResponseEntity<Subscriber> update(@RequestBody Subscriber subscriber) throws IdInvalidException {
        // check id
        Subscriber subscriberDB = this.subscriberService.findById(subscriber.getId());

        if (subscriberDB == null) {
            throw new IdInvalidException("Id " + subscriber.getId() + " không tồn tại");
        }

        return ResponseEntity.ok().body(this.subscriberService.update(subscriberDB, subscriber));
    }

    @PostMapping("/subscribers/skills")
    @ApiMessage("Get list subscriber by skills")
    // Requires authentication - user must be logged in
    public ResponseEntity<Subscriber> getSubscribersSkill() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        return ResponseEntity.ok().body(this.subscriberService.findByEmail(email));
    }
}
