package api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel {
    private long id;
    private double amount;
    private String type;
    @JsonIgnore
    private ZonedDateTime timestamp;
    private int relatedAccountId;
}