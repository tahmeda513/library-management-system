# St Mary's University Digital Library Management System

**CPS4005 – Object-Oriented Programming | Assessment 2**

## Project Overview

A comprehensive Java-based Library Management System developed for St Mary's University. This application provides full CRUD management of books, members, and borrowing records, backed by a persistent SQLite database and presented through a Java Swing graphical user interface. The system demonstrates core OOP principles including abstraction, encapsulation, inheritance, polymorphism, generics, multi-threading, and the Singleton design pattern.

## Features

### Core Features (MVP)

✅ **Book Management**

* Add, view, update, and delete book records (Title, Author, Category, Availability Status)
* Search books by title, author, and category simultaneously
* Sort books in ascending or descending order

✅ **Member Management**

* Register new members with details (Name, Email, Membership Type)
* View and filter members by name or membership type
* Update and delete member records

✅ **Borrowing Records**

* Record new borrowing transactions linked to books and members
* Track and update return status (Borrowed, Returned, Overdue)
* Filter records by status, member ID, book ID, or date range
* Automatic overdue detection with red row highlighting

✅ **Database Integration**

* SQLite database via JDBC for persistent storage
* Auto-creates schema and seeds sample data on first launch
* Singleton `DatabaseManager` for shared connection management

### Enhanced Features (Medium Requirements)

✅ **Graphical User Interface (GUI)**

* Java Swing-based desktop application with native system look-and-feel
* Tabbed interface: Dashboard, Books, Members, Borrowing Records
* Sortable `JTable` views with column headers
* Confirmation dialogs for destructive actions

✅ **Data Validation \& Error Handling**

* Email format validation via regex
* Numeric ID validation
* ISO date format validation (`yyyy-MM-dd`)
* Due date must be strictly after borrow date
* Graceful DB error messages surfaced to the user

### Advanced Features

✅ **SwingWorker Multi-threading**

* All database queries execute off the Event Dispatch Thread (EDT)
* Prevents GUI freezing during data load operations

✅ **Dashboard Panel**

* Live statistics: total books, available vs. borrowed, total members, overdue count
* Overdue records summary list with refresh button

✅ **Advanced Search \& Filtering**

* Multi-field book search (title + author + category)
* Borrowing filter by status, date range, member ID, and book ID

## OOP Concepts Applied

|Concept|Where Used|
|-|-|
|**Abstraction**|`LibraryEntity` abstract class; `GenericDAO<T,K>` interface|
|**Encapsulation**|All model fields private with getters/setters|
|**Inheritance**|`Book`, `Member`, `BorrowRecord` extend `LibraryEntity`|
|**Polymorphism**|`getSummary()` overridden in each entity; DAO implementations|
|**Generics**|`GenericDAO<T, K>` used by all three DAOs|
|**Exception Handling**|Try-catch in all DAO methods; DB failures surfaced to user|
|**Multi-threading**|`SwingWorker` used in every panel for background DB loading|
|**Singleton**|`DatabaseManager` — single shared DB connection|

## Project Structure

```
LibrarySystem/
├── Main.java                          ← Application entry point
├── db/
│   └── DatabaseManager.java           ← Singleton DB connection \& schema init
├── model/
│   ├── LibraryEntity.java             ← Abstract base class
│   ├── Book.java
│   ├── Member.java
│   └── BorrowRecord.java
├── dao/
│   ├── GenericDAO.java                ← Generic CRUD interface
│   ├── BookDAO.java
│   ├── MemberDAO.java
│   └── BorrowRecordDAO.java
├── ui/
│   ├── MainWindow.java                ← Main JFrame with tabbed navigation
│   ├── DashboardPanel.java            ← Live statistics view
│   ├── BookPanel.java
│   ├── MemberPanel.java
│   └── BorrowPanel.java
├── util/
│   └── InputValidator.java            ← Centralised validation utility
├── lib/
│   └── sqlite-jdbc.jar                ← SQLite JDBC driver
├── out/                               ← Compiled .class files
├── build.bat                          ← Windows build \& run script
└── README.md
```

