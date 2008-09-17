public class Raw {
    public static void main(String[] args) {
        Class x = Bob.class;
        System.out.println(x.getName());
    }
    
    static void class1(Class c) {
        System.out.println(c.getName());
    }
}

class Bob {
    
}