package com.cv.s03cloudgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FallBackResponseDto implements Serializable {

    private boolean status;
    private String message;
    private Integer type;
    private Object object;

}
