package pet.project.wish.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@Table("presents")
public class Present {
    @Id
    private Long id;
    private String title;
    private String description;
    private List<String> links;
    private String url;
    private Boolean reserved;
}
