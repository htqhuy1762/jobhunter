package vn.hoidanit.notificationservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.notificationservice.domain.Subscriber;
import vn.hoidanit.notificationservice.repository.SubscriberRepository;

@Service
@RequiredArgsConstructor
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;

    public boolean isExistsByEmail(String email) {
        return this.subscriberRepository.existsByEmail(email);
    }

    public Subscriber findById(Long id) {
        return this.subscriberRepository.findById(id).orElse(null);
    }

    public Subscriber create(Subscriber subscriber) {
        // In microservices, skillIds are already set
        // No need to fetch Skill entities from database
        return this.subscriberRepository.save(subscriber);
    }

    public Subscriber update(Subscriber subscriberDB, Subscriber subscriber) {
        // Update skillIds
        if (subscriber.getSkillIds() != null) {
            subscriberDB.setSkillIds(subscriber.getSkillIds());
        }
        subscriberDB.setName(subscriber.getName());
        return this.subscriberRepository.save(subscriberDB);
    }

    public Subscriber findByEmail(String email) {
        return this.subscriberRepository.findByEmail(email);
    }

    public void delete(Long id) {
        this.subscriberRepository.deleteById(id);
    }

    public List<Subscriber> findAll() {
        return this.subscriberRepository.findAll();
    }
}

