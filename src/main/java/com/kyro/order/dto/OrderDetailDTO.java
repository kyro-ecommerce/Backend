package com.kyro.order.dto;

import com.kyro.auth.dto.UserDTO;
import com.kyro.order.Order;



import lombok.Data;

@Data
public class OrderDetailDTO extends OrderDTO {
    private UserDTO user;

    public OrderDetailDTO(Order order) {
        super(order); // Gọi constructor của lớp cha

        // Bổ sung thông tin user
        if (order.getUser() != null) {
            this.user = new UserDTO(order.getUser());
        }
    }
}
