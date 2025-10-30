package vn.hoidanit.notificationservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.notificationservice.client.JobServiceClient;
import vn.hoidanit.notificationservice.domain.Subscriber;
import vn.hoidanit.notificationservice.dto.ResSubscriberSkillsDTO;
import vn.hoidanit.notificationservice.dto.ResSubscriberSkillsDTO.SkillInfo;
import vn.hoidanit.notificationservice.repository.SubscriberRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final JobServiceClient jobServiceClient;

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

    public Subscriber findByEmailWithSkills(String email) {
        return this.subscriberRepository.findByEmailWithSkills(email);
    }

    public ResSubscriberSkillsDTO getSubscriberSkillsDetails(String email) {
        Subscriber subscriber = this.subscriberRepository.findByEmailWithSkills(email);

        if (subscriber == null || subscriber.getSkillIds() == null || subscriber.getSkillIds().isEmpty()) {
            return new ResSubscriberSkillsDTO(new ArrayList<>());
        }

        List<SkillInfo> skills = new ArrayList<>();
        for (Long skillId : subscriber.getSkillIds()) {
            try {
                JobServiceClient.SkillResponse response = jobServiceClient.getSkillById(skillId);
                if (response != null && response.getData() != null) {
                    JobServiceClient.SkillData skillData = response.getData();
                    SkillInfo skillInfo = new SkillInfo(skillData.getId(), skillData.getName());
                    skills.add(skillInfo);
                }
            } catch (Exception e) {
                log.error("Error fetching skill with id {}: {}", skillId, e.getMessage());
                // Continue with other skills even if one fails
            }
        }

        return new ResSubscriberSkillsDTO(skills);
    }

    public void delete(Long id) {
        this.subscriberRepository.deleteById(id);
    }

    public List<Subscriber> findAll() {
        return this.subscriberRepository.findAll();
    }
}

