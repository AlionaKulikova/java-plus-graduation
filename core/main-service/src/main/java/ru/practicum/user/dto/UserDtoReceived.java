package ru.practicum.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.Validator;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDtoReceived {

    private Long id;

    @Email(groups = {Validator.Create.class})
    @Size(min = 6, max = 254, groups = {Validator.Create.class})
    @NotBlank(groups = {Validator.Create.class})
    private String email;

    @NotBlank(groups = {Validator.Create.class})
    @Size(min = 2, max = 250, groups = {Validator.Create.class})
    private String name;


    private Boolean isAdmin;
}