public class StringBuilderBufferExample {

    public static void main(String[] args) {

        // StringBuilder is normally used when thread safety is not needed.
        StringBuilder builder = new StringBuilder();

        builder.append("Advanced");
        builder.append(" Java");
        builder.append(" Programming");

        System.out.println("StringBuilder: " + builder);

        // StringBuffer works similarly but uses synchronized methods.
        StringBuffer buffer = new StringBuffer();

        buffer.append("Java");
        buffer.append(" Multithreading");

        System.out.println("StringBuffer: " + buffer);

        // Both objects can also be modified after they are created.
        builder.replace(0, 8, "Learning");
        buffer.insert(4, " and");

        System.out.println("Modified StringBuilder: " + builder);
        System.out.println("Modified StringBuffer: " + buffer);
    }
}
