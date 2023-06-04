package persistence.entity;

import domain.model.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("Movement")
public class MovementEntity {

    @Id
    private UUID id;

    @Column("account_id")
    private UUID accountId;

    private String description;

    private BigDecimal amount;

    private MovementType type;

    private LocalDateTime date;

    public MovementEntity() {
    }

    public MovementEntity(UUID id, UUID accountId, String description, BigDecimal amount, MovementType type, LocalDateTime date) {
        this.id = id;
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovementEntity that = (MovementEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(accountId, that.accountId) && Objects.equals(description, that.description) && Objects.equals(amount, that.amount) && type == that.type && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, description, amount, type, date);
    }
}
