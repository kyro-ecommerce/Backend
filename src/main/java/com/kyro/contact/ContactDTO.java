package com.kyro.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {
    String phone;
    String email;
    String address;
    String businessHours;
    List<Map<String, String>> socialMedia;
}
