// http://cs.fit.edu/~ryan/java/programs/break/break.html
//
// Break.java -- illustrate "break" and "continue" in Java

/*
   Without labels "break" and "continue" are the same in Java
   as in C/C++.   With labels "break" and "continue" perform
   their function no matter how deeply nested.
*/

class Break {

   public static void main (String args[]) {

      for (int i=1; i<=4; i++) {

         int j;

         System.out.println ("point A.  'break' on iteration i=" + i);

         for (j=1; j<=3; j++) {
            System.out.println ("point B.  j=" + j);
            if (i==j) break;
            System.out.println ("point C.  j=" + j);
         }

         System.out.println("\npoint D.  'continue' on iteration i="+i);

         for (j=1; j<=3; j++) {
            System.out.println ("point E.  j=" + j);
            if (i==j) continue;
            System.out.println ("point F.  j=" + j);
         }

         System.out.println ("\npoint G.  'for'/'switch' i=" + i);
        
         loop:  for (j=1; j<=3; j++) {
            System.out.println ("point H.  j=" + j);
            switch (i) {
            case 1:
               break;
            case 2:
               continue;
            case 3:
               break loop;
            case 4:
               continue loop;      // same as case 2, since "continue"
                                   // does not apply to "switch"
            }
            System.out.println ("point I.  j=" + j);
         }

         System.out.println ("\npoint J.  i=" + i);
         System.out.println ("\n");
      }
   }
}

