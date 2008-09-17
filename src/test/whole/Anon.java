public class Anon {
   static String when;

   public static class Obj {
      private final String whenFactory;
      private final String whenObject = when;

      public Obj(String when) {
         whenFactory = when;
      }

      public String sayWhen() {
         return "Factory from "+whenFactory+", Obj from "+whenObject;
      }
   }

   public static class Factory {
      private final String whenFactory = when;

      public Obj make() {
         return new Obj(whenFactory);
      }
   }

   public static interface Function0<T> {
      public T apply();
   }

   public static void main(String[] args) {
      when = "Creation";
      final Factory factory = new Factory();
      Function0 closure = new Function0() {
         public String apply() {
            return factory.make().sayWhen();
         }
      };
      when = "Application";
      // factory = new Factory();           // Cannot change factory in Java
      String result = (String)closure.apply();
      System.out.println(result);
      System.exit(0);
   }
}