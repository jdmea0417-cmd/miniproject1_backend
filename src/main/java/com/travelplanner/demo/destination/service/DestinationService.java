package com.travelplanner.demo.destination.service;

import com.travelplanner.demo.destination.dto.DestinationRequest;
import com.travelplanner.demo.destination.dto.DestinationResponse;
import com.travelplanner.demo.destination.entity.DestinationEntity;
import com.travelplanner.demo.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationResponse create(DestinationRequest request) {
        // Assuming no travel plan association for standalone destination
        DestinationEntity entity = DestinationEntity.builder()
                .date(request.getDate())
                .time(request.getTime())
                .place(request.getKeywords() != null && !request.getKeywords().isEmpty()
                        ? String.join(", ", request.getKeywords())
                        : "")
                .build();
        DestinationEntity saved = destinationRepository.save(entity);
        return DestinationResponse.fromEntity(saved);
    }

    public DestinationResponse update(Integer id, DestinationRequest request) {
        System.out.println(">>>> debug destination service update");
        System.out.println(">>>> debug param : "+request);
        DestinationEntity entity = destinationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Destination not found with id: " + id));
        entity.setDate(request.getDate());
        entity.setTime(request.getTime());
        // place 필드 우선, 없으면 keywords 조합
        String placeValue = (request.getPlace() != null && !request.getPlace().isBlank())
                ? request.getPlace()
                : (request.getKeywords() != null && !request.getKeywords().isEmpty()
                        ? String.join(", ", request.getKeywords())
                        : "");
        entity.setPlace(placeValue);
        DestinationEntity updated = destinationRepository.save(entity);
        return DestinationResponse.fromEntity(updated);
    }

    public void delete(Integer id) {
        if (!destinationRepository.existsById(id)) {
            throw new NoSuchElementException("Destination not found with id: " + id);
        }
        destinationRepository.deleteById(id);
    }
}