## System Requirements

* **Java**: JDK 22 or higher
* **Operating System**: Windows, macOS, or Linux
* **RAM**: 512 MB minimum (1 GB recommended)
* **Disk Space**: 50 MB for application and database

## Installation \& Setup

### 1\. Download the SQLite JDBC Driver

Download `sqlite-jdbc-3.45.1.0.jar` from Maven Central and place it in the `lib/` directory:

```
https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/
```

### 2\. Build \& Run

**Windows:**

```bat
build.bat
```

**Linux / macOS:**

```bash
# Compile
find . -name "\*.java" | grep -v "/out/" | xargs javac --release 22 -cp lib/sqlite-jdbc.jar -d out

# Run
java -cp out:lib/sqlite-jdbc.jar Main
```

**Manual compile + run (cross-platform):**

```bash
# Compile
find src -name "\*.java" | xargs javac --release 22 -cp lib/sqlite-jdbc.jar -d out

# Run
java -cp out:lib/sqlite-jdbc.jar Main
```

### 3\. IDE Setup (NetBeans / VS Code / IntelliJ)

1. Open the `LibrarySystem/` folder as a project.
2. Add `lib/sqlite-jdbc.jar` to the project classpath / libraries.
3. Set `Main` as the main class.
4. Build and run.

## Running the Application

On launch, the application will:

1. Connect to (or create) `library.db` in the working directory.
2. Create all required tables if they do not already exist.
3. Seed 3 sample books, 3 members, and 3 borrow records on first run.
4. Open the main GUI window with a tabbed interface.

### Dashboard Tab

Displays live counts of total books, available books, borrowed books, total members, total borrowing records, and overdue items. Click **↺ Refresh Dashboard** to reload statistics.

### Books Tab

* Search books by title, author, or category using the filter fields.
* Click **Add Book** to register a new book.
* Select a row and click **Edit** to update book details.
* Select a row and click **Delete** to remove a book (with confirmation).
* Use the column headers to sort ascending or descending.

### Members Tab

* Search members by name or filter by membership type.
* Click **Add Member** to register a new member.
* Select a row and click **Edit** to update member details.
* Select a row and click **Delete** to remove a member (with confirmation).

### Borrowing Records Tab

* Filter records by status, date range, member ID, or book ID.
* Click **Add Record** to log a new borrowing transaction.
* Select a row and click **Edit** to update the return status.
* Select a row and click **Delete** to remove an incorrect record.
* Overdue records are highlighted in red automatically.

## Database Schema

### Books Table

```sql
CREATE TABLE books (
    book\_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    title               TEXT    NOT NULL,
    author              TEXT    NOT NULL,
    category            TEXT    NOT NULL,
    availability\_status TEXT    NOT NULL DEFAULT 'Available'
);
```

### Members Table

```sql
CREATE TABLE members (
    member\_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    member\_name     TEXT    NOT NULL,
    email           TEXT    NOT NULL UNIQUE,
    membership\_type TEXT    NOT NULL
);
```

### Borrow Records Table

```sql
CREATE TABLE borrow\_records (
    record\_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    book\_id       INTEGER NOT NULL,
    member\_id     INTEGER NOT NULL,
    borrow\_date   DATE    NOT NULL,
    due\_date      DATE    NOT NULL,
    return\_status TEXT    NOT NULL DEFAULT 'Borrowed',
    FOREIGN KEY (book\_id)   REFERENCES books(book\_id),
    FOREIGN KEY (member\_id) REFERENCES members(member\_id)
);
```

## Data Validation Rules

### Email

* Must match the pattern `username@domain.ext` (validated via regex).
* Required field for member registration.

### IDs (Book ID / Member ID)

* Must be a positive integer.
* Used when creating borrowing records to reference existing books and members.

### Dates

* Format: `yyyy-MM-dd` (e.g., `2026-04-27`).
* Due date must be strictly after borrow date.
* Neither field may be blank when creating a borrowing record.

