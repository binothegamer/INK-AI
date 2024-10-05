package com.AI.AIEXAMPLE.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private String marketCap;
    private String EV;
    private String sharesOut;
    private String Revenue;
    private String Employee;

}
