package io.confluent.developer.ccloud.demo.kstream.data.domain.account;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

  private int number;
  private String firstName;
  private String lastName;
  private String streetAddress;
  private String numberAddress;
  private String cityAddress;
  private String countryAddress;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  
  @Id
  public int getNumber() {
    return number;
  }

  @Column(nullable = false)
  public LocalDateTime getUpdateDate() {
    return updateDate;
  }

}