### Membership Types

* Student
* Staff
* Premium
* Senior

### Availability Status

* Available
* Borrowed
* Reserved

### Borrowing / Return Status

* Borrowed
* Returned
* Overdue

## Key Classes and Methods

### `GenericDAO<T, K>` (Interface)

* `create(T entity)` — insert a new entity
* `findById(K id)` — retrieve by primary key
* `findAll()` — retrieve all records
* `update(T entity)` — update an existing record
* `delete(K id)` — delete by primary key

### `BookDAO`

* `findAll(String sortColumn, boolean ascending)` — sorted retrieval
* `search(String title, String author, String category)` — multi-field search
* `findByAvailability(String status)` — filter by status

### `MemberDAO`

* `searchByName(String name)` — partial-match name search
* `findByMembershipType(String type)` — filter by type

### `BorrowRecordDAO`

* `findOverdue()` — records past their due date with status "Borrowed"
* `findByMemberId(int memberId)` — borrowing history for a member
* `findByBookId(int bookId)` — borrowing history for a book
* `findByDateRange(LocalDate from, LocalDate to)` — date-range filter
* `findByStatus(String status)` — filter by return status

### `DatabaseManager` (Singleton)

* `getInstance()` — returns the single shared instance (thread-safe)
* `getConnection()` — returns the live connection, reconnecting if closed

### `InputValidator` (Utility)

* `isNotBlank(String value)` — null/blank check
* `isPositiveInteger(String value)` — numeric ID validation
* `isValidEmail(String email)` — regex email check
* `isValidDate(String date)` — `yyyy-MM-dd` format check
* `isDateAfter(LocalDate start, LocalDate end)` — date range validation

## Error Handling

* **Database connection failure**: A dialog is shown at startup and the application exits cleanly.
* **Invalid input**: Field-level validation messages guide the user before any DB call is made.
* **SQL exceptions**: Caught in every DAO method; logged via `java.util.logging` and surfaced as user-friendly messages.
* **Reconnection**: `DatabaseManager.getConnection()` automatically reconnects if the connection is found closed.

## Libraries Used

|Library|Purpose|
|-|-|
|`javax.swing` / `java.awt`|GUI framework|
|`java.sql`|JDBC database connectivity|
|`java.time`|Date parsing and overdue calculation|
|`java.util.concurrent`|`SwingWorker` (background threading)|
|`java.util.logging`|Application logging|
|`org.xerial.sqlite-jdbc`|SQLite JDBC driver|

## Troubleshooting

### `No suitable driver found`

**Cause**: `sqlite-jdbc.jar` is missing from the classpath.  
**Fix**: Ensure the JAR is in `lib/` and the classpath includes `lib/sqlite-jdbc.jar` (or `lib/\*`).

### GUI does not open

**Cause**: Compilation error or incorrect main class.  
**Fix**: Confirm the project compiled without errors and the main class is `Main` (not `com.stmarys.library.Main` — this project uses the default package).

### `Table not found` / schema errors

**Cause**: A stale or corrupt `library.db` from a previous version.  
**Fix**: Delete `library.db` from the working directory and relaunch — the schema will be recreated automatically.

### Overdue records not highlighted

**Cause**: System date or borrow dates entered incorrectly.  
**Fix**: Ensure dates follow `yyyy-MM-dd` format and that the system clock is correct.

## Future Enhancements

1. User authentication with role-based access control (Admin / Librarian / Student)
2. Fine calculation and payment tracking for overdue returns
3. Email notifications for approaching and past due dates
4. Advanced reporting and exportable PDF/CSV summaries
5. Book reservation and waitlist system
6. Mobile or web front-end integration
7. Book cover image support

\---

*Submitted for CPS4005 Assessment 2 | St Mary's University, Twickenham*

**Version**: 1.0  
**Last Updated**: April 27, 2026  
**Developer**: St Mary's University IT Department

