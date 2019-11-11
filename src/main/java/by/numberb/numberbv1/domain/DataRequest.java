package by.numberb.numberbv1.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataRequest {
  private long id;
  private UUID uuid = UUID.randomUUID();
  private String clientId;
  private int level;
  private long record;
}
