package ru.practicum.shareit.utility;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ValidationException;

@UtilityClass
public class PageUtils {
    public Pageable getPageable(int from, int size, Sort sort) {
        if (from < 0) {
            throw new ValidationException("Введен отрицательный индекс первого элемента");
        }
        if (size <= 0) {
            throw new ValidationException("Количество элементов для отображения не может быть меньше или равно нулю");
        }
        return sort != null ? PageRequest.of(from / size, size, sort)
                : PageRequest.of(from / size, size);
    }

    public Pageable getPageable(int from, int size) {
        return getPageable(from, size, null);
    }
}
