package integration.common;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.common.MethodInfo;

import static org.junit.jupiter.api.Assertions.*;

class MethodInfoTest {
    @Test
    void equals_ShouldReturnFalse_WhenComparedWithDifferentObjectType() {
        MethodInfo methodInfo = new MethodInfo("methodName", 1, "test");
        Object differentObject = new Object();

        boolean result = methodInfo.equals(differentObject);

        assertFalse(result);
    }

    @Test
    void equals_ShouldReturnTrue_WhenComparedWithEqualMethodInfo() {
        MethodInfo methodInfo1 = new MethodInfo("methodName", 1, "test");
        MethodInfo methodInfo2 = new MethodInfo("methodName", 1, "test");

        boolean result = methodInfo1.equals(methodInfo2);

        assertTrue(result);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparedWithMethodInfoWithDifferentMethodName() {
        MethodInfo methodInfo1 = new MethodInfo("methodName", 1, "test");
        MethodInfo methodInfo2 = new MethodInfo("differentMethodName", 1, "test");

        boolean result = methodInfo1.equals(methodInfo2);

        assertFalse(result);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparedWithMethodInfoWithDifferentArguments() {
        MethodInfo methodInfo1 = new MethodInfo("methodName", 1, "test");
        MethodInfo methodInfo2 = new MethodInfo("methodName", 2, "test");

        boolean result = methodInfo1.equals(methodInfo2);

        assertFalse(result);
    }
}