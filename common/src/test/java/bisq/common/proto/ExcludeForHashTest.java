package bisq.common.proto;

import bisq.common.encoding.Hex;
import bisq.common.proto.mocks.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
public class ExcludeForHashTest {

    @Test
    public void testExcludeForHash() {
        String serialized, serializedForHash;
        Child child;
        Parent parent;

        // No annotations
        child = new ChildMock("childValue");
        parent = new ParentMock("parentValue", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertEquals(serialized, serializedForHash);

        // ParentMockWithExcludedValue
        child = new ChildMock("childValue");
        parent = new ParentMockWithExcludedValue("parentValue", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertNotEquals(serialized, serializedForHash);

        // If annotated field is set to default value (empty string) we get same results
        parent = new ParentMockWithExcludedValue("", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertEquals(serialized, serializedForHash);

        // ParentMockWithExcludedChild
        child = new ChildMock("childValue");
        parent = new ParentMockWithExcludedChild("parentValue", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertNotEquals(serialized, serializedForHash);

        // ParentMockWithExcludedChild and ChildMockWithExcludedValue
        child = new ChildMockWithExcludedValue("childValue");
        parent = new ParentMockWithExcludedChild("parentValue", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertNotEquals(serialized, serializedForHash);


        // If child is excluded it does not matter what actual child impl is
        child = new ChildMockWithExcludedValue("childValue");
        parent = new ParentMockWithExcludedChild("parentValue", child);
        var serialized1 = Hex.encode(parent.serialize(false));

        child = new ChildMock("childValue");
        parent = new ParentMockWithExcludedChild("parentValue", child);
        var serialized2 = Hex.encode(parent.serialize(false));
        assertEquals(serialized1, serialized2);


        // ChildMockWithExcludedValue
        child = new ChildMockWithExcludedValue("childValue");
        parent = new ParentMock("parentValue", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertNotEquals(serialized, serializedForHash);


        // ChildMockWithExcludedValue and ParentMockWithExcludedValue
        child = new ChildMockWithExcludedValue("childValue");
        parent = new ParentMockWithExcludedValue("parentValue", child);
        serialized = Hex.encode(parent.serialize(true));
        serializedForHash = Hex.encode(parent.serialize(false));
        assertNotEquals(serialized, serializedForHash);
    }
}
