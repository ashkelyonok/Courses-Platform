package com.askel.coursesplatform.model.entity;

import com.askel.coursesplatform.model.enums.CourseStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;


    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = true)
    @JoinColumn(name = "instructor_id", nullable = true)
    private User instructor;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "course_students",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> students = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.PENDING_INSTRUCTOR;

    //private List<Module>; //OneToMany
    //private BigDecimal price;
    //private LocalDate startDate;
    //private LocalDate endDate;
    //private String category;
}
