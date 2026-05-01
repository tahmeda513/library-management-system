package dao;

import java.util.List;

/**
 * GenericDAO – interface defining standard CRUD operations.
 *
 * OOP concepts: Abstraction (interface), Polymorphism (multiple concrete implementations).
 * Using generics (T = entity type, K = primary key type) to maximise reuse.
 *
 * @param <T> the entity type (Book, Member, BorrowRecord)
 * @param <K> the primary key type (Integer)
 */
public interface GenericDAO<T, K> {

    /** Inserts a new entity into the database. */
    boolean create(T entity);

    /** Retrieves an entity by its primary key. Returns null if not found. */
    T findById(K id);

    /** Retrieves all entities of this type. */
    List<T> findAll();

    /** Updates an existing entity. */
    boolean update(T entity);

    /** Deletes an entity by its primary key. */
    boolean delete(K id);
}
