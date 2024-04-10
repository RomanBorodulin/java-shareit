package ru.practicum.shareit.user.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    @Test
    @SneakyThrows
    public void serializeInCorrectFormat() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("email@email.ru")
                .build();

        JsonContent<UserDto> json = jacksonTester.write(userDto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("User");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("email@email.ru");
    }

    @Test
    @SneakyThrows
    void deserializeFromCorrectFormat() {
        String json = "{\"id\": \"1\", \"name\": \"User\", \"email\": \"email@email.ru\"}";

        UserDto user = jacksonTester.parseObject(json);

        assertThat(user.getId()).isEqualTo(Long.valueOf(1));
        assertThat(user.getName()).isEqualTo("User");
        assertThat(user.getEmail()).isEqualTo("email@email.ru");
    }

}