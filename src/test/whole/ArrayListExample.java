// -----------------------------------------------------------------------------
// ArrayListExample.java
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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collections;
import java.util.Random;

/**
 * -----------------------------------------------------------------------------
 * The following class provides an example of storing and retrieving objects 
 * from an ArrayList.
 * 
 * A List corresponds to an "ordered" group of elements where duplicates are
 * allowed.
 * 
 * An ArrayList is a good choice for simple sequences.
 * It is an Array based implementation where elements of the List can be
 * accessed directly through get() and set() methods.
 * 
 * ArrayList's give great performance on get() and set() methods, but do not
 * perform well on add() and remove() methods when compared to a LinkedList.
 * 
 * @version 1.0
 * @author  Jeffrey M. Hunter  (jhunter@idevelopment.info)
 * @author  http://www.idevelopment.info
 * -----------------------------------------------------------------------------
 */

public class ArrayListExample {


    /**
     * Provides an example of how to work with the ArrayList container.
     */
    public void doArrayListExample() {

        final int MAX = 10;
        int counter = 0;

        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Create/Store objects in an ArrayList container.                     |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        List listA = new ArrayList();
        List listB = new ArrayList();

        for (int i = 0; i < MAX; i++) {
            System.out.println("  - Storing Integer(" + i + ")");
            listA.add(new Integer(i));
        }

        System.out.println("  - Storing String(Alex)");
        listA.add("Alex");

        System.out.println("  - Storing String(Melody)");
        listA.add("Melody");

        System.out.println("  - Storing String(Jeff)");
        listA.add("Jeff");

        System.out.println("  - Storing String(Alex)");
        listA.add("Alex");

        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Retrieve objects in an ArrayList container using an Iterator.       |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        Iterator i = listA.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Retrieve objects in an ArrayList container using a ListIterator.    |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        counter = 0;
        ListIterator li = listA.listIterator();
        while (li.hasNext()) {
            System.out.println("Element [" + counter + "] = " + li.next());
            System.out.println("  - hasPrevious    = " + li.hasPrevious());
            System.out.println("  - hasNext        = " + li.hasNext());
            System.out.println("  - previousIndex  = " + li.previousIndex());
            System.out.println("  - nextIndex      = " + li.nextIndex());
            System.out.println();
            counter++;
        }


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Retrieve objects in an ArrayList container using index.             |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        for (int j=0; j < listA.size(); j++) {
            System.out.println("[" + j + "] - " + listA.get(j));
        }


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Search for a particular Object and return its index location.       |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        int locationIndex = listA.indexOf("Jeff");
        System.out.println("Index location of the String \"Jeff\" is: " + locationIndex);  


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Search for an object and return the first and last (highest) index. |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("First occurance search for String \"Alex\".  Index =  " + listA.indexOf("Alex"));
        System.out.println("Last Index search for String \"Alex\".       Index =  " + listA.lastIndexOf("Alex"));


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Extract a sublist from the main list, then print the new List.      |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        List listSub = listA.subList(10, listA.size());
        System.out.println("New Sub-List from index 10 to " + listA.size() + ": " + listSub);


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Sort the Sub-List created above.                                    |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("Original List   : " + listSub);
        Collections.sort(listSub);
        System.out.println("New Sorted List : " + listSub);
        System.out.println();


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Reverse the Sub-List created above.                                 |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("Original List     : " + listSub);
        Collections.reverse(listSub);
        System.out.println("New Reversed List : " + listSub);
        System.out.println();


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Check to see if the Lists are empty.                                |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("Is List A empty?   " + listA.isEmpty());
        System.out.println("Is List B empty?   " + listB.isEmpty());
        System.out.println("Is Sub-List empty? " + listSub.isEmpty());


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Clone the initial List.                                             |");
        System.out.println("| NOTE: The contents of the List are object references, so both       |");
        System.out.println("|       of the List's contain the same exact object reference's.      |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("List A   (before) : " + listA);
        System.out.println("List B   (before) : " + listB);
        System.out.println("Sub-List (before) : " + listSub);
        System.out.println();
        System.out.println("Are List's A and B equal? " + listA.equals(listB));
        System.out.println();
        listB = new ArrayList(listA);
        System.out.println("List A   (after)  : " + listA);
        System.out.println("List B   (after)  : " + listB);
        System.out.println("Sub-List (after)  : " + listSub);
        System.out.println();
        System.out.println("Are List's A and B equal? " + listA.equals(listB));


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Shuffle the elements around in some Random order for List A.        |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("List A   (before) : " + listA);
        System.out.println("List B   (before) : " + listB);
        System.out.println("Sub-List (before) : " + listSub);
        System.out.println();
        System.out.println("Are List's A and B equal? " + listA.equals(listB));
        System.out.println();
        Collections.shuffle(listA, new Random());
        System.out.println("List A   (after)  : " + listA);
        System.out.println("List B   (after)  : " + listB);
        System.out.println("Sub-List (after)  : " + listSub);
        System.out.println();
        System.out.println("Are List's A and B equal? " + listA.equals(listB));


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Convert a List to an Array.                                         |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        Object[] objArray = listA.toArray();
        for (int j=0; j < objArray.length; j++) {
            System.out.println("Array Element [" + j + "] = " + objArray[j]);
        }


        System.out.println();
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("| Remove (clear) Elements from List A.                                |");
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println();

        System.out.println("List A   (before) : " + listA);
        System.out.println("List B   (before) : " + listB);
        System.out.println();
        listA.clear();
        System.out.println("List A   (after)  : " + listA);
        System.out.println("List B   (after)  : " + listB);
        System.out.println();

    }


    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    public static void main(String[] args) {
        ArrayListExample listExample = new ArrayListExample();
        listExample.doArrayListExample();
    }

}
