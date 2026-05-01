package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * BorrowRecord – represents a borrowing transaction.
 *
 * OOP concepts: Encapsulation, Inheritance, Polymorphism.
 * Business logic: isOverdue() checks the due date against today's date.
 */
public class BorrowRecord extends LibraryEntity {

    private int recordId;
    private int bookId;
    private int memberId;
    private String borrowDate;
    private String dueDate;
    private String returnStatus;

    // Joined display fields (populated by DAO queries)
    private String bookTitle;
    private String memberName;

    public BorrowRecord() {}

    public BorrowRecord(int recordId, int bookId, int memberId,
                        String borrowDate, String dueDate, String returnStatus) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnStatus = returnStatus;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────
    @Override
    public int getId() { return recordId; }
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public String getBookTitle() { return bookTitle != null ? bookTitle : String.valueOf(bookId); }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getMemberName() { return memberName != null ? memberName : String.valueOf(memberId); }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    /**
     * Determines whether this record is overdue.
     * A record is overdue if its status is not 'Returned' and today is past the due date.
     */
    public boolean isOverdue() {
        if ("Returned".equalsIgnoreCase(returnStatus)) return false;
        try {
            LocalDate due = LocalDate.parse(dueDate);
            return LocalDate.now().isAfter(due);
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the number of days overdue (negative = days remaining). */
    public long daysOverdue() {
        try {
            LocalDate due = LocalDate.parse(dueDate);
            return ChronoUnit.DAYS.between(due, LocalDate.now());
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Polymorphic override ───────────────────────────────────────────────────
    @Override
    public String getSummary() {
        String overdueFlag = isOverdue() ? " ⚠ OVERDUE" : "";
        return String.format(
            "RECORD ID: %d | Book: %-25s | Member: %-20s | Borrowed: %s | Due: %s | Status: %s%s",
            recordId, getBookTitle(), getMemberName(), borrowDate, dueDate, returnStatus, overdueFlag);
    }
}
