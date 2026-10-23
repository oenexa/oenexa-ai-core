package com.oenexa.infrastructure.adapter;

import com.oenexa.domain.model.auth.User;
import com.oenexa.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JpaUserStorageAdapter Zero-Mock Unit Tests")
class JpaUserStorageAdapterTest {

    private InMemoryUserRepository repository;
    private JpaUserStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        adapter = new JpaUserStorageAdapter(repository);
    }

    @Test
    @DisplayName("Should save and find user by ID, username, and email")
    void shouldSaveAndFindUser() {
        User user = new User("alice", "hash_pw", "alice@oenexa.com");
        User saved = adapter.save(user);

        assertNotNull(saved);
        assertNotNull(saved.id());

        Optional<User> byId = adapter.findById(saved.id());
        assertTrue(byId.isPresent());
        assertEquals("alice", byId.get().username());

        Optional<User> byUsername = adapter.findByUsername("alice");
        assertTrue(byUsername.isPresent());

        Optional<User> byEmail = adapter.findByEmail("alice@oenexa.com");
        assertTrue(byEmail.isPresent());

        assertTrue(adapter.existsByUsername("alice"));
        assertFalse(adapter.existsByUsername("bob"));

        assertTrue(adapter.existsByEmail("alice@oenexa.com"));
        assertFalse(adapter.existsByEmail("bob@oenexa.com"));
    }

    @Test
    @DisplayName("Should reject null repository in constructor")
    void shouldRejectNullRepository() {
        assertThrows(NullPointerException.class, () -> new JpaUserStorageAdapter(null));
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> store = new ConcurrentHashMap<>();
        private long idGen = 1;

        @Override
        public <S extends User> S save(S entity) {
            if (entity.id() == null) {
                entity.withId(idGen++);
            }
            store.put(entity.id(), entity);
            return entity;
        }

        @Override
        public Optional<User> findById(Long aLong) {
            return Optional.ofNullable(store.get(aLong));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return store.values().stream().filter(u -> u.username().equals(username)).findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(u -> u.email().equals(email)).findFirst();
        }

        @Override
        public boolean existsByUsername(String username) {
            return store.values().stream().anyMatch(u -> u.username().equals(username));
        }

        @Override
        public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(u -> u.email().equals(email));
        }

        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<User> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<User> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(User entity) { store.remove(entity.id()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<User> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long aLong) { return store.get(aLong); }
        @Override public User getById(Long aLong) { return store.get(aLong); }
        @Override public User getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends User> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends User> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends User> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends User> long count(Example<S> example) { return 0; }
        @Override public <S extends User> boolean exists(Example<S> example) { return false; }
        @Override public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public List<User> findAll(Sort sort) { return List.of(); }
        @Override public Page<User> findAll(Pageable pageable) { return Page.empty(); }
    }
}

