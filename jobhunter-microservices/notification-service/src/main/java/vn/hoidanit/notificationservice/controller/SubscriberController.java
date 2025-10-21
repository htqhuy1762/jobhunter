package vn.hoidanit.notificationservice.controller;

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
import vn.hoidanit.notificationservice.domain.Subscriber;
import vn.hoidanit.notificationservice.service.SubscriberService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriberController {
    private final SubscriberService subscriberService;

    @PostMapping("/subscribers")
    public ResponseEntity<Subscriber> create(@Valid @RequestBody Subscriber subscriber) {
        // Check email exists
        if (this.subscriberService.isExistsByEmail(subscriber.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.subscriberService.create(subscriber));
    }

    @PutMapping("/subscribers")
    public ResponseEntity<Subscriber> update(@RequestBody Subscriber subscriber) {
        // Check exists
        Subscriber currentSubscriber = this.subscriberService.findById(subscriber.getId());
        if (currentSubscriber == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(this.subscriberService.update(currentSubscriber, subscriber));
    }

    @GetMapping("/subscribers/{id}")
    public ResponseEntity<Subscriber> fetchById(@PathVariable("id") long id) {
        Subscriber subscriber = this.subscriberService.findById(id);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(subscriber);
    }

    @DeleteMapping("/subscribers/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        Subscriber subscriber = this.subscriberService.findById(id);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }

        this.subscriberService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscribers/by-email")
    public ResponseEntity<Subscriber> fetchByEmail(String email) {
        Subscriber subscriber = this.subscriberService.findByEmail(email);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(subscriber);
    }
}


