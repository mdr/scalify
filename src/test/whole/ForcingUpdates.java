// -----------------------------------------------------------------------------
// ForcingUpdates.java
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
 
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * -----------------------------------------------------------------------------
 * This program demonstrates how to force updates to a file to a disk. In some
 * applications, such as transaction processing, it is necessary to ensure that
 * an update has been made to the disk. FileDescriptor.sunc() blocks until
 * all changes to a file are written to disk.
 * 
 * @version 1.0
 * @author  Jeffrey M. Hunter  (jhunter@idevelopment.info)
 * @author  http://www.idevelopment.info
 * -----------------------------------------------------------------------------
 */

public class ForcingUpdates {

    private static void doForceUpdate() {

        try {
        
            // Open or create the output file
            String           fn = "outfile.txt";
            FileOutputStream os = new FileOutputStream(fn);
            FileDescriptor   fd = os.getFD();
            System.out.println("Opened file: " + fn);

            // Write some data to the stream
            byte[] data = new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
			// var data: Array[Byte] =
			// 		List(0xCA.asInstanceOf[Byte], 0xFE.asInstanceOf[Byte],
			//      	 0xBA.asInstanceOf[Byte], 0xBE.asInstanceOf[Byte]).toArray
            os.write(data);
            System.out.println("Wrote the following data to stream: " + data);

            // Flush the data from the streams and writers into system buffers.
            // The data may or may not be written to disk.
            System.out.println("Flushing from streams to system buffers.");
            os.flush();

            // Block until the system buffers have been written to disk.
            // After this method returns, the data is guaranteed to have been
            // written to disk.
            System.out.println("Blocking until system buffers have been written.");
            fd.sync();

            // Close out the file stream
            os.close();
            System.out.println("Closed file: " + fn);
        
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    public static void main(String[] args) {
        doForceUpdate();
    }

}
