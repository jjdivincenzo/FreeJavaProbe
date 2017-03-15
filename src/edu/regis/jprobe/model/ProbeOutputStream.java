///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////
package edu.regis.jprobe.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author jdivince
 *
 */
public class ProbeOutputStream extends ObjectOutputStream {

    private long bytesWritten = 0;
    private long bytesWrittenUncompressed = 0;
    
 
    public ProbeOutputStream(OutputStream os) throws IOException {
        super(os);
       
    }
 
    
    public void writeObject(Object obj, boolean compressed) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        byte[] data = baos.toByteArray();
        
        if (compressed) {
            bytesWrittenUncompressed += data.length;
            byte[] comp = Utilities.compress(data);
            super.writeObject(comp);
            bytesWritten += comp.length;
        } else {
            super.writeObject(data);
            bytesWritten += data.length;
        }

    }


    public void close() throws IOException {
        super.close();
        //System.out.println("Bytes Written = " + bytesWritten);
        //System.out.println("Bytes Written Uncompressed = " + bytesWrittenUncompressed);
        
    }
    /**
     * @return the bytesWritten
     */
    public long getBytesWritten() {
        return bytesWritten;
    }
    /**
     * @return the bytesWrittenUncompressed
     */
    public long getBytesWrittenUncompressed() {
        return bytesWrittenUncompressed;
    }
    

}
