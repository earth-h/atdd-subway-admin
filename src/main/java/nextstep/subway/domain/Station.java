package nextstep.subway.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Station extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Name name;

    protected Station() {
    }

    public Station(String name) {
        this.name = Name.of(name);
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }
}
