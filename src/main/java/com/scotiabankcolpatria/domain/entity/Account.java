package com.scotiabankcolpatria.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scotiabankcolpatria.domain.enums.AccountStatus;
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
@Table(indexes = @Index(name = "idx_account_number", columnList = "number"))
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Account implements Serializable {

    private static final long serialVersionUID = 3567087174121099153L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, unique = true, length = 19)
    private String number;

    @Column(nullable = false)
    private Double balance;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ENABLED;

}
