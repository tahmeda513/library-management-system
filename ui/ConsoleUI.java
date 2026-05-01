package ui;

import dao.BookDAO;
import dao.BorrowRecordDAO;
import dao.MemberDAO;
import model.Book;
import model.BorrowRecord;
import model.Member;
import util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * ConsoleUI – text-based, menu-driven interface for the Library Management System.
 *
 * OOP concepts: Encapsulation (all I/O logic here), Abstraction (hides DAO calls from the user).
 * Satisfies the Basic Requirements: console UI with five top-level menus.
 *
 * Menu structure:
 *   1. Manage Books
 *   2. Manage Members
 *   3. Manage Borrowing Records
 *   4. Search Records
 *   5. Exit System
 */
public class ConsoleUI {

    private static final Logger LOGGER = Logger.getLogger(ConsoleUI.class.getName());

    private final Scanner scanner = new Scanner(System.in);
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowRecordDAO borrowDAO = new BorrowRecordDAO();

    // ── Entry point ───────────────────────────────────────────────────────────

    /** Starts the interactive console session. */
    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Select option: ", 1, 5);
            switch (choice) {
                case 1 -> manageBooksMenu();
                case 2 -> manageMembersMenu();
                case 3 -> manageBorrowingMenu();
                case 4 -> searchMenu();
                case 5 -> { running = false; println("Goodbye!"); }
            }
        }
    }

    // ── Main menu ─────────────────────────────────────────────────────────────

    private void printMainMenu() {
        println("\n╔══════════════════════════════════════╗");
        println("║       LIBRARY MANAGEMENT SYSTEM      ║");
        println("╠══════════════════════════════════════╣");
        println("║  1. Manage Books                     ║");
        println("║  2. Manage Members                   ║");
        println("║  3. Manage Borrowing Records         ║");
        println("║  4. Search Records                   ║");
        println("║  5. Exit System                      ║");
        println("╚══════════════════════════════════════╝");
    }

    // ── 1. Manage Books ───────────────────────────────────────────────────────

    private void manageBooksMenu() {
        boolean back = false;
        while (!back) {
            println("\n--- Manage Books ---");
            println("  1. List all books");
            println("  2. Add a book");
            println("  3. Edit a book");
            println("  4. Delete a book");
            println("  5. View book by ID");
            println("  0. Back");
            int choice = readInt("Select: ", 0, 5);
            switch (choice) {
                case 1 -> listBooks();
                case 2 -> addBook();
                case 3 -> editBook();
                case 4 -> deleteBook();
                case 5 -> viewBookById();
                case 0 -> back = true;
            }
        }
    }

    private void listBooks() {
        List<Book> books = bookDAO.findAll();
        if (books.isEmpty()) { println("No books found."); return; }
        println("\n" + headerLine());
        books.forEach(b -> println(b.getSummary()));
        println(headerLine());
        println("Total: " + books.size() + " book(s).");
    }

    private void addBook() {
        println("\n--- Add Book ---");
        String title = readNonBlank("Title: ");
        String author = readNonBlank("Author: ");
        String category = readNonBlank("Category: ");
        String status = readEnum("Availability (Available/Borrowed): ",
                new String[]{"Available", "Borrowed"});

        Book book = new Book(0, title, author, category, status);
        if (bookDAO.create(book)) println("✓ Book added successfully.");
        else println("✗ Failed to add book.");
    }

    private void editBook() {
        println("\n--- Edit Book ---");
        int id = readPositiveInt("Enter Book ID to edit: ");
        Book existing = bookDAO.findById(id);
        if (existing == null) { println("Book not found."); return; }
        println("Current: " + existing.getSummary());

        String title = readWithDefault("New title [" + existing.getTitle() + "]: ", existing.getTitle());
        String author = readWithDefault("New author [" + existing.getAuthor() + "]: ", existing.getAuthor());
        String category = readWithDefault("New category [" + existing.getCategory() + "]: ", existing.getCategory());
        String status = readEnumWithDefault(
                "New status (Available/Borrowed) [" + existing.getAvailabilityStatus() + "]: ",
                new String[]{"Available", "Borrowed"}, existing.getAvailabilityStatus());

        existing.setTitle(title);
        existing.setAuthor(author);
        existing.setCategory(category);
        existing.setAvailabilityStatus(status);

        if (bookDAO.update(existing)) println("✓ Book updated.");
        else println("✗ Update failed.");
    }

    private void deleteBook() {
        println("\n--- Delete Book ---");
        int id = readPositiveInt("Enter Book ID to delete: ");
        Book book = bookDAO.findById(id);
        if (book == null) { println("Book not found."); return; }
        println("About to delete: " + book.getSummary());
        if (confirm("Confirm delete? (y/n): ")) {
            if (bookDAO.delete(id)) println("✓ Book deleted.");
            else println("✗ Delete failed.");
        } else {
            println("Cancelled.");
        }
    }

    private void viewBookById() {
        int id = readPositiveInt("Enter Book ID: ");
        Book book = bookDAO.findById(id);
        if (book == null) println("No book found with ID " + id + ".");
        else println("\n" + book.getSummary());
    }

    // ── 2. Manage Members ─────────────────────────────────────────────────────

    private void manageMembersMenu() {
        boolean back = false;
        while (!back) {
            println("\n--- Manage Members ---");
            println("  1. List all members");
            println("  2. Add a member");
            println("  3. Edit a member");
            println("  4. Delete a member");
            println("  5. View member by ID");
            println("  0. Back");
            int choice = readInt("Select: ", 0, 5);
            switch (choice) {
                case 1 -> listMembers();
                case 2 -> addMember();
                case 3 -> editMember();
                case 4 -> deleteMember();
                case 5 -> viewMemberById();
                case 0 -> back = true;
            }
        }
    }

    private void listMembers() {
        List<Member> members = memberDAO.findAll();
        if (members.isEmpty()) { println("No members found."); return; }
        println("\n" + headerLine());
        members.forEach(m -> println(m.getSummary()));
        println(headerLine());
        println("Total: " + members.size() + " member(s).");
    }

    private void addMember() {
        println("\n--- Add Member ---");
        String name = readNonBlank("Full name: ");
        String email = readValidEmail("Email: ");
        String type = readEnum("Membership type (Student/Staff): ", new String[]{"Student", "Staff"});

        Member member = new Member(0, name, email, type);
        if (memberDAO.create(member)) println("✓ Member added successfully.");
        else println("✗ Failed to add member.");
    }

    private void editMember() {
        println("\n--- Edit Member ---");
        int id = readPositiveInt("Enter Member ID to edit: ");
        Member existing = memberDAO.findById(id);
        if (existing == null) { println("Member not found."); return; }
        println("Current: " + existing.getSummary());

        String name = readWithDefault("New name [" + existing.getMemberName() + "]: ", existing.getMemberName());
        String email = readWithDefault("New email [" + existing.getEmail() + "]: ", existing.getEmail());
        if (!InputValidator.isValidEmail(email)) {
            println("Invalid email format; keeping existing email.");
            email = existing.getEmail();
        }
        String type = readEnumWithDefault(
                "New type (Student/Staff) [" + existing.getMembershipType() + "]: ",
                new String[]{"Student", "Staff"}, existing.getMembershipType());

        existing.setMemberName(name);
        existing.setEmail(email);
        existing.setMembershipType(type);

        if (memberDAO.update(existing)) println("✓ Member updated.");
        else println("✗ Update failed.");
    }

    private void deleteMember() {
        println("\n--- Delete Member ---");
        int id = readPositiveInt("Enter Member ID to delete: ");
        Member member = memberDAO.findById(id);
        if (member == null) { println("Member not found."); return; }
        println("About to delete: " + member.getSummary());
        if (confirm("Confirm delete? (y/n): ")) {
            if (memberDAO.delete(id)) println("✓ Member deleted.");
            else println("✗ Delete failed.");
        } else {
            println("Cancelled.");
        }
    }

    private void viewMemberById() {
        int id = readPositiveInt("Enter Member ID: ");
        Member member = memberDAO.findById(id);
        if (member == null) println("No member found with ID " + id + ".");
        else println("\n" + member.getSummary());
    }

    // ── 3. Manage Borrowing Records ───────────────────────────────────────────

    private void manageBorrowingMenu() {
        boolean back = false;
        while (!back) {
            println("\n--- Manage Borrowing Records ---");
            println("  1. List all records");
            println("  2. Add a borrowing record");
            println("  3. Mark record as returned");
            println("  4. Delete a record");
            println("  5. List overdue records");
            println("  0. Back");
            int choice = readInt("Select: ", 0, 5);
            switch (choice) {
                case 1 -> listBorrowRecords();
                case 2 -> addBorrowRecord();
                case 3 -> markReturned();
                case 4 -> deleteBorrowRecord();
                case 5 -> listOverdue();
                case 0 -> back = true;
            }
        }
    }

    private void listBorrowRecords() {
        List<BorrowRecord> records = borrowDAO.findAll();
        if (records.isEmpty()) { println("No borrowing records found."); return; }
        println("\n" + headerLine());
        records.forEach(r -> println(r.getSummary()));
        println(headerLine());
        println("Total: " + records.size() + " record(s).");
    }

    private void addBorrowRecord() {
        println("\n--- Add Borrowing Record ---");
        int bookId = readPositiveInt("Book ID: ");
        if (bookDAO.findById(bookId) == null) { println("Book not found."); return; }

        int memberId = readPositiveInt("Member ID: ");
        if (memberDAO.findById(memberId) == null) { println("Member not found."); return; }

        String borrowDate = readValidDate("Borrow date (yyyy-MM-dd) [today]: ", LocalDate.now().toString());
        String dueDate;
        do {
            dueDate = readValidDate("Due date (yyyy-MM-dd): ", null);
            if (!InputValidator.isDueDateAfterBorrowDate(borrowDate, dueDate)) {
                println("Due date must be after borrow date. Please re-enter.");
                dueDate = null;
            }
        } while (dueDate == null);

        BorrowRecord record = new BorrowRecord(0, bookId, memberId, borrowDate, dueDate, "Borrowed");
        if (borrowDAO.create(record)) {
            // Mark the book as borrowed
            Book book = bookDAO.findById(bookId);
            book.setAvailabilityStatus("Borrowed");
            bookDAO.update(book);
            println("✓ Borrowing record created.");
        } else {
            println("✗ Failed to create record.");
        }
    }

    private void markReturned() {
        println("\n--- Mark as Returned ---");
        int id = readPositiveInt("Enter Record ID: ");
        BorrowRecord record = borrowDAO.findById(id);
        if (record == null) { println("Record not found."); return; }
        println("Record: " + record.getSummary());
        if ("Returned".equalsIgnoreCase(record.getReturnStatus())) {
            println("This record is already marked as returned.");
            return;
        }
        record.setReturnStatus("Returned");
        if (borrowDAO.update(record)) {
            // Mark book as available again
            Book book = bookDAO.findById(record.getBookId());
            if (book != null) {
                book.setAvailabilityStatus("Available");
                bookDAO.update(book);
            }
            println("✓ Record marked as returned. Book is now available.");
        } else {
            println("✗ Update failed.");
        }
    }

    private void deleteBorrowRecord() {
        println("\n--- Delete Borrowing Record ---");
        int id = readPositiveInt("Enter Record ID to delete: ");
        BorrowRecord record = borrowDAO.findById(id);
        if (record == null) { println("Record not found."); return; }
        println("About to delete: " + record.getSummary());
        if (confirm("Confirm delete? (y/n): ")) {
            if (borrowDAO.delete(id)) println("✓ Record deleted.");
            else println("✗ Delete failed.");
        } else {
            println("Cancelled.");
        }
    }

    private void listOverdue() {
        List<BorrowRecord> overdue = borrowDAO.findOverdue();
        if (overdue.isEmpty()) { println("No overdue records. 🎉"); return; }
        println("\n⚠ OVERDUE RECORDS (" + overdue.size() + "):");
        println(headerLine());
        overdue.forEach(r -> println(r.getSummary() + "  [" + r.daysOverdue() + " days overdue]"));
        println(headerLine());
    }

    // ── 4. Search Records ─────────────────────────────────────────────────────

    private void searchMenu() {
        boolean back = false;
        while (!back) {
            println("\n--- Search Records ---");
            println("  1. Search books by title, author, or ID");
            println("  2. Search members by name or email");
            println("  3. Search borrowing records by book or member ID");
            println("  0. Back");
            int choice = readInt("Select: ", 0, 3);
            switch (choice) {
                case 1 -> searchBooks();
                case 2 -> searchMembers();
                case 3 -> searchBorrowRecords();
                case 0 -> back = true;
            }
        }
    }

    private void searchBooks() {
        println("\nEnter a book ID to look up by ID, or a keyword to search by title/author:");
        String input = readNonBlank("Search: ").trim();

        // Try numeric → search by ID first
        if (input.matches("\\d+")) {
            int id = Integer.parseInt(input);
            Book book = bookDAO.findById(id);
            if (book != null) {
                println("\nFound by ID:");
                println(book.getSummary());
                return;
            }
            println("No book found with ID " + id + ". Falling back to keyword search...");
        }

        // Keyword search across title, author, and category
        List<Book> results = bookDAO.search(input);
        if (results.isEmpty()) {
            println("No books found matching \"" + input + "\".");
        } else {
            println("\nFound " + results.size() + " result(s):");
            println(headerLine());
            results.forEach(b -> println(b.getSummary()));
            println(headerLine());
        }
    }

    private void searchMembers() {
        String keyword = readNonBlank("Search by name or email: ").trim();
        List<Member> results = memberDAO.search(keyword);
        if (results.isEmpty()) {
            println("No members found matching \"" + keyword + "\".");
        } else {
            println("\nFound " + results.size() + " result(s):");
            println(headerLine());
            results.forEach(m -> println(m.getSummary()));
            println(headerLine());
        }
    }

    private void searchBorrowRecords() {
        println("  1. By Book ID");
        println("  2. By Member ID");
        int choice = readInt("Select: ", 1, 2);
        if (choice == 1) {
            int bookId = readPositiveInt("Book ID: ");
            List<BorrowRecord> records = borrowDAO.findByBook(bookId);
            printBorrowResults(records, "book ID " + bookId);
        } else {
            int memberId = readPositiveInt("Member ID: ");
            List<BorrowRecord> records = borrowDAO.findByMember(memberId);
            printBorrowResults(records, "member ID " + memberId);
        }
    }

    private void printBorrowResults(List<BorrowRecord> records, String label) {
        if (records.isEmpty()) println("No borrowing records found for " + label + ".");
        else {
            println("\nFound " + records.size() + " record(s) for " + label + ":");
            println(headerLine());
            records.forEach(r -> println(r.getSummary()));
            println(headerLine());
        }
    }

    // ── I/O helpers ───────────────────────────────────────────────────────────

    private void printBanner() {
        println("╔═══════════════════════════════════════════════════╗");
        println("║   St Mary's Digital Library Management System     ║");
        println("║            Console Interface v1.0                 ║");
        println("╚═══════════════════════════════════════════════════╝");
    }

    private String headerLine() {
        return "─".repeat(110);
    }

    private void println(String msg) { System.out.println(msg); }

    /** Reads an integer within [min, max]. Loops until valid. */
    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                println("Invalid input. Please enter a number.");
            }
        }
    }

    /** Reads a positive integer. Loops until valid. */
    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val > 0) return val;
                println("Please enter a positive integer.");
            } catch (NumberFormatException e) {
                println("Invalid input. Please enter a number.");
            }
        }
    }

    /** Reads a non-blank string. Loops until valid. */
    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isBlank()) return line;
            println("Input cannot be blank.");
        }
    }

    /** Reads a string; if blank, returns the default value. */
    private String readWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        return line.isBlank() ? defaultValue : line;
    }

    /** Reads one of the allowed enum values (case-insensitive). Loops until valid. */
    private String readEnum(String prompt, String[] allowed) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            for (String a : allowed) {
                if (a.equalsIgnoreCase(line)) return a;
            }
            println("Invalid choice. Please enter one of: " + String.join(", ", allowed));
        }
    }

    /** readEnum with a default fallback when input is blank. */
    private String readEnumWithDefault(String prompt, String[] allowed, String defaultValue) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isBlank()) return defaultValue;
            for (String a : allowed) {
                if (a.equalsIgnoreCase(line)) return a;
            }
            println("Invalid choice. Please enter one of: " + String.join(", ", allowed));
        }
    }

    /**
     * Reads a valid ISO date (yyyy-MM-dd).
     * If blank and defaultValue is provided, returns the default.
     */
    private String readValidDate(String prompt, String defaultValue) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isBlank() && defaultValue != null) return defaultValue;
            if (InputValidator.isValidDate(line)) return line;
            println("Invalid date format. Use yyyy-MM-dd (e.g. 2025-06-01).");
        }
    }

    /** Reads a valid email. Loops until valid. */
    private String readValidEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (InputValidator.isValidEmail(line)) return line;
            println("Invalid email format. Please try again.");
        }
    }

    /** Reads a y/n confirmation. Returns true for 'y'. */
    private boolean confirm(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        return line.equalsIgnoreCase("y");
    }
}
