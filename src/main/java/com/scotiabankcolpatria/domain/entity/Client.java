package com.scotiabankcolpatria.domain.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scotiabankcolpatria.domain.enums.ClientStatus;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = @Index(name = "idx_client_name", columnList = "name"))
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Client implements Serializable {

    private static final long serialVersionUID = 3567087174121099153L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Integer id;

    @Column(nullable = false, length = 45, unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(length = 15)
    private String phone;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status = ClientStatus.ENABLED;

}
