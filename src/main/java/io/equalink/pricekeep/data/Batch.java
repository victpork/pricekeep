package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "batch_service_param", joinColumns = @JoinColumn(name = "batch_id"))    
    private List<String> services;

    private String parameter;
}
