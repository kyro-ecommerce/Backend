package com.kyro.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/contact")
public class ContactController {

    @Value("${contact.info.phone}")
    private String phone;

    @Value("${contact.info.email}")
    private String email;

    @Value("${contact.info.address}")
    private String address;

    @Value("${contact.info.businessHours}")
    private String businessHours;

    @Value("${contact.info.facebook}")
    private String facebookUrl;

    @Value("${contact.info.instagram}")
    private String instagramUrl;

    @Value("${contact.info.youtube}")
    private String youtubeUrl;

    @GetMapping("/info")
    public ResponseEntity<ContactDTO> getInfo() {
        List<Map<String, String>> socialMedia = List.of(
                Map.of("facebook", facebookUrl),
                Map.of("instagram", instagramUrl),
                Map.of("youtube", youtubeUrl)
        );
        ContactDTO contactDTO = new ContactDTO(phone, email, address, businessHours, socialMedia);
        return ResponseEntity.ok(contactDTO);
    }
}
