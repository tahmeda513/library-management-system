package model;

/**
 * LibraryEntity – abstract base class for all domain entities.
 *
 * OOP concepts: Abstraction (abstract class), Polymorphism (overridden methods).
 * Every entity in the library system must be able to return a formatted summary.
 */
public abstract class LibraryEntity {

    /** Returns a formatted, human-readable description of the entity. */
    public abstract String getSummary();

    /** Returns the primary key identifier of the entity. */
    public abstract int getId();

    @Override
    public String toString() {
        return getSummary();
    }
}
