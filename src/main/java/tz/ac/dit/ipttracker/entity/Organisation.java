package tz.ac.dit.ipttracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organisations")
@Getter
@Setter
@NoArgsConstructor
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String sector;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(length = 255)
    private String address;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;
}
