package by.numberb.numberbv1.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

  @MongoId(targetType = FieldType.OBJECT_ID)
  private ObjectId id;
  private String login;
  private Integer clientId;
  private Long chatId;
  private String firstName;
  private String lastName;
  private Integer score = 0;
  private int level = 2;
}
