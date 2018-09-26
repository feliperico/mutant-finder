package meli.magneto.mutantfinder;

import org.springframework.data.annotation.Id;

public class Statistics {
    public static final String MUTANT_DNA = "count_mutant_dna";
    public static final String HUMAN_DNA = "count_human_dna";

    @Id
    private String id;
    private String name;
    private Long amount;

    public Statistics() {}

    public Statistics(String name, Long amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
