package com.dozenflow.be.label;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LabelService {

    private final LabelRepository labelRepository;

    public LabelService(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    public List<Label> findAll() {
        return labelRepository.findAll();
    }

    public Label create(Label label) {
        return labelRepository.save(label);
    }

    @Transactional
    public Label update(Long id, Label updated) {
        Label existing = labelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Label not found with id: " + id));

        existing.setName(updated.getName());
        existing.setColorHex(updated.getColorHex());

        return labelRepository.save(existing);
    }

    public void delete(Long id) {
        if (!labelRepository.existsById(id)) {
            throw new EntityNotFoundException("Label not found with id: " + id);
        }
        labelRepository.deleteById(id);
    }
}
