package com.tuantoquq.forwardmail;

import lombok.Data;

@Data
public class GetCodeForwardRequest {
    private String email;
    private String service;
}
