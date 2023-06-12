package ru.practicum.shareit.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@Builder
public class MethodInfo {
    private String methodName;
    private Object[] args;

    public MethodInfo(String methodName, Object... args) {
        this.methodName = methodName;
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodInfo)) return false;

        MethodInfo methodInfo = (MethodInfo) o;

        if (!Objects.equals(methodName, methodInfo.methodName)) return false;
        return Arrays.equals(args, methodInfo.args);
    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }
}
