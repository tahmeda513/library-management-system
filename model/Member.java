package model;

/**
 * Member – represents a library member (student or staff).
 *
 * OOP concepts: Encapsulation, Inheritance (extends LibraryEntity),
 * Polymorphism (overrides getSummary).
 */
public class Member extends LibraryEntity {

    private int memberId;
    private String memberName;
    private String email;
    private String membershipType;

    public Member() {}

    public Member(int memberId, String memberName, String email, String membershipType) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.email = email;
        this.membershipType = membershipType;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────
    @Override
    public int getId() { return memberId; }
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    // ── Polymorphic override ───────────────────────────────────────────────────
    @Override
    public String getSummary() {
        return String.format("MEMBER ID: %d | Name: %-25s | Email: %-35s | Type: %s",
                memberId, memberName, email, membershipType);
    }
}
