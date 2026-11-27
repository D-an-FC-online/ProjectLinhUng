package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_users")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(name = "password_hash") private String passwordHash;
    private String role = "ADMIN";
    @Column(name = "is_active") private boolean active = true;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}
