package com.example.batchprocessing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class Person {

  private String lastName;
  private String firstName;

  @Override
  public String toString() {
    return "firstName: " + firstName + ", lastName: " + lastName;
  }

}