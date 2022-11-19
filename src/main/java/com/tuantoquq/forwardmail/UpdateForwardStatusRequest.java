package com.tuantoquq.forwardmail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateForwardStatusRequest {
    private String email;
}
