package ru.practicum.shareit.jsontest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingRequestDtoTest {
    @Autowired
    private JacksonTester<BookingRequestDto> jsonTester;

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    public void testSerializeToJson() throws Exception {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        String startString = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endString = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        dto.setStart(start);
        dto.setEnd(end);

        String json = jsonTester.write(dto).getJson();
        String expected = String.format("{\"itemId\":%d,\"start\":\"%s\",\"end\":\"%s\"}", 1, startString, endString);

        assertThat(json).isEqualTo(expected);
    }

    @Test
    public void testDeserializeFromJson() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"2023-05-28T12:00:00\",\"end\":\"2023-05-28T14:00:00\"}";

        BookingRequestDto dto = jsonTester.parseObject(json);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2023, 5, 28, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2023, 5, 28, 14, 0, 0));
    }

    @Test
    public void testValidBookingRequestDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusSeconds(3));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThat(validator.validate(dto)).isEmpty();
    }

//    @Test
//    public void testMissingItemId() {
//        BookingRequestDto dto = new BookingRequestDto();
//        dto.setStart(LocalDateTime.now().plusSeconds(3));
//        dto.setEnd(LocalDateTime.now().plusHours(2));
//
//        assertThat(validator.validate(dto)).hasSize(1);
//    }
//
//    @Test
//    public void testInvalidStart() {
//        BookingRequestDto dto = new BookingRequestDto();
//        dto.setItemId(1L);
//        dto.setStart(LocalDateTime.now().plusHours(2));
//        dto.setEnd(LocalDateTime.now().plusHours(1));
//
//        assertThat(validator.validate(dto)).hasSize(1);
//    }
//
//    @Test
//    public void testInvalidEnd() {
//        BookingRequestDto dto = new BookingRequestDto();
//        dto.setItemId(1L);
//        dto.setStart(LocalDateTime.now().plusSeconds(3));
//        dto.setEnd(LocalDateTime.now().minusHours(1));
//
//        assertThat(validator.validate(dto)).hasSizeGreaterThan(0);
//    }
}
