// -----------------------------------------------------------------------------
// QuintessentialCollection.java
// -----------------------------------------------------------------------------

/*
 * =============================================================================
 * Copyright (c) 1998-2007 Jeffrey M. Hunter. All rights reserved.
 * 
 * All source code and material located at the Internet address of
 * http://www.idevelopment.info is the copyright of Jeffrey M. Hunter and
 * is protected under copyright laws of the United States. This source code may
 * not be hosted on any other site without my express, prior, written
 * permission. Application to host any of the material elsewhere can be made by
 * contacting me at jhunter@idevelopment.info.
 *
 * I have made every effort and taken great care in making sure that the source
 * code and other content included on my web site is technically accurate, but I
 * disclaim any and all responsibility for any loss, damage or destruction of
 * data or any other property which may arise from relying on it. I will in no
 * case be liable for any monetary damages arising from such loss, damage or
 * destruction.
 * 
 * As with any code, ensure to test this code in a development environment 
 * before attempting to run it in production.
 * =============================================================================
 */
 
import java.util.*;

/**
 * -----------------------------------------------------------------------------
 * Used to provide an example of printing a container.  Unlike arrays, 
 * containers require no special formating help when being printed.
 * 
 * @version 1.0
 * @author  Jeffrey M. Hunter  (jhunter@idevelopment.info)
 * @author  http://www.idevelopment.info
 * -----------------------------------------------------------------------------
 */

public class QuintessentialCollection {


    /**
     * An overloaded method from the Collection class.
     */
    public static Collection fill(Collection c) {
        c.add("Enterprise Server");
        c.add("Department Server");
        c.add("Workstation");
        return c;
    }


    /**
     * An overloaded method from the Map class.
     */
    public static Map fill(Map m) {
        m.put("Enterprise Server", "Sun Solaris");
        m.put("Enterprise Server", "HP-UX");
        m.put("Department Server", "Linux");
        m.put("Workstation", "MS Windows");
        m.put("Workstation", "Macintosh");
        return m;
    }


    /**
     * Provides an example of the default printing behavior of the Collection
     * and Map classes. This is provided by the various toString() methods of
     * the containers.
     */
    public static void doPrintContainer() {

        Collection p1;
        Map m1;

        List  a1 = new ArrayList();
        p1 = fill(a1);
        System.out.println("\n");
        System.out.println("+-------------+");
        System.out.println("| ArrayList   |");
        System.out.println("+-------------+");
        System.out.println("  - Ordered group of elements");
        System.out.println("  - Duplicates allowed");
        System.out.println("================================");
        System.out.println("    " + p1 + "\n");


        List a2 = new LinkedList();
        p1 = fill(a2);
        System.out.println("+-------------+");
        System.out.println("| LinkedList  |");
        System.out.println("+-------------+");
        System.out.println("  - Ordered (by entry into the list) group of elements");
        System.out.println("  - Duplicates allowed");
        System.out.println("================================");
        System.out.println("    " + p1 + "\n");


        Set a3 = new HashSet();
        p1 = fill(a3);
        System.out.println("+-------------+");
        System.out.println("| HashSet     |");
        System.out.println("+-------------+");
        System.out.println("  - No ordering of elements");
        System.out.println("  - No duplicates are allowed");
        System.out.println("  - add, remove, and contains methods constant time");
        System.out.println("    complexity: O(c).");
        System.out.println("================================");
        System.out.println("    " + p1 + "\n");


        Set a4 = new TreeSet();
        p1 = fill(a4);
        System.out.println("+-------------+");
        System.out.println("| TreeSet     |");
        System.out.println("+-------------+");
        System.out.println("  - Ordered (by element ASCII value) group of elements");
        System.out.println("  - No duplicates are allowed");
        System.out.println("  - add, remove, and contains methods logarithmic");
        System.out.println("    time complexity: O(log(n)), where n is the number of");
        System.out.println("    elements in the group.");
        System.out.println("================================");
        System.out.println("    " + p1 + "\n");


        Map a5 = new HashMap();
        m1 = fill(a5);
        System.out.println("+-------------+");
        System.out.println("| HashMap     |");
        System.out.println("+-------------+");
        System.out.println("  - No ordering on (key, value) pairs.");
        System.out.println("  - A Map is an object that maps keys to values.");
        System.out.println("  - Also called an Associative Array or Dictionary.");
        System.out.println("================================");
        System.out.println("    " + m1 + "\n");


        Map a6 = new TreeMap();
        m1 = fill(a6);
        System.out.println("+-------------+");
        System.out.println("| TreeMap     |");
        System.out.println("+-------------+");
        System.out.println("  - (key, value) pairs are ordered on the key.");
        System.out.println("  - The implementation is based on red-black tree structure.");
        System.out.println("  - A Map is an object that maps keys to values.");
        System.out.println("  - Also called an Associative Array or Dictionary.");
        System.out.println("================================");
        System.out.println("    " + m1 + "\n");

    }


    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    public static void main(String[] args) {
        doPrintContainer();
    }

}
