package hellojpa;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Member {

    @Id //pk 매핑
    private Long id;

    //DB column명은 name이다.
    @Column(name = "name")
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    private String description;

    @Transient //DB에 매핑되지 않는다.
    private int temp;
    
    public Member(){}

}
