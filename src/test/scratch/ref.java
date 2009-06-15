public class ref {
    public ref() {
        foo x = new foo();
        System.out.println(x.y.baz);
    }
}

class foo {
    public Bar y = new Bar();
    
    public class Bar {
        int baz = 7;
        public Bar() {
            System.out.println("baz");
        }
    }
}