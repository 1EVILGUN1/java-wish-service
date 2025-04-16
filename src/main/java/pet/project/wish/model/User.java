package pet.project.wish.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;

@Data
@Table("users")
public class User {
    @Id
    private Long id;
    private String name;
    @Column("last_name")
    private String lastName;
    private String password;
    private LocalDate birthday;
    @Column("friends_ids")
    private List<Long> friendsIds;
    @Column("present_ids")
    private List<Long> presentIds;
    private String url;
}
