package com.viadee.sonarQuest.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viadee.sonarQuest.constants.AdventureState;
import com.viadee.sonarQuest.constants.QuestState;
import com.viadee.sonarQuest.entities.Adventure;
import com.viadee.sonarQuest.entities.Quest;
import com.viadee.sonarQuest.entities.User;
import com.viadee.sonarQuest.entities.World;
import com.viadee.sonarQuest.repositories.AdventureRepository;
import com.viadee.sonarQuest.repositories.QuestRepository;

@Service
public class AdventureService {

    @Autowired
    private AdventureRepository adventureRepository;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private GratificationService gratificationService;

    @Autowired
    private UserService userService;

    public void updateAdventures() {
        final List<Adventure> adventures = adventureRepository.findAll();
        adventures.forEach(this::updateAdventure);
    }

    @Transactional // Adventure updates are not to be mixed
    public synchronized void updateAdventure(final Adventure adventure) {
        if (adventure != null) {
            final List<Quest> quests = adventure.getQuests();
            final List<Quest> solvedQuests = questRepository.findByAdventureAndStatus(adventure, QuestState.SOLVED);
            if (quests.size() == solvedQuests.size()) {
                gratificationService.rewardUsersForSolvingAdventure(adventure);
                adventure.setStatus(AdventureState.SOLVED);
                adventureRepository.save(adventure);
            }
        }
    }

    /**
     * expects a developer object and the current world and returns the adventures that the developer has already
     * joined.
     *
     * @param world
     * @param user
     * @return allAdventuresForDeveloper
     */
    public List<Adventure> getJoinedAdventuresForUserInWorld(final World world, final User user) {
        final List<User> users = new ArrayList<>();
        users.add(user);

        return adventureRepository.findByUsersAndWorld(users, world);
    }

    /**
     * expects a developer object and the current world and returns the adventures that the developer can still enter.
     *
     * @param world
     * @param user
     * @return freeAdventuresForDeveloperInWorld
     */
    public List<Adventure> getFreeAdventuresForUserInWorld(final World world, final User user) {
        final List<Adventure> freeAdventuresForDeveloperInWorld = adventureRepository.findByWorld(world);
        freeAdventuresForDeveloperInWorld.removeAll(getJoinedAdventuresForUserInWorld(world, user));

        return freeAdventuresForDeveloperInWorld;
    }

    /**
     * Removes the developer from adventure
     * 
     * @param adventureId
     * @param developerId
     * @return adventure
     */
    public Adventure removeUserFromAdventure(final long adventureId, final long userId) {
        Optional<Adventure> adventure = adventureRepository.findById(adventureId);
        final User user = userService.findById(userId);
        if (adventure.isPresent() && user != null) {
            Adventure realAdventure = adventure.get();
            final List<User> developerList = realAdventure.getUsers();
            if (developerList.contains(user)) {
                developerList.remove(user);
            }
            return adventureRepository.save(realAdventure);
        } else {
            return null;
        }
    }

    /**
     * Add a developer to adventure
     * 
     * @param adventureId
     * @param userId
     * @return adventure
     */
    public Adventure addUserToAdventure(final long adventureId, final long userId) {
        Optional<Adventure> adventure = adventureRepository.findById(adventureId);
        final User user = userService.findById(userId);
        if (adventure.isPresent() && user != null) {
            Adventure realAdventure = adventure.get();
            final List<User> userList = realAdventure.getUsers();
            if (!userList.contains(user)) {
                userList.add(user);
            }
            return adventureRepository.save(realAdventure);
        } else {
            return null;
        }
    }

}
