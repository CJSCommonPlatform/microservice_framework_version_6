package uk.gov.justice.services.test.utils.common.reflection;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ReflectionUtilsTest {

    @Test
    public void shouldSetThePrivateFieldOfAnObject() throws Exception {

        final MyTestReflectionsClass2 myTestReflectionsClass = new MyTestReflectionsClass2("oldValue");

        ReflectionUtils.setField(myTestReflectionsClass, "property", "newValue");

        assertThat(myTestReflectionsClass.getProperty(), is("newValue"));
    }

    public static class MyTestReflectionsClass {
        private final String property;

        public MyTestReflectionsClass(final String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    public static class MyTestReflectionsClass2 extends MyTestReflectionsClass {

        public MyTestReflectionsClass2(final String property) {
            super(property);
        }
    }
}
