package com.example.labadaptor.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformationDTO {

    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    private String address;
}
