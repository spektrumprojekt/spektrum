/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.spektrumprojekt.persistence.jpa.impl;

import static de.spektrumprojekt.datamodel.user.UserModel.DEFAULT_USER_MODEL_TYPE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Persistence.MatchMode;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserPersistenceTest {

    /**
     * The name of the persistence unit for testing purposes, as configured in
     * META-INF/persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private Persistence persistence;
    private UserPersistence userPersistence;

    private final static String userGlobalId1 = "testUserId1";

    private final static String userGlobalId2 = "testUserId2";

    public void addTermsToUserModel(UserModel userModel, float score, Term... terms) {
        Collection<UserModelEntry> entries;

        entries = new HashSet<UserModelEntry>();
        for (Term term : terms) {
            entries.add(new UserModelEntry(userModel, new ScoredTerm(term, score)));
        }

        persistence.storeOrUpdateUserModelEntries(userModel, entries);
    }

    public void checkTerms(Map<Term, UserModelEntry> modelEntries, int size, float weight) {
        // 4 terms
        Assert.assertEquals(size, modelEntries.size());
        for (Entry<Term, UserModelEntry> entry : modelEntries.entrySet()) {
            Assert.assertEquals(weight, entry.getValue().getScoredTerm().getWeight(), 0.001);
        }
    }

    private void checkUser(User user, Long id, String userGlobalId) {
        Assert.assertNotNull("user cannot be null", user);
        Assert.assertNotNull("id of user cannot be null", user.getId());
        if (userGlobalId != null) {
            Assert.assertEquals("userGlobalId does not match", userGlobalId, user.getGlobalId());
        }

        if (id != null) {
            Assert.assertEquals("id of user does not match!", user.getId(),
                    id);
        }
    }

    private void checkUserModel(UserModel userModel, String userModelGlobalId, String userGlobalId) {

        Assert.assertNotNull("userModel cannot be null.", userModel);
        Assert.assertNotNull("userModel.id cannot be null.", userModel.getId());
        Assert.assertNotNull("userModel.globalId cannot be null.", userModel.getGlobalId());
        Assert.assertNotNull("user of userModel cannot be null.", userModel.getUser());
        if (userModelGlobalId != null) {
            Assert.assertEquals("user model global ids must be equal.", userModel.getGlobalId(),
                    userModelGlobalId);
        }
        if (userGlobalId != null) {
            Assert.assertEquals("user global ids must be equal.",
                    userModel.getUser().getGlobalId(), userGlobalId);
        }
    }

    private UserModel getNewUserModel() {
        String userGlobalId = UUID.randomUUID().toString();
        UserModel userModel = persistence.getOrCreateUserModelByUser(userGlobalId,
                DEFAULT_USER_MODEL_TYPE);
        checkUserModel(userModel, null, userGlobalId);
        return userModel;
    }

    @Before
    public void setUp() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);
        properties.put("eclipselink.logging.level.sql", "FINE");
        properties.put("eclipselink.logging.parameters", "true");
        properties.put("eclipselink.logging.file", "test-output.log");

        Configuration configuration = new SimpleProperties(properties);

        JPAPersistence jpaPersistence = new JPAPersistence(configuration);
        jpaPersistence.initialize();

        userPersistence = new UserPersistence(new JPAConfiguration(configuration));
        persistence = jpaPersistence;
    }

    @Test
    public void testDifferentUserModelTypes() {
        final String userModelType = "TEST_USER_MODEL_TYPE_";
        final String userGlobalId = UUID.randomUUID().toString();
        final String refUserGlobalId = UUID.randomUUID().toString();

        Term term1 = persistence.getOrCreateTerm(TermCategory.TERM, "term_1_"
                + UUID.randomUUID().toString());
        Term term2 = persistence.getOrCreateTerm(TermCategory.TERM, "term_2_"
                + UUID.randomUUID().toString());
        Term term3 = persistence.getOrCreateTerm(TermCategory.TERM, "term_3_"
                + UUID.randomUUID().toString());
        Term term4 = persistence.getOrCreateTerm(TermCategory.TERM, "term_4_"
                + UUID.randomUUID().toString());

        // just add some other values (with a score that is distinct
        UserModel refUserModel = persistence.getOrCreateUserModelByUser(refUserGlobalId,
                DEFAULT_USER_MODEL_TYPE);
        checkUserModel(refUserModel, null, refUserGlobalId);
        addTermsToUserModel(refUserModel, 0.55f, term1, term2, term3, term4);

        for (int i = 0; i < 10; i++) {
            UserModel userModel = persistence.getOrCreateUserModelByUser(userGlobalId,
                    userModelType + i);
            refUserModel = persistence.getOrCreateUserModelByUser(refUserGlobalId,
                    userModelType + i);
            checkUserModel(userModel, null, userGlobalId);
            checkUserModel(refUserModel, null, refUserGlobalId);

            addTermsToUserModel(userModel, i / 10f, term1, term2, term3, term4);

            addTermsToUserModel(refUserModel, 0.55f, term1, term2, term3, term4);

        }

        Collection<Term> terms = Arrays.asList(term1, term2, term3, term4);

        // the reference user
        Map<Term, UserModelEntry> modelEntries = persistence.getUserModelEntriesForTerms(
                persistence.getOrCreateUserModelByUser(refUserGlobalId,
                        DEFAULT_USER_MODEL_TYPE), terms);

        checkTerms(modelEntries, 4, 0.55f);

        for (int i = 0; i < 10; i++) {
            modelEntries = persistence.getUserModelEntriesForTerms(
                    persistence.getOrCreateUserModelByUser(userGlobalId,
                            userModelType + i), terms);

            // 4 terms
            checkTerms(modelEntries, 4, i / 10f);

            // the same for the reference user
            modelEntries = persistence.getUserModelEntriesForTerms(
                    persistence.getOrCreateUserModelByUser(refUserGlobalId,
                            userModelType + i), terms);

            checkTerms(modelEntries, 4, 0.55f);

        }

    }

    @Test
    public void testGetAllusers() {

        final int numberOfUsersToCreate = 25;

        // remember the users created before
        Collection<User> usersBefore = persistence.getAllUsers();

        Collection<String> ids = new HashSet<String>();

        // create some users
        for (int i = 0; i < numberOfUsersToCreate; i++) {
            String userGlobalId = UUID.randomUUID().toString();
            User user = persistence.getOrCreateUser(userGlobalId);
            checkUser(user, null, userGlobalId);
            Assert.assertTrue(ids.add(userGlobalId));
        }

        Collection<User> usersAfter = persistence.getAllUsers();

        Assert.assertEquals(numberOfUsersToCreate + usersBefore.size(), usersAfter.size());

        // check if the users are correct, and remove it from the lists
        for (User user : usersAfter) {
            if (!ids.remove(user.getGlobalId())) {
                // users must have been created before, so remove it from this list
                Assert.assertTrue(usersBefore.contains(user));
                usersBefore.remove(user);
            }
        }

        // finally both collections must be empty
        Assert.assertTrue(usersBefore.isEmpty());
        Assert.assertTrue(ids.isEmpty());

    }

    @Test
    public void testGetOrCreateUser() {

        // create the user
        User user1 = persistence.getOrCreateUser(userGlobalId1);
        checkUser(user1, null, userGlobalId1);

        User user1ById = userPersistence.getEntityById(User.class, user1.getId());
        checkUser(user1ById, user1.getId(), userGlobalId1);

        // get the user
        User user1LoadedAgain = persistence.getOrCreateUser(userGlobalId1);
        checkUser(user1LoadedAgain, user1.getId(), userGlobalId1);

        // create another user
        User user2 = persistence.getOrCreateUser(userGlobalId2);
        checkUser(user2, null, userGlobalId2);

        Assert.assertFalse("ids of user for different global id must be unequal!", user1.getId()
                .equals(user2.getId()));

    }

    /**
     * 
     * @param userGlobalId
     *            the users id
     * @return the user model
     */
    @Test
    public void testGetOrCreateUserModelByUser() {

        String newUserGlobalId = UUID.randomUUID().toString();
        User newUser = persistence.getOrCreateUser(newUserGlobalId);
        checkUser(newUser, null, newUserGlobalId);

        // test one create user model for existing user
        UserModel userModel1 = persistence.getOrCreateUserModelByUser(newUserGlobalId,
                DEFAULT_USER_MODEL_TYPE);
        checkUserModel(userModel1, null, newUserGlobalId);

        // test one.one get user model again
        UserModel userModel1LoadedAgain = persistence.getOrCreateUserModelByUser(newUserGlobalId,
                DEFAULT_USER_MODEL_TYPE);
        checkUserModel(userModel1LoadedAgain, userModel1.getGlobalId(), newUserGlobalId);

        // test create user model for new user
        String unknownUserGlobalId = UUID.randomUUID().toString();

        UserModel userModel2 = persistence.getOrCreateUserModelByUser(unknownUserGlobalId,
                DEFAULT_USER_MODEL_TYPE);
        checkUserModel(userModel2, null, null);
        checkUser(userModel2.getUser(), userModel2.getUser().getId(), unknownUserGlobalId);

        // test the unknown user
        User unknownUser = persistence.getOrCreateUser(unknownUserGlobalId);
        checkUser(unknownUser, userModel2.getUser().getId(), unknownUserGlobalId);

        // test the 2nd user model again
        UserModel userModel2LoadedAgain = persistence
                .getOrCreateUserModelByUser(unknownUserGlobalId, DEFAULT_USER_MODEL_TYPE);
        checkUserModel(userModel2LoadedAgain, userModel2.getGlobalId(), unknownUserGlobalId);
        checkUser(userModel2LoadedAgain.getUser(), unknownUser.getId(), unknownUserGlobalId);

    }

    /**
     * Store(create) or update the changed user model
     * 
     * @param userModel
     *            the user model
     * @param changedEntries
     *            the changed entries
     */
    @Test
    public void testGetStoreUpdateUserModelEntries() {

        // the user model to use
        UserModel userModel = getNewUserModel();

        Collection<Term> terms = new HashSet<Term>();
        Collection<UserModelEntry> entriesToStore = new HashSet<UserModelEntry>();

        // create a user model with two terms

        Term term1 = persistence.getOrCreateTerm(TermCategory.TERM, "term1");
        Assert.assertTrue(terms.add(term1));
        ScoredTerm scoredTerm1 = new ScoredTerm(term1, 0.1f);
        UserModelEntry entry1 = new UserModelEntry(userModel, scoredTerm1);
        entriesToStore.add(entry1);

        Term term2 = persistence.getOrCreateTerm(TermCategory.TERM, "term2");
        Assert.assertTrue(terms.add(term2));
        ScoredTerm scoredTerm2 = new ScoredTerm(term2, 0.5f);
        UserModelEntry entry2 = new UserModelEntry(userModel, scoredTerm2);
        entriesToStore.add(entry2);

        // store the entries
        Collection<UserModelEntry> stored = persistence.storeOrUpdateUserModelEntries(userModel,
                entriesToStore);

        // check the store result
        Assert.assertEquals(entriesToStore.size(), stored.size());
        for (UserModelEntry entry : stored) {
            Assert.assertNotNull(entry.getId());
            Assert.assertNotNull(entry.getScoredTerm());
            Assert.assertNotNull(entry.getScoredTerm().getId());
            Assert.assertNotNull(entry.getScoredTerm().getTerm());
            Assert.assertNotNull(entry.getScoredTerm().getTerm().getId());

            UserModelEntry entryLoadedAgain = userPersistence.getEntityById(UserModelEntry.class,
                    entry.getId());
            Assert.assertNotNull(entryLoadedAgain.getScoredTerm());
            Assert.assertNotNull(entryLoadedAgain.getScoredTerm().getId());
            Assert.assertNotNull(entryLoadedAgain.getScoredTerm().getTerm());
            Assert.assertNotNull(entryLoadedAgain.getScoredTerm().getTerm().getId());
            Assert.assertEquals(entry.getGlobalId(), entryLoadedAgain.getGlobalId());

            if (entry.getGlobalId().equals(entry1.getGlobalId())) {
                Assert.assertEquals(entry1.getGlobalId(), entry.getGlobalId());
                Assert.assertEquals(term1.getGlobalId(), entry.getScoredTerm().getTerm()
                        .getGlobalId());
                Assert.assertEquals(term1.getId(), entry.getScoredTerm().getTerm().getId());
                Assert.assertEquals(term1.getId(), entryLoadedAgain.getScoredTerm().getTerm()
                        .getId());
            } else if (entry.getGlobalId().equals(entry2.getGlobalId())) {
                Assert.assertEquals(entry2.getGlobalId(), entry.getGlobalId());
                Assert.assertEquals(term2.getGlobalId(), entry.getScoredTerm().getTerm()
                        .getGlobalId());
                Assert.assertEquals(term2.getId(), entry.getScoredTerm().getTerm().getId());
                Assert.assertEquals(term2.getId(), entryLoadedAgain.getScoredTerm().getTerm()
                        .getId());
            } else {
                Assert.fail("Entry does not match any globalId: " + entry);
            }

        }

        // dummy term
        Term term3 = persistence.getOrCreateTerm(TermCategory.TERM, "term3");
        Assert.assertTrue(terms.add(term3));

        // now get the terms. the terms have size of 3 but the user model contains only two of them
        Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(userModel,
                terms);
        // it must be exactly two terms since the user model contains two of the given terms
        Assert.assertEquals("user model entries", 2, entries.size());

        // now check that the result is ok
        UserModelEntry entry1Loaded = entries.get(term1);
        UserModelEntry entry2Loaded = entries.get(term2);

        Assert.assertNotNull("Entry should not be null for term1", entry1Loaded);
        Assert.assertNotNull("Entry should not be null for term2", entry2Loaded);

        Assert.assertEquals("globalIds should be equal", entry1.getGlobalId(),
                entry1Loaded.getGlobalId());
        Assert.assertEquals("globalIds should be equal", entry2.getGlobalId(),
                entry2Loaded.getGlobalId());

        Assert.assertEquals(entry1.getScoredTerm().getGlobalId(), entry1Loaded.getScoredTerm()
                .getGlobalId());
        Assert.assertEquals(entry2.getScoredTerm().getGlobalId(), entry2Loaded.getScoredTerm()
                .getGlobalId());

        Assert.assertEquals("The score values should match!", entry1.getScoredTerm().getWeight(),
                entry1Loaded.getScoredTerm().getWeight(), 0d);
        Assert.assertEquals("The score values should match!", entry2.getScoredTerm().getWeight(),
                entry2Loaded.getScoredTerm().getWeight(), 0d);

        // Update
        // now change the entry and get it again and check if it is updated
        //

        entry2Loaded.setScoreCount(2);
        entry2Loaded.setScoreSum(3);
        entry2Loaded.getScoredTerm().setWeight(4);

        entriesToStore.clear();
        terms.clear();

        terms.add(term2);
        entriesToStore.add(entry2Loaded);

        // store the entry
        persistence.storeOrUpdateUserModelEntries(userModel, entriesToStore);

        // and get it again
        entries = persistence.getUserModelEntriesForTerms(userModel, terms);

        // now it must be one since only one term is in terms
        Assert.assertEquals("must have as many user model entries", 1, entries.size());

        // get it
        UserModelEntry entry2UpdatedLoaded = entries.get(term2);

        // check that the values are updated and match as expected
        Assert.assertNotNull("Entry should not be null for term2", entry2UpdatedLoaded);
        Assert.assertEquals("ids should be equal", entry2Loaded.getId(),
                entry2UpdatedLoaded.getId());
        Assert.assertEquals("globalIds should be equal", entry2Loaded.getGlobalId(),
                entry2UpdatedLoaded.getGlobalId());
        Assert.assertEquals(entry2Loaded.getScoreCount(), entry2UpdatedLoaded.getScoreCount(), 0);
        Assert.assertEquals(entry2Loaded.getScoreSum(), entry2UpdatedLoaded.getScoreSum(), 0);
        Assert.assertEquals(entry2Loaded.getScoredTerm().getId(), entry2UpdatedLoaded
                .getScoredTerm().getId());
        Assert.assertEquals(entry2Loaded.getScoredTerm().getGlobalId(), entry2UpdatedLoaded
                .getScoredTerm().getGlobalId());
        Assert.assertEquals(entry2Loaded.getScoredTerm().getWeight(), entry2UpdatedLoaded
                .getScoredTerm().getWeight(), 0);
        Assert.assertEquals(term2.getId(), entry2UpdatedLoaded.getScoredTerm().getTerm().getId());

    }

    @Test
    public void testGetUsersWithUserModel() {
        UserModel userModel1 = getNewUserModel();
        UserModel userModel2 = getNewUserModel();
        UserModel userModel3 = getNewUserModel();

        Term term1 = persistence.getOrCreateTerm(TermCategory.TERM, "term_1_"
                + UUID.randomUUID().toString());
        Term term2 = persistence.getOrCreateTerm(TermCategory.TERM, "term_2_"
                + UUID.randomUUID().toString());
        Term term3 = persistence.getOrCreateTerm(TermCategory.TERM, "term_3_"
                + UUID.randomUUID().toString());
        Term term4 = persistence.getOrCreateTerm(TermCategory.TERM, "term_4_"
                + UUID.randomUUID().toString());

        Collection<UserModelEntry> entries;

        entries = new HashSet<UserModelEntry>();
        entries.add(new UserModelEntry(userModel1, new ScoredTerm(term1, 0.1f)));
        entries.add(new UserModelEntry(userModel1, new ScoredTerm(term2, 0.2f)));
        entries.add(new UserModelEntry(userModel1, new ScoredTerm(term3, 0.3f)));

        persistence.storeOrUpdateUserModelEntries(userModel2, entries);

        entries = new HashSet<UserModelEntry>();
        entries.add(new UserModelEntry(userModel2, new ScoredTerm(term1, 0.4f)));
        entries.add(new UserModelEntry(userModel2, new ScoredTerm(term2, 0.5f)));
        entries.add(new UserModelEntry(userModel2, new ScoredTerm(term3, 0.6f)));

        persistence.storeOrUpdateUserModelEntries(userModel3, entries);

        entries = new HashSet<UserModelEntry>();
        entries.add(new UserModelEntry(userModel3, new ScoredTerm(term3, 0.7f)));
        entries.add(new UserModelEntry(userModel3, new ScoredTerm(term4, 0.8f)));

        persistence.storeOrUpdateUserModelEntries(userModel3, entries);

        Collection<Term> terms = new HashSet<Term>();
        terms.add(term1);

        Collection<UserModel> userModels;
        userModels = persistence.getUsersWithUserModel(terms, DEFAULT_USER_MODEL_TYPE,
                MatchMode.EXACT);

        Assert.assertEquals(2, userModels.size());
        Assert.assertTrue(userModels.contains(userModel1));
        Assert.assertTrue(userModels.contains(userModel2));

        terms.add(term2);
        userModels = persistence.getUsersWithUserModel(terms, DEFAULT_USER_MODEL_TYPE,
                MatchMode.EXACT);

        Assert.assertEquals(2, userModels.size());
        Assert.assertTrue(userModels.contains(userModel1));
        Assert.assertTrue(userModels.contains(userModel2));

        terms.add(term3);
        userModels = persistence.getUsersWithUserModel(terms, DEFAULT_USER_MODEL_TYPE,
                MatchMode.EXACT);

        Assert.assertEquals(3, userModels.size());
        Assert.assertTrue(userModels.contains(userModel1));
        Assert.assertTrue(userModels.contains(userModel2));
        Assert.assertTrue(userModels.contains(userModel3));

        terms = new HashSet<Term>();
        terms.add(term1);
        terms.add(term4);
        userModels = persistence.getUsersWithUserModel(terms, DEFAULT_USER_MODEL_TYPE,
                MatchMode.EXACT);

        Assert.assertEquals(3, userModels.size());
        Assert.assertTrue(userModels.contains(userModel1));
        Assert.assertTrue(userModels.contains(userModel2));
        Assert.assertTrue(userModels.contains(userModel3));

        terms = new HashSet<Term>();
        terms.add(term4);
        userModels = persistence.getUsersWithUserModel(terms, DEFAULT_USER_MODEL_TYPE,
                MatchMode.EXACT);

        Assert.assertEquals(1, userModels.size());
        Assert.assertTrue(userModels.contains(userModel3));
    }

    @Test
    public void testUserSimilarities() {
        User user1 = persistence.getOrCreateUser(UUID.randomUUID().toString());
        User user2 = persistence.getOrCreateUser(UUID.randomUUID().toString());
        User user3 = persistence.getOrCreateUser(UUID.randomUUID().toString());
        User user4 = persistence.getOrCreateUser(UUID.randomUUID().toString());

        String groupId1 = UUID.randomUUID().toString();
        String groupId2 = UUID.randomUUID().toString();

        UserSimilarity sim1 = new UserSimilarity(user1.getGlobalId(), user2.getGlobalId(),
                groupId1, 0.1);
        UserSimilarity sim2 = new UserSimilarity(user2.getGlobalId(), user1.getGlobalId(),
                groupId1, 0.2);

        UserSimilarity sim3 = new UserSimilarity(user1.getGlobalId(), user3.getGlobalId(),
                groupId1, 0.3);
        UserSimilarity sim4 = new UserSimilarity(user3.getGlobalId(), user1.getGlobalId(),
                groupId1, 0.4);

        UserSimilarity sim5 = new UserSimilarity(user2.getGlobalId(), user3.getGlobalId(),
                groupId1, 0.5);
        UserSimilarity sim6 = new UserSimilarity(user3.getGlobalId(), user2.getGlobalId(),
                groupId1, 0.6);

        Collection<UserSimilarity> sims = new HashSet<UserSimilarity>();
        sims.add(sim1);
        sims.add(sim2);
        sims.add(sim3);
        sims.add(sim4);
        sims.add(sim5);
        sims.add(sim6);

        persistence.deleteAndCreateUserSimilarities(sims);

        Collection<String> usersTo = new HashSet<String>();
        usersTo.add(user2.getGlobalId());
        usersTo.add(user3.getGlobalId());

        sims = persistence.getUserSimilarities(user1.getGlobalId(), usersTo, groupId1, 0);
        double epsilon = 0.000001;
        Assert.assertEquals(2, sims.size());
        for (UserSimilarity sim : sims) {
            Assert.assertEquals(user1.getGlobalId(), sim.getUserGlobalIdFrom());
            Assert.assertEquals(groupId1, sim.getMessageGroupGlobalId());

            if (sim.getUserGlobalIdTo().equals(user2.getGlobalId())) {
                Assert.assertEquals(0.1, sim.getSimilarity(), epsilon);
            } else if (sim.getUserGlobalIdTo().equals(user3.getGlobalId())) {
                Assert.assertEquals(0.3, sim.getSimilarity(), epsilon);
            } else {
                Assert.fail("Invalid userFrom. " + sim);
            }
        }

        sims = persistence.getUserSimilarities(user1.getGlobalId(), usersTo, groupId1, 0.4);
        for (UserSimilarity sim : sims) {
            Assert.assertTrue(sim.getSimilarity() > 0.4);
        }

        UserSimilarity sim7 = new UserSimilarity(user1.getGlobalId(), user4.getGlobalId(),
                groupId1, 0.7);
        UserSimilarity sim8 = new UserSimilarity(user2.getGlobalId(), user4.getGlobalId(),
                groupId1, 0.8);

        UserSimilarity sim9 = new UserSimilarity(user1.getGlobalId(), user2.getGlobalId(),
                groupId2, 0.9);
        UserSimilarity sim10 = new UserSimilarity(user2.getGlobalId(), user1.getGlobalId(),
                groupId2, 1.0);

        UserSimilarity sim11 = new UserSimilarity(user1.getGlobalId(), user4.getGlobalId(),
                groupId2, 0.1);
        UserSimilarity sim12 = new UserSimilarity(user2.getGlobalId(), user4.getGlobalId(),
                groupId2, 0.2);

        sims = new HashSet<UserSimilarity>();
        sims.add(sim1);
        sims.add(sim2);
        sims.add(sim7);
        sims.add(sim8);
        sims.add(sim9);
        sims.add(sim10);
        sims.add(sim11);
        sims.add(sim12);

        persistence.deleteAndCreateUserSimilarities(sims);

        usersTo = new HashSet<String>();
        usersTo.add(user2.getGlobalId());
        usersTo.add(user3.getGlobalId());
        usersTo.add(user4.getGlobalId());

        sims = persistence.getUserSimilarities(user1.getGlobalId(), usersTo, groupId2, 0);
        Assert.assertEquals(2, sims.size());
        for (UserSimilarity sim : sims) {
            Assert.assertEquals(user1.getGlobalId(), sim.getUserGlobalIdFrom());
            Assert.assertEquals(groupId2, sim.getMessageGroupGlobalId());

            if (sim.getUserGlobalIdTo().equals(user2.getGlobalId())) {
                Assert.assertEquals(0.9, sim.getSimilarity(), epsilon);
            } else if (sim.getUserGlobalIdTo().equals(user4.getGlobalId())) {
                Assert.assertEquals(0.1, sim.getSimilarity(), epsilon);
            } else {
                Assert.fail("Invalid userFrom. " + sim);
            }
        }

        sims = persistence.getUserSimilarities(user1.getGlobalId(), usersTo, groupId1, 0);
        Assert.assertEquals(2, sims.size());

    }

}
