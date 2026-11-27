package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "languages")
public class Language {
    public static final String SOURCE = "vi";

    @Id
    @Column(length = 5)
    private String code;
    private String name;
    @Column(name = "is_source")
    private boolean source;

    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isSource() { return source; }
}
