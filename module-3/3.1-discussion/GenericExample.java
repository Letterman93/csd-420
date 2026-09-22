public class GenericExample<T> {

    // Stores a value using the type selected when
    // the GenericExample object is created.
    private T value;

    public GenericExample(T value) {
        this.value = value;
    }

    // Returns the value using the same type.
    public T getValue() {
        return value;
    }

    // Generic method that can print different types of values.
    public static <T> void printValue(T value) {
        System.out.println("Value: " + value);
    }

    public static void main(String[] args) {

        // The same generic class is used with two different types.
        GenericExample<String> name =
                new GenericExample<>("Zak");

        GenericExample<Integer> number =
                new GenericExample<>(420);

        System.out.println(name.getValue());
        System.out.println(number.getValue());

        // The generic method also works with different types.
        printValue("Advanced Java");
        printValue(100);
        printValue(3.14);
    }
}
