package com.viadee.sonarQuest.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viadee.sonarQuest.entities.Artefact;
import com.viadee.sonarQuest.entities.Level;
import com.viadee.sonarQuest.entities.User;
import com.viadee.sonarQuest.repositories.ArtefactRepository;

@Service
public class ArtefactService {

    @Autowired
    private ArtefactRepository artefactRepository;

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserService userService;

    public List<Artefact> getArtefacts() {
        return artefactRepository.findAll();
    }

    public List<Artefact> getArtefactsforMarkteplace() {
        return artefactRepository.findByQuantityIsGreaterThanEqual((long) 1);
    }

    public Artefact getArtefact(final long id) {
        return artefactRepository.findById(id).orElse(null);
    }

    public Artefact createArtefact(final Artefact artefact) {
        return artefactRepository.save(artefact);
    }

    public Artefact updateArtefact(final Long id, final Artefact artefactDto) {
        final Optional<Artefact> artefact = artefactRepository.findById(id);
        if (artefact.isPresent()) {
            Artefact realArtefact = artefact.get();
            realArtefact.setName(artefactDto.getName());
            realArtefact.setIcon(artefactDto.getIcon());
            realArtefact.setPrice(artefactDto.getPrice());
            realArtefact.setDescription(artefactDto.getDescription());
            realArtefact.setQuantity(artefactDto.getQuantity());
            realArtefact.setMinLevel(levelService.findById(artefactDto.getMinLevel().getId()));
            realArtefact.setSkills(artefactDto.getSkills());
            return artefactRepository.save(realArtefact);
        }
        return null;
    }

    public Artefact buyArtefact(Artefact artefact, final User user) {

        // If developer has TOO LITTLE GOLD, Then the purchase is canceled
        final long gold = user.getGold() - artefact.getPrice();
        if (gold < 0) {
            return null;
        }

        // If the developer has ALREADY BOUGHT the Artefact, Then the purchase is
        // canceled
        for (final Artefact a : user.getArtefacts()) {
            if (a.equals(artefact)) {
                return null;
            }
        }

        // If the artefact is SOLD OUT, then the purchase is canceled
        if (artefact.getQuantity() < 1) {
            return null;
        }

        // When the LEVEL of the developer is too low, then the purchase is canceled
        final Level minLevel = artefact.getMinLevel();
        final Level devLevel = user.getLevel();

        if (minLevel.getLevel() > devLevel.getLevel()) {
            return null;
        }

        user.getArtefacts().add(artefact);
        user.setGold(gold);
        userService.save(user);

        artefact.setQuantity(artefact.getQuantity() - 1);
        artefact = artefactRepository.save(artefact);
        return artefact;
    }

}